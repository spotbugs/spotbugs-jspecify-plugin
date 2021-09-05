/*
 * Copyright (c) 2019-2021 The SpotBugs team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.spotbugs.jspecify;

import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcher;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcherBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is responsible to build diagnostic {@link BugInstanceMatcher}</a>
 *
 * @see <a
 *     href="https://github.com/jspecify/jspecify/blob/9a84cf66b0f897281337aa1fc7d82ed4b6dce12c/samples/README.md#syntax">The
 *     syntax of special comment</a>
 */
class DiagnosticBuilder {
  private static final String BUGTYPE_CONFLICTING_ANNOTATIONS = "JSPECIFY_CONFLICTING_ANNOTATIONS";
  private static final String BUGTYPE_NULLNESS_INTRINSICALLY_NOT_NULLABLE =
      "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE";

  private static final Map<String, String> COMMENT_TO_BUGTYPE =
      Map.of(
          "jspecify_nullness_intrinsically_not_nullable",
          BUGTYPE_NULLNESS_INTRINSICALLY_NOT_NULLABLE);

  static final class WithLine<E> {
    final int line;
    final E element;

    WithLine(int line, E element) {
      this.line = line;
      this.element = element;
    }
  }

  static final class WithBugType<E> {
    final int line;
    final E element;
    final String bugType;

    WithBugType(int line, E element, String bugType) {
      this.line = line;
      this.element = element;
      this.bugType = Objects.requireNonNull(bugType);
    }
  }

  static final class LineNumberCounter<T> {
    int lines = 0;

    WithLine<T> next(T element) {
      ++lines;
      return new WithLine<>(lines, element);
    }
  }

  List<BugInstanceMatcher> build(File javaFile) throws IOException {
    LineNumberCounter<String> counter = new LineNumberCounter<>();
    try (Stream<String> lines = Files.lines(javaFile.toPath(), StandardCharsets.UTF_8)) {
      return lines
          .map(counter::next)
          .flatMap(this::findBugType)
          .map(
              withBugType ->
                  new BugInstanceMatcherBuilder()
                      // TODO Field has no line number table
                      //                      .atLine(withBugType.line + 1)
                      .bugType(withBugType.bugType)
                      .build())
          .collect(Collectors.toList());
    }
  }

  private Stream<WithBugType<String>> findBugType(WithLine<String> withLine) {
    return COMMENT_TO_BUGTYPE.entrySet().stream()
        .flatMap(
            commentToBugType ->
                withLine.element.contains(commentToBugType.getKey())
                    ? Stream.of(commentToBugType.getValue())
                    : Stream.empty())
        .map(bugType -> new WithBugType<>(withLine.line, withLine.element, bugType));
  }
}
