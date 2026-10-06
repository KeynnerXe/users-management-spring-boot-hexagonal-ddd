package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

public record DatabaseConfig(
    String host,
    int port,
    String databaseName,
    String username,
    String password,
    String sslMode,
    String engine) {

  private static final String MYSQL_URL_TEMPLATE =
      "jdbc:mysql://%s:%d/%s?sslMode=%s&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  private static final String POSTGRESQL_URL_TEMPLATE =
      "jdbc:postgresql://%s:%d/%s?sslmode=%s";

  public DatabaseConfig(
      final String host,
      final int port,
      final String databaseName,
      final String username,
      final String password,
      final String sslMode) {
    this(host, port, databaseName, username, password, sslMode, "mysql");
  }

  public String buildJdbcUrl() {
    if (isPostgreSql()) {
      return String.format(POSTGRESQL_URL_TEMPLATE, host, port, databaseName, mapPostgreSqlSslMode(sslMode));
    }
    return String.format(MYSQL_URL_TEMPLATE, host, port, databaseName, sslMode);
  }

  public boolean isPostgreSql() {
    return (engine != null && (engine.equalsIgnoreCase("postgresql") || engine.equalsIgnoreCase("postgres")))
        || port == 5432;
  }

  private static String mapPostgreSqlSslMode(final String mode) {
    if (mode == null || mode.isBlank()) {
      return "disable";
    }
    return switch (mode.toUpperCase()) {
      case "REQUIRED", "REQUIRE" -> "require";
      case "VERIFY_CA", "VERIFY-CA" -> "verify-ca";
      case "VERIFY_IDENTITY", "VERIFY-FULL" -> "verify-full";
      case "PREFERRED", "PREFER" -> "prefer";
      default -> "disable";
    };
  }
}
