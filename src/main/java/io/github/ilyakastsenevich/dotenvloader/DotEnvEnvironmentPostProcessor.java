package io.github.ilyakastsenevich.dotenvloader;

import io.github.ilyakastsenevich.dotenvloader.utils.DotEnvParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.support.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String DOT_ENV_FILE = ".env";
  private static final String PROPERTY_SOURCE_NAME = "dotEnvFile";

  private final Path dotEnvPath;

  public DotEnvEnvironmentPostProcessor() {
    this(Path.of(DOT_ENV_FILE));
  }

  DotEnvEnvironmentPostProcessor(Path dotEnvPath) {
    this.dotEnvPath = dotEnvPath;
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Boolean enabled = environment.getProperty("dotenvloader.enabled", Boolean.class, true);
    if (!enabled) {
      return;
    }

    if (!Files.isRegularFile(dotEnvPath)) {
      return;
    }

    Map<String, Object> properties;
    try {
      properties = DotEnvParser.parseLines(Files.readAllLines(dotEnvPath));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read " + dotEnvPath.getFileName(), e);
    }

    if (properties.isEmpty()) {
      return;
    }

    SystemEnvironmentPropertySource dotEnvSource = new SystemEnvironmentPropertySource(PROPERTY_SOURCE_NAME, properties);
    if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
      environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
          dotEnvSource);
    } else {
      environment.getPropertySources().addLast(dotEnvSource);
    }
  }

  @Override
  public int getOrder() {
    return Math.min(SystemEnvironmentPropertySourceEnvironmentPostProcessor.DEFAULT_ORDER + 1,
        ConfigDataEnvironmentPostProcessor.ORDER - 1);
  }
}
