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
package com.github.spotbugs.jspecify.nullness;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.Condition;
import org.jspecify.annotations.DefaultNonNull;
import org.jspecify.annotations.Nullable;

@DefaultNonNull
public class BugInstanceConditionBuilder {
  private static final Pattern ANON_FUNCTION_SCALA_PATTERN =
      Pattern.compile("\\$\\$anonfun\\$([^\\$]+)\\$");

  private Predicate<BugInstance> predicate = (bug) -> true;

  public BugInstanceConditionBuilder bugType(String type) {
    this.predicate = predicate.and(bug -> type.equals(bug.getBugPattern().getType()));
    return this;
  }

  public BugInstanceConditionBuilder atLine(int lineNumber) {
    this.predicate =
        predicate.and(
            bug -> {
              SourceLineAnnotation srcAnn = extractBugAnnotation(bug, SourceLineAnnotation.class);
              if (srcAnn == null) {
                return false;
              }
              return srcAnn.getStartLine() <= lineNumber && lineNumber <= srcAnn.getEndLine();
            });
    return this;
  }

  public BugInstanceConditionBuilder inClass(String className) {
    this.predicate =
        predicate.and(
            bug -> {
              ClassAnnotation classAnn = extractBugAnnotation(bug, ClassAnnotation.class);
              if (classAnn == null) {
                return false;
              }

              String fullName = classAnn.getClassName();
              if (fullName.equals(className)) {
                return true;
              }

              int startDot = fullName.lastIndexOf(".") + 1;
              int endDollar = fullName.indexOf('$');
              String simpleName =
                  fullName.substring(
                      startDot != -1 ? startDot : 0,
                      endDollar != -1 ? endDollar : fullName.length());
              if (simpleName.equals(className)) {
                return true;
              }

              String simpleNameInner =
                  fullName.substring(startDot != -1 ? startDot : 0, fullName.length());
              return simpleNameInner.equals(className);
            });
    return this;
  }

  public BugInstanceConditionBuilder inMethod(String methodName) {
    this.predicate =
        predicate.and(
            bug -> {
              MethodAnnotation methodAnn = extractBugAnnotation(bug, MethodAnnotation.class);
              if (methodAnn == null) {
                return false;
              }

              ClassAnnotation classAnn = extractBugAnnotation(bug, ClassAnnotation.class);
              String fullClassName = classAnn.getClassName();
              if (methodAnn.getMethodName().startsWith("apply") && fullClassName != null) {
                Matcher m = ANON_FUNCTION_SCALA_PATTERN.matcher(fullClassName);
                if (m.find()) { // Scala function enclose in
                  return methodAnn.getMethodName().equals(methodName)
                      || methodName.equals(m.group(1));
                }
              } else {
                return methodAnn.getMethodName().equals(methodName);
              }

              return true;
            });
    return this;
  }

  public Condition<BugInstance> build() {
    return new Condition<>(predicate, "Expected BugInstance");
  }

  @Nullable
  private static <T> T extractBugAnnotation(BugInstance bugInstance, Class<T> annotationType) {
    for (BugAnnotation annotation : bugInstance.getAnnotations()) {
      if (annotation.getClass().equals(annotationType)) {
        return annotationType.cast(annotation);
      }
    }
    return null;
  }
}
