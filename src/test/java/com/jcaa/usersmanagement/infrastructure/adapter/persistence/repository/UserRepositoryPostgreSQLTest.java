package com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserRepositoryPostgreSQL")
@ExtendWith(MockitoExtension.class)
class UserRepositoryPostgreSQLTest {

  private static final String ID = "u-001";
  private static final String NAME = "John Doe";
  private static final String EMAIL = "john@example.com";
  private static final String HASH = "$2a$12$abcdefghijklmnopqrstuO";
  private static final String ROLE = "ADMIN";
  private static final String STATUS = "ACTIVE";
  private static final String CREATED_AT = "2024-01-01";
  private static final String UPDATED_AT = "2024-01-02";

  @Mock private DataSource dataSource;
  @Mock private Connection connection;
  @Mock private PreparedStatement statement;
  @Mock private ResultSet resultSet;

  private UserRepositoryPostgreSQL repository;
  private UserModel userModel;
  private UserId userId;
  private UserEmail userEmail;

  @BeforeEach
  void setUp() {
    repository = new UserRepositoryPostgreSQL(dataSource);
    userId = new UserId(ID);
    userEmail = new UserEmail(EMAIL);
    userModel =
        new UserModel(
            userId,
            new UserName(NAME),
            userEmail,
            UserPassword.fromHash(HASH),
            UserRole.ADMIN,
            UserStatus.ACTIVE);
  }

  private void configureStatementAndResultSet() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
  }

  private void configureResultSetRow() throws SQLException {
    when(resultSet.getString("id")).thenReturn(ID);
    when(resultSet.getString("name")).thenReturn(NAME);
    when(resultSet.getString("email")).thenReturn(EMAIL);
    when(resultSet.getString("password")).thenReturn(HASH);
    when(resultSet.getString("role")).thenReturn(ROLE);
    when(resultSet.getString("status")).thenReturn(STATUS);
    when(resultSet.getString("created_at")).thenReturn(CREATED_AT);
    when(resultSet.getString("updated_at")).thenReturn(UPDATED_AT);
  }

  @Test
  @DisplayName("save() executes INSERT and returns the persisted user fetched by id")
  void shouldSaveUserAndReturnById() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(true);
    configureResultSetRow();

    final UserModel result = repository.save(userModel);

    assertAll(
        "save() happy path",
        () -> assertNotNull(result),
        () -> assertEquals(ID, result.getId().value()),
        () -> assertEquals(NAME, result.getName().value()),
        () -> assertEquals(EMAIL, result.getEmail().value()),
        () -> assertEquals(HASH, result.getPassword().value()),
        () -> assertEquals(UserRole.ADMIN, result.getRole()),
        () -> assertEquals(UserStatus.ACTIVE, result.getStatus()));
  }

  @Test
  @DisplayName("save() throws PersistenceException when INSERT fails")
  void shouldThrowPersistenceExceptionWhenSaveFails() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeUpdate()).thenThrow(new SQLException("connection lost"));

    assertThrows(PersistenceException.class, () -> repository.save(userModel));
  }

  @Test
  @DisplayName("save() throws UserNotFoundException when user is not found after insert")
  void shouldThrowUserNotFoundExceptionWhenNotFoundAfterSave() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> repository.save(userModel));
  }

  @Test
  @DisplayName("update() executes UPDATE and returns the updated user")
  void shouldUpdateUserAndReturnById() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(true);
    configureResultSetRow();

    final UserModel result = repository.update(userModel);

    assertNotNull(result);
    assertEquals(ID, result.getId().value());
  }

  @Test
  @DisplayName("update() throws PersistenceException when UPDATE fails")
  void shouldThrowPersistenceExceptionWhenUpdateFails() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeUpdate()).thenThrow(new SQLException("deadlock"));

    assertThrows(PersistenceException.class, () -> repository.update(userModel));
  }

  @Test
  @DisplayName("getById() returns user when found")
  void shouldReturnUserWhenFoundById() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(true);
    configureResultSetRow();

    final Optional<UserModel> result = repository.getById(userId);

    assertTrue(result.isPresent());
    assertEquals(ID, result.get().getId().value());
  }

  @Test
  @DisplayName("getById() returns empty when not found")
  void shouldReturnEmptyWhenNotFoundById() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(false);

    final Optional<UserModel> result = repository.getById(userId);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("getById() throws PersistenceException on SQLException")
  void shouldThrowPersistenceExceptionOnGetByIdSQLException() throws SQLException {
    when(dataSource.getConnection()).thenThrow(new SQLException("timeout"));

    assertThrows(PersistenceException.class, () -> repository.getById(userId));
  }

  @Test
  @DisplayName("getByEmail() returns user when found")
  void shouldReturnUserWhenFoundByEmail() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(true);
    configureResultSetRow();

    final Optional<UserModel> result = repository.getByEmail(userEmail);

    assertTrue(result.isPresent());
    assertEquals(EMAIL, result.get().getEmail().value());
  }

  @Test
  @DisplayName("getByEmail() returns empty when not found")
  void shouldReturnEmptyWhenNotFoundByEmail() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(false);

    final Optional<UserModel> result = repository.getByEmail(userEmail);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("getByEmail() throws PersistenceException on SQLException")
  void shouldThrowPersistenceExceptionOnGetByEmailSQLException() throws SQLException {
    when(dataSource.getConnection()).thenThrow(new SQLException("error"));

    assertThrows(PersistenceException.class, () -> repository.getByEmail(userEmail));
  }

  @Test
  @DisplayName("getAll() returns list of users")
  void shouldReturnAllUsers() throws SQLException {
    configureStatementAndResultSet();
    when(resultSet.next()).thenReturn(true, false);
    configureResultSetRow();

    final List<UserModel> result = repository.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("getAll() throws PersistenceException on SQLException")
  void shouldThrowPersistenceExceptionOnGetAllSQLException() throws SQLException {
    when(dataSource.getConnection()).thenThrow(new SQLException("error"));

    assertThrows(PersistenceException.class, () -> repository.getAll());
  }

  @Test
  @DisplayName("delete() executes DELETE successfully")
  void shouldDeleteUser() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeUpdate()).thenReturn(1);

    assertDoesNotThrow(() -> repository.delete(userId));
    verify(statement).executeUpdate();
  }

  @Test
  @DisplayName("delete() throws PersistenceException on SQLException")
  void shouldThrowPersistenceExceptionOnDeleteSQLException() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeUpdate()).thenThrow(new SQLException("FK violation"));

    assertThrows(PersistenceException.class, () -> repository.delete(userId));
  }
}
