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
package com.github.spotbugs.jspecify.nullness;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.Priorities;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.generic.Type;

public class NeedlessAnnotationDetector extends BytecodeScanningDetector {
  private final Set<Type> INTRINSICALLY_NOT_NULLABLE_TYPES =
      Set.of(Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.INT, Type.LONG, Type.FLOAT, Type.DOUBLE);

  private final BugReporter reporter;

  public NeedlessAnnotationDetector(BugReporter reporter) {
    this.reporter = Objects.requireNonNull(reporter);
  }

  @Override
  public void visitParameterAnnotation(
      int p, String annotationClass, Map<String, ElementValue> map, boolean runtimeVisible) {
    Nullness.from(annotationClass)
        .map(nullness -> nullness.canBeNull())
        .ifPresent(
            nullness -> {
              Type type = getMethod().getArgumentTypes()[p];
              if (INTRINSICALLY_NOT_NULLABLE_TYPES.contains(type)) {
                BugInstance bug =
                    new BugInstance(
                            "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE",
                            Priorities.HIGH_PRIORITY)
                        .addClassAndMethod(this)
                        .addSourceLine(this);
                reporter.reportBug(bug);
              }
            });
    super.visitParameterAnnotation(p, annotationClass, map, runtimeVisible);
  }
}
