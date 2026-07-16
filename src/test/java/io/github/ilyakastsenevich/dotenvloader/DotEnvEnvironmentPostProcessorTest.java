package io.github.ilyakastsenevich.dotenvloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.support.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

class DotEnvEnvironmentPostProcessorTest {

  private final DotEnvEnvironmentPostProcessor processor = new DotEnvEnvironmentPostProcessor();

  @Test
  void postProcessEnvironmentShouldLoadEnvFile(@TempDir Path tempDir) throws IOException {
    Path envFile = tempDir.resolve(".env");
    Files.writeString(envFile, "MY_KEY=my_value\nANOTHER_KEY=another_value");

    DotEnvEnvironmentPostProcessor testProcessor = new DotEnvEnvironmentPostProcessor(envFile);
    StandardEnvironment environment = new StandardEnvironment();
    testProcessor.postProcessEnvironment(environment, new SpringApplication());
    assertThat(environment.getProperty("MY_KEY")).isEqualTo("my_value");
    assertThat(environment.getProperty("ANOTHER_KEY")).isEqualTo("another_value");
  }

  @Test
  void postProcessEnvironmentShouldSkipWhenNoEnvFile(@TempDir Path tempDir) {
    Path envFile = tempDir.resolve(".env");
    DotEnvEnvironmentPostProcessor testProcessor = new DotEnvEnvironmentPostProcessor(envFile);
    StandardEnvironment environment = new StandardEnvironment();
    testProcessor.postProcessEnvironment(environment, new SpringApplication());
    assertThat(environment.getPropertySources().contains("dotEnvFile")).isFalse();
  }

  @Test
  void postProcessEnvironmentShouldSkipWhenDisabled(@TempDir Path tempDir) throws IOException {
    Path envFile = tempDir.resolve(".env");
    Files.writeString(envFile, "MY_KEY=my_value");

    DotEnvEnvironmentPostProcessor testProcessor = new DotEnvEnvironmentPostProcessor(envFile);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty("dotenvloader.enabled", "false");
    testProcessor.postProcessEnvironment(environment, new SpringApplication());
    assertThat(environment.getProperty("MY_KEY")).isNull();
  }

  @Test
  void postProcessEnvironmentShouldLoadWhenExplicitlyEnabled(@TempDir Path tempDir) throws IOException {
    Path envFile = tempDir.resolve(".env");
    Files.writeString(envFile, "MY_KEY=my_value");

    DotEnvEnvironmentPostProcessor testProcessor = new DotEnvEnvironmentPostProcessor(envFile);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty("dotenvloader.enabled", "true");
    testProcessor.postProcessEnvironment(environment, new SpringApplication());
    assertThat(environment.getProperty("MY_KEY")).isEqualTo("my_value");
  }

  @Test
  void postProcessEnvironmentShouldPlaceDotEnvAfterSystemEnvironment(@TempDir Path tempDir) throws IOException {
    Path envFile = tempDir.resolve(".env");
    Files.writeString(envFile, "TEST_VAR=test_value");

    DotEnvEnvironmentPostProcessor testProcessor = new DotEnvEnvironmentPostProcessor(envFile);
    StandardEnvironment environment = new StandardEnvironment();
    testProcessor.postProcessEnvironment(environment, new SpringApplication());

    int sysEnvIndex = environment.getPropertySources()
        .stream()
        .map(p -> p.getName())
        .toList()
        .indexOf(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
    int dotEnvIndex = environment.getPropertySources()
        .stream()
        .map(p -> p.getName())
        .toList()
        .indexOf("dotEnvFile");

    assertThat(sysEnvIndex).isGreaterThanOrEqualTo(0);
    assertThat(dotEnvIndex).isEqualTo(sysEnvIndex + 1);
  }

  @Test
  void getOrderShouldBeBetweenSystemEnvironmentAndConfigData() {
    int order = processor.getOrder();
    assertThat(order).isGreaterThan(SystemEnvironmentPropertySourceEnvironmentPostProcessor.DEFAULT_ORDER);
    assertThat(order).isLessThan(ConfigDataEnvironmentPostProcessor.ORDER);
  }
}
