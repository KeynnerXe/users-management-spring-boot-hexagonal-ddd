package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

  private static final String HOST = "mysql.example.com";
  private static final int PORT = 15425;
  private static final String DATABASE = "crud_usuarios";
  private static final String USERNAME = "avnadmin";
  private static final String PASSWORD = "secret";
  private static final String SSL_MODE = "REQUIRED";

  @Test
  void shouldBuildJdbcUrlWithConfiguredSslMode() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE);

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo(
            "jdbc:mysql://mysql.example.com:15425/crud_usuarios"
                + "?sslMode=REQUIRED&serverTimezone=UTC&allowPublicKeyRetrieval=true");
  }

  @Test
  void shouldBuildPostgreSqlJdbcUrlWhenEngineIsPostgreSql() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig("postgres.example.com", 5432, "crud_usuarios", "pguser", "pgpass", "REQUIRED", "postgresql");

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo("jdbc:postgresql://postgres.example.com:5432/crud_usuarios?sslmode=require");
    assertThat(config.isPostgreSql()).isTrue();
  }

  @Test
  void shouldBuildPostgreSqlJdbcUrlWhenPortIs5432() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig("postgres.example.com", 5432, "crud_usuarios", "pguser", "pgpass", "DISABLED");

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo("jdbc:postgresql://postgres.example.com:5432/crud_usuarios?sslmode=disable");
    assertThat(config.isPostgreSql()).isTrue();
  }
}
