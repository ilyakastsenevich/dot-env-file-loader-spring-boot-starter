package io.github.ilyakastsenevich.dotenvloader.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DotEnvParser {

  private static final String HASH_COMMENT_PREFIX = "#";
  private static final String SEMICOLON_COMMENT_PREFIX = ";";
  private static final char DOUBLE_QUOTE = '"';
  private static final char SINGLE_QUOTE = '\'';

  private DotEnvParser() {
  }

  public static Map<String, Object> parseLines(List<String> lines) {
    Map<String, Object> properties = new LinkedHashMap<>();
    for (String line : lines) {
      Property property = parseLine(line);
      if (property != null) {
        properties.put(property.key(), property.value());
      }
    }
    return properties;
  }

  private static Property parseLine(String line) {
    String trimmedLine = line.strip();
    if (isIgnoredLine(trimmedLine)) {
      return null;
    }

    int separatorIndex = trimmedLine.indexOf('=');
    if (separatorIndex <= 0) {
      return null;
    }

    String key = trimmedLine.substring(0, separatorIndex).strip();
    if (key.isEmpty()) {
      return null;
    }

    String value = parseValue(trimmedLine.substring(separatorIndex + 1).strip());
    return new Property(key, value);
  }

  private static boolean isIgnoredLine(String line) {
    return line.isEmpty() || startsWithComment(line);
  }

  private static String parseValue(String value) {
    if (value.isEmpty()) {
      return value;
    }

    String quotedValue = parseQuotedValue(value);
    if (quotedValue != null) {
      return quotedValue;
    }

    return stripTrailingComment(value);
  }

  private static String parseQuotedValue(String value) {
    char quote = value.charAt(0);
    if (quote != DOUBLE_QUOTE && quote != SINGLE_QUOTE) {
      return null;
    }

    int closingQuoteIndex = value.indexOf(quote, 1);
    if (closingQuoteIndex <= 0) {
      return null;
    }

    String trailing = value.substring(closingQuoteIndex + 1).stripLeading();
    if (!trailing.isEmpty() && !startsWithComment(trailing)) {
      return null;
    }

    return value.substring(1, closingQuoteIndex);
  }

  private static String stripTrailingComment(String value) {
    int hashCommentIndex = value.indexOf(" #");
    int semicolonCommentIndex = value.indexOf(" ;");

    int commentStartIndex;
    if (hashCommentIndex < 0) {
      commentStartIndex = semicolonCommentIndex;
    } else if (semicolonCommentIndex < 0) {
      commentStartIndex = hashCommentIndex;
    } else {
      commentStartIndex = Math.min(hashCommentIndex, semicolonCommentIndex);
    }

    if (commentStartIndex < 0) {
      return value;
    }
    return value.substring(0, commentStartIndex).strip();
  }

  private static boolean startsWithComment(String value) {
    return value.startsWith(HASH_COMMENT_PREFIX) || value.startsWith(SEMICOLON_COMMENT_PREFIX);
  }

  private record Property(String key, String value) {
  }
}
