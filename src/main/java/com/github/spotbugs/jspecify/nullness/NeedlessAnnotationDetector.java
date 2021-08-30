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
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.util.ClassName;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.bcel.Repository;
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
  public void visitAnnotation(
      @DottedClassName String annotationClass,
      Map<String, ElementValue> map,
      boolean runtimeVisible) {
    if (visitingField()) {
      doVisit(getField().getType(), annotationClass);
    }
    super.visitAnnotation(annotationClass, map, runtimeVisible);
  }

  @Override
  public void visitParameterAnnotation(
      int p,
      @DottedClassName String annotationClass,
      Map<String, ElementValue> map,
      boolean runtimeVisible) {
    doVisit(getMethod().getArgumentTypes()[p], annotationClass);
    super.visitParameterAnnotation(p, annotationClass, map, runtimeVisible);
  }

  private boolean isEnum(Type type) throws ClassNotFoundException {
    String className = ClassName.fromFieldSignature(type.getSignature());
    if (className != null) {
      return Repository.lookupClass(className).isEnum();
    } else {
      return false;
    }
  }

  private void doVisit(Type type, @DottedClassName String annotationClass) {
    try {
      if (!INTRINSICALLY_NOT_NULLABLE_TYPES.contains(type) && !isEnum(type)) {
        return;
      }
    } catch (ClassNotFoundException e) {
      AnalysisContext.reportMissingClass(e);
      return;
    }

    System.err.printf("annotation %s on the type %s%n", annotationClass, type.getSignature());
    Nullness.from(annotationClass)
        .filter(Nullness::canBeNull)
        .ifPresent(
            nullness -> {
              BugInstance bug =
                  new BugInstance(
                      "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE", Priorities.HIGH_PRIORITY);
              if (visitingField()) {
                bug.addClass(this).addField(this);
              } else if (visitingMethod()) {
                bug.addClassAndMethod(this).addSourceLine(this);
              }
              reporter.reportBug(bug);
            });
  }
}
