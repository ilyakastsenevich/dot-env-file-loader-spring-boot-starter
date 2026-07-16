package io.github.ilyakastsenevich.dotenvloader.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DotEnvParserTest {

  @Test
  void shouldParseSimpleKeyValue() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=value"));
    assertThat(result).containsEntry("KEY", "value");
  }

  @Test
  void shouldTrimSpacesAroundKey() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("  KEY  =value"));
    assertThat(result).containsEntry("KEY", "value");
  }

  @Test
  void shouldIgnoreBlankLines() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY1=value1", "", "  ", "\t", "KEY2=value2"));
    assertThat(result).hasSize(2)
        .containsEntry("KEY1", "value1")
        .containsEntry("KEY2", "value2");
  }

  @Test
  void shouldIgnoreHashComments() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("# this is a comment", "KEY=value"));
    assertThat(result).hasSize(1).containsEntry("KEY", "value");
  }

  @Test
  void shouldIgnoreSemicolonComments() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("; this is a comment", "KEY=value"));
    assertThat(result).hasSize(1).containsEntry("KEY", "value");
  }

  @Test
  void shouldHandleDoubleQuotedValues() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=\"hello world\""));
    assertThat(result).containsEntry("KEY", "hello world");
  }

  @Test
  void shouldHandleSingleQuotedValues() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY='hello world'"));
    assertThat(result).containsEntry("KEY", "hello world");
  }

  @Test
  void shouldStripTrailingHashComment() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=value # comment"));
    assertThat(result).containsEntry("KEY", "value");
  }

  @Test
  void shouldStripTrailingSemicolonComment() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=value ; comment"));
    assertThat(result).containsEntry("KEY", "value");
  }

  @Test
  void shouldHandleEmptyValue() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY="));
    assertThat(result).containsEntry("KEY", "");
  }

  @Test
  void shouldIgnoreLinesWithoutSeparator() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("NOSEPARATOR", "KEY=value"));
    assertThat(result).hasSize(1).containsEntry("KEY", "value");
  }

  @Test
  void shouldIgnoreLinesStartingWithEquals() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("=value", "KEY=actual"));
    assertThat(result).hasSize(1).containsEntry("KEY", "actual");
  }

  @Test
  void shouldHandleValueWithEqualsSign() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=value=with=equals"));
    assertThat(result).containsEntry("KEY", "value=with=equals");
  }

  @Test
  void shouldHandleQuotedValueWithTrailingComment() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=\"value\" # comment"));
    assertThat(result).containsEntry("KEY", "value");
  }

  @Test
  void shouldHandleMultipleEntries() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of(
        "# Database config",
        "DB_HOST=localhost",
        "DB_PORT=5432",
        "DB_NAME=\"mydb\"",
        "",
        "# App config",
        "APP_NAME='MyApp'",
        "DEBUG=true # enable debug"
    ));
    assertThat(result).hasSize(5)
        .containsEntry("DB_HOST", "localhost")
        .containsEntry("DB_PORT", "5432")
        .containsEntry("DB_NAME", "mydb")
        .containsEntry("APP_NAME", "MyApp")
        .containsEntry("DEBUG", "true");
  }

  @Test
  void shouldHandleKeysWithDotsAndUnderscores() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("spring.datasource.url=jdbc:h2:mem:test"));
    assertThat(result).containsEntry("spring.datasource.url", "jdbc:h2:mem:test");
  }

  @Test
  void shouldKeepValueWithHashThatIsNotTrailingComment() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=val#ue"));
    assertThat(result).containsEntry("KEY", "val#ue");
  }

  @Test
  void shouldKeepValueWithSemicolonThatIsNotTrailingComment() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=val;ue"));
    assertThat(result).containsEntry("KEY", "val;ue");
  }

  @Test
  void shouldReturnEmptyMapForEmptyInput() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of());
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyMapForAllComments() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("# comment one", "# comment two"));
    assertThat(result).isEmpty();
  }

  @Test
  void shouldPreserveOrder() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("A=1", "B=2", "C=3"));
    assertThat(result.keySet()).containsExactly("A", "B", "C");
  }

  @Test
  void shouldHandleValueWithLeadingAndTrailingSpaces() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=  spaced value  "));
    assertThat(result).containsEntry("KEY", "spaced value");
  }

  @Test
  void shouldHandleSingleQuotedValueWithSemicolonComment() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY='value' ; comment"));
    assertThat(result).containsEntry("KEY", "value");
  }

  @Test
  void shouldTreatUnclosedDoubleQuoteAsRegularValue() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY=\"unclosed"));
    assertThat(result).containsEntry("KEY", "\"unclosed");
  }

  @Test
  void shouldTreatUnclosedSingleQuoteAsRegularValue() {
    Map<String, Object> result = DotEnvParser.parseLines(List.of("KEY='unclosed"));
    assertThat(result).containsEntry("KEY", "'unclosed");
  }
}
