/*
 * Copyright (c) 2021-2021 The SpotBugs team.
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
import edu.umd.cs.findbugs.OpcodeStack.CustomUserValue;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.Global;
import java.util.Objects;
import java.util.Optional;
import org.apache.bcel.Const;

@CustomUserValue
public class ReturnUnexpectedNullDetector extends OpcodeStackDetector {
  private final BugReporter reporter;

  public ReturnUnexpectedNullDetector(BugReporter reporter) {
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
                new BugInstance("JSPECIFY_RETURN_UNEXPECTED_NULL", Priorities.HIGH_PRIORITY)
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
    NullnessDatabase database = Global.getAnalysisCache().getDatabase(NullnessDatabase.class);
    Optional<Nullness> optional =
        database.findNullnessOf(getXClass(), getXMethod(), Global.getAnalysisCache());
    return optional.isPresent() && !optional.get().canBeNull();
  }

  @Override
  public void afterOpcode(int code) {
    switch (code) {
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKESTATIC:
      case Const.INVOKEVIRTUAL:
        XMethod methodOperand = getXMethodOperand();
        NullnessDatabase database = Global.getAnalysisCache().getDatabase(NullnessDatabase.class);
        Optional<Nullness> optional =
            database.findNullnessOf(getXClassOperand(), methodOperand, Global.getAnalysisCache());
        super.afterOpcode(code);
        optional.ifPresent(nullness -> stack.getStackItem(0).setUserValue(nullness));
        return;
        // constructor has no returned value
      default:
        super.afterOpcode(code);
    }
  }
}
