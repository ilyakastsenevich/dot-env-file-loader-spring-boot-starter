# .env file loader spring boot starter

Loads `.env` entries into the Spring `Environment` at application startup.

> [!IMPORTANT]
> **Requires Spring Boot 4.0+ and Java 17+.**

## How It Works

The starter registers an `EnvironmentPostProcessor` that runs before `application.yml`/`application.properties` are processed. It reads a `.env` file from the working directory, parses its key-value pairs, and inserts them into the environment after system environment properties but before file-based config sources.

**Property resolution order** (highest to lowest priority):

1. OS environment variables / system properties
2. `.env` file values
3. `application.yml` / `application.properties`
4. Config server sources

This means real environment variables override `.env` entries, and `.env` values override config files.

Since `.env` is loaded before cloud config sources, you can use `.env` to disable Spring Cloud Config repo loading entirely. For example, set `SPRING_CLOUD_CONFIG_ENABLED=false` in your `.env` file to skip the config server bootstrap.

## Usage

**Maven:**

```xml
<dependency>
    <groupId>io.github.ilyakastsenevich</groupId>
    <artifactId>dot-env-file-loader-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

```groovy
implementation 'io.github.ilyakastsenevich:dot-env-file-loader-spring-boot-starter:1.0.0'
```

Place a `.env` file in your project's working directory:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME="myapp"

# Application
APP_DEBUG=true
```

## Configuration

| Property | Type | Default | Description |
|---|---|---|---|
| `dotenvloader.enabled` | `boolean` | `true` | Enable or disable the `.env` file loader |

> **Note**: `dotenvloader.enabled` must be set via system property (`-Ddotenvloader.enabled=false`), environment variable (`DOTENVLOADER_ENABLED=false`), or command-line argument (`--dotenvloader.enabled=false`). It cannot be set in `application.properties` because the post-processor runs before config files are loaded.

## .env File Format

- Each line is a `KEY=value` pair
- Lines starting with `#` or `;` are comments
- Blank lines are ignored
- Values can be wrapped in single or double quotes
- Trailing inline comments are stripped (e.g. `KEY=value # comment`)
