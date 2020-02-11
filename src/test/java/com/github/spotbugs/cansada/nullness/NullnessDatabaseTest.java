/*
 * Copyright (c) 2019-present The SpotBugs team.
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
package com.github.spotbugs.cansada.nullness;

import static org.assertj.core.api.Assertions.assertThat;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.test.SpotBugsExtension;
import edu.umd.cs.findbugs.test.SpotBugsRunner;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpotBugsExtension.class)
class NullnessDatabaseTest {
  static final Path PATH =
      Paths.get("build/classes/java/test/com/github/spotbugs/cansada/nullness");

  @Test
  void test(SpotBugsRunner spotbugs) {
    BugCollection bugs = spotbugs.performAnalysis(PATH.resolve("AnnotatedWithNotNull.class"));
    Condition<BugInstance> condition =
        new BugInstanceConditionBuilder()
            .bugType("CANSADA_RETURN_UNEXPECTED_NULL")
            .atLine(25)
            .build();
    assertThat(bugs).haveExactly(1, condition);
  }
  @Test
  void testNeedMerge(SpotBugsRunner spotbugs) {
    BugCollection bugs = spotbugs.performAnalysis(PATH.resolve("AnnotatedWithNotNull.class"));
    Condition<BugInstance> condition =
        new BugInstanceConditionBuilder()
            .bugType("CANSADA_RETURN_UNEXPECTED_NULL")
            .atLine(36)
            .build();
    assertThat(bugs).haveExactly(1, condition);
  }
}
