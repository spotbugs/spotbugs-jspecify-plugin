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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.test.SpotBugsExtension;
import edu.umd.cs.findbugs.test.SpotBugsRunner;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcher;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Run test with samples bundled to the JSpecify project. */
@ExtendWith(SpotBugsExtension.class)
class TestWithSample {
  private static final String ABBREV = "JSPECIFY";

  // TODO resolve version dynamically
  private static final Path JAR =
      Paths.get("..", "jspecify", "build", "libs", "jspecify-0.1.0-SNAPSHOT.jar")
          .toAbsolutePath()
          .normalize();

  @TempDir File classFileDir;

  private static File[] listSamples() {
    // TODO support packages in the sample dir
    Path samples = Paths.get("..", "jspecify", "samples").normalize();
    return Arrays.stream(samples.toFile().listFiles())
        .filter(File::isFile)
        .filter(file -> file.getName().endsWith(".java"))
        .toArray(size -> new File[size]);
  }

  @ParameterizedTest(name = "Compile and run analysis on {0}")
  @MethodSource("listSamples")
  void test(File javaFile, SpotBugsRunner spotbugs) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager javaFileManager =
        compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      compile(javaFile, compiler, javaFileManager);
      List<BugInstanceMatcher> expectedBugs = new DiagnosticBuilder().build(javaFile);
      BugCollection bugs = spotbugs.performAnalysis(classFileDir.toPath());
      expectedBugs.forEach(expectedBug -> assertThat(bugs, hasItem(expectedBug)));
      assertEquals(expectedBugs.size(), countJSpecifyBugs(bugs));
    }
  }

  /**
   * Count bugs detected by JSpecify plugin.
   *
   * @param bugs non-null {@link BugCollection} instance
   * @return count of bugs detected by JSpecify plugin.
   */
  private long countJSpecifyBugs(BugCollection bugs) {
    return bugs.getCollection().stream().filter(bug -> ABBREV.equals(bug.getAbbrev())).count();
  }

  private void compile(
      File javaFile, JavaCompiler compiler, StandardJavaFileManager javaFileManager) {
    Iterable<? extends JavaFileObject> javaFileObjects =
        javaFileManager.getJavaFileObjects(javaFile);
    Boolean success =
        compiler
            .getTask(
                null,
                javaFileManager,
                null,
                List.of("-classpath", JAR.toString(), "-d", classFileDir.getPath()),
                null,
                javaFileObjects)
            .call();
    Assertions.assertTrue(success);
  }
}
