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

import codeanalysis.experimental.annotations.NotNull;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.CustomUserValue;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.bcel.Const;

@CustomUserValue
public class ReturnUnexpectedNullDetector extends OpcodeStackDetector {
  @NotNull private final BugReporter reporter;

  public ReturnUnexpectedNullDetector(@NotNull BugReporter reporter) {
    this.reporter = Objects.requireNonNull(reporter);
  }

  @Override
  public void sawOpcode(int seen) {
    switch (seen) {
      case Const.ARETURN:
        if (isTargetMethod() && stack.getStackDepth() > 0) {
          Item item = stack.getStackItem(0);
          Nullness nullness = (Nullness) item.getUserValue();
          if (item.isNull() || (nullness != null && nullness.canBeNull())) {
            reporter.reportBug(
                new BugInstance("CANSADA_RETURN_UNEXPECTED_NULL", Priorities.HIGH_PRIORITY)
                    .addClassAndMethod(this)
                    .addSourceLine(this));
          }
        }
        return;
      default: // do nothing
    }
  }

  boolean isTargetMethod() {
    // TODO does it work with lambda?
    List<Nullness> nullnesses =
        getXMethod().getAnnotationDescriptors().stream()
            .map(ClassDescriptor::getClassName)
            .map(Nullness::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    final Nullness nullness;
    if (nullnesses.isEmpty()) {
      nullness = Nullness.UNKNOWN;
    } else if (nullnesses.size() == 1) {
      nullness = nullnesses.get(0);
    } else {
      throw new RuntimeException("Found multiple nullness annotations on methods");
    }
    return !nullness.canBeNull();
  }

  @Override
  public void afterOpcode(int code) {
    switch (code) {
      case Const.INVOKESPECIAL:
      case Const.INVOKEVIRTUAL:
        if (!getXMethodOperand().isReturnTypeReferenceType()) {
          return;
        }
        List<@NotNull Nullness> nullnesses =
            getXMethodOperand().getAnnotationDescriptors().stream()
                .map(desc -> Nullness.from(desc.getClassName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final Nullness nullness;
        if (nullnesses.isEmpty()) {
          nullness = Nullness.UNKNOWN;
        } else if (nullnesses.size() == 1) {
          nullness = nullnesses.get(0);
        } else {
          throw new RuntimeException("Found multiple nullness annotations on methods");
        }
        super.afterOpcode(code);
        stack.getStackItem(0).setUserValue(nullness);
        return;
      default:
        super.afterOpcode(code);
    }
  }
}
