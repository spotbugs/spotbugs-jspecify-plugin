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
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.asm.ClassNodeDetector;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.FieldDescriptor;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.classfile.engine.asm.FindBugsASM;
import edu.umd.cs.findbugs.internalAnnotations.SlashedClassName;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Set;
import org.jspecify.nullness.NullMarked;
import org.jspecify.nullness.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeedlessAnnotationDetector extends ClassNodeDetector {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Set<Type> INTRINSICALLY_NOT_NULLABLE_TYPES =
      Set.of(
          Type.BOOLEAN_TYPE,
          Type.BYTE_TYPE,
          Type.CHAR_TYPE,
          Type.INT_TYPE,
          Type.LONG_TYPE,
          Type.FLOAT_TYPE,
          Type.DOUBLE_TYPE);

  @Nullable private Nullness nullness;
  @SlashedClassName @Nullable private ClassDescriptor classDescriptor;

  public NeedlessAnnotationDetector(BugReporter bugReporter) {
    super(bugReporter);
  }

  private boolean canBeNull(Type type) {
    final XClass clazz;
    try {
      clazz = Global.getAnalysisCache().getClassAnalysis(XClass.class, classDescriptor);
    } catch (CheckedAnalysisException e) {
      bugReporter.reportMissingClass(classDescriptor);
      return false;
    }
    return !INTRINSICALLY_NOT_NULLABLE_TYPES.contains(type)
        && !clazz.getSuperclassDescriptor().matches(Enum.class);
  }

  @Override
  public void visitClass(ClassDescriptor classDescriptor) throws CheckedAnalysisException {
    this.classDescriptor = classDescriptor;
    super.visitClass(classDescriptor);
  }

  @Override
  public org.objectweb.asm.FieldVisitor visitField(
      int access, String name, String descriptor, String signature, Object value) {
    boolean isStatic = (access & Modifier.STATIC) != 0;
    FieldDescriptor fieldDescriptor =
        new FieldDescriptor(classDescriptor.getClassName(), name, descriptor, isStatic);
    return new FieldVisitor(
        FindBugsASM.ASM_VERSION,
        fieldDescriptor,
        nullness == null ? Nullness.NO_EXPLICIT_CONFIG : nullness);
  }

  @Override
  public MethodVisitor visitMethod(
      int access, String name, String descriptor, String signature, String[] exceptions) {
    MethodDescriptor methodDescriptor =
        new MethodDescriptor(classDescriptor.getClassName(), name, descriptor);
    return new MethodVisitor(
        FindBugsASM.ASM_VERSION,
        methodDescriptor,
        nullness == null ? Nullness.NO_EXPLICIT_CONFIG : nullness);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    Nullness.from(descriptor)
        .ifPresent(
            newNullness -> {
              if (this.nullness != null) {
                // TODO jspecify_conflicting_annotations
              }
              this.nullness = newNullness;
            });

    return super.visitAnnotation(descriptor, visible);
  }

  @NullMarked
  class FieldVisitor extends org.objectweb.asm.FieldVisitor {
    private final FieldDescriptor fieldDescriptor;
    /** Default nullness in the current scope. */
    private final Nullness defaultNullness;
    /** Nullness specified by the annotation on this method's return value. */
    @Nullable private Nullness nullness;

    FieldVisitor(int api, FieldDescriptor fieldDescriptor, Nullness defaultNullness) {
      super(api);
      this.defaultNullness = defaultNullness;
      this.fieldDescriptor = fieldDescriptor;
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      Nullness nullnessOfReturnedValue = nullness == null ? defaultNullness : nullness;
      Type type = Type.getType(fieldDescriptor.getSignature());
      if (!canBeNull(type)
          && nullnessOfReturnedValue.isSetExplicitly()
          && nullnessOfReturnedValue != Nullness.NOT_NULL) {
        log.info("{} is annotated as nullable, but {} cannot be null", fieldDescriptor, type);
        bugReporter.reportBug(
            new BugInstance(
                    "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE", Priorities.HIGH_PRIORITY)
                .addClass(classDescriptor)
                .addField(
                    fieldDescriptor.getSlashedClassName(),
                    fieldDescriptor.getName(),
                    fieldDescriptor.getSignature(),
                    fieldDescriptor.isStatic()));
      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      Nullness.from(descriptor)
          .ifPresent(
              newNullness -> {
                if (defaultNullness == newNullness) {
                  // TODO 重複したアノテーション
                } else if (nullness != null) {
                  // TODO jspecify_conflicting_annotations
                }
                this.nullness = newNullness;
              });
      return super.visitAnnotation(descriptor, visible);
    }
  }

  @NullMarked
  class MethodVisitor extends org.objectweb.asm.MethodVisitor {
    private final MethodDescriptor methodDescriptor;
    /** Default nullness in the current scope. */
    private final Nullness defaultNullness;
    /** Nullness specified by the annotation on this method's return value. */
    @Nullable private Nullness nullness;

    MethodVisitor(int api, MethodDescriptor methodDescriptor, Nullness defaultNullness) {
      super(api);
      this.defaultNullness = defaultNullness;
      this.methodDescriptor = methodDescriptor;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(
        int parameter, String descriptor, boolean visible) {
      // TODO check annotation on params
      return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      Nullness nullnessOfReturnedValue = nullness == null ? defaultNullness : nullness;
      Type returnType = Type.getReturnType(methodDescriptor.getSignature());
      if (!canBeNull(returnType)
          && nullnessOfReturnedValue.isSetExplicitly()
          && nullnessOfReturnedValue != Nullness.NOT_NULL) {
        System.err.printf(
            "%s is annotated as nullable, but %s cannot be null%n", methodDescriptor, returnType);
        bugReporter.reportBug(
            new BugInstance(
                    "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE", Priorities.HIGH_PRIORITY)
                .addClass(classDescriptor)
                .addMethod(
                    classDescriptor.getClassName(),
                    methodDescriptor.getName(),
                    methodDescriptor.getSignature(),
                    methodDescriptor.isStatic())
                .addClassAndMethod(methodDescriptor));
      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      Nullness.from(descriptor)
          .ifPresent(
              newNullness -> {
                if (defaultNullness == newNullness) {
                  // TODO 重複したアノテーション
                } else if (nullness != null) {
                  // TODO jspecify_conflicting_annotations
                }
                this.nullness = newNullness;
              });
      return super.visitAnnotation(descriptor, visible);
    }
  }
}
