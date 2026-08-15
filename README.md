# Mine-asterisk Plugin

Enhances a vanilla Minecraft server with additional opinionated features, built on the Paper API.

## Features

- **Team**: Create, manage, and disband persistent Teams.
- **Enchantment**: Additional enchantments.

## Built With

- **Java** with the **Paper API**
- **Hibernate** for persistence
- **MySQL** for storage
- **Gradle** (Kotlin DSL) for builds
- **JUnit** for testing

## Testing

This project was developed test-first: tests were written before the
implementation for each feature, and the persistence and gameplay logic is
covered by a JUnit suite.

The test suite runs against a separate database, initialised from
`initialize-database-test.sql`, so it never touches development or production
data.

`./gradlew test`

## Installation

1. Initialise the database using `initialize-database.sql`.
2. Execute `./gradlew shadowJar`.
3. Copy the build output to the Minecraft server's plugin folder.
4. Start the Minecraft server.

## Plugin Dependencies

None.

## Disclaimer

This plugin is intended to be used only with the plugins listed under Plugin
Dependencies.
