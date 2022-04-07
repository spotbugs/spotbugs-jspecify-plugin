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

import com.github.spotbugs.jspecify.nullness.asm.*;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.asm.ClassNodeDetector;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.classfile.*;
import edu.umd.cs.findbugs.classfile.engine.asm.FindBugsASM;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Objects;
import org.jspecify.nullness.NullMarked;
import org.jspecify.nullness.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeedlessAnnotationDetector extends ClassNodeDetector {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Nullable private Nullness nullness;
  @Nullable private ClassDescriptor classDescriptor;

  public NeedlessAnnotationDetector(BugReporter bugReporter) {
    super(bugReporter);
  }

  /** Return true if given type can be null. */
  private boolean canBeNull(Type type) {
    return type.getSort() > Type.DOUBLE;
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
        signature,
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

    /** Return true if visiting firld is an Enum field like {@code enum Foo {FOO;} } */
    private boolean isEnumField() {
      if (!fieldDescriptor.isStatic()) {
        return false;
      }

      try {
        XClass clazz = Global.getAnalysisCache().getClassAnalysis(XClass.class, classDescriptor);
        if (!clazz.getSuperclassDescriptor().matches(Enum.class)) {
          return false;
        }
      } catch (CheckedAnalysisException e) {
        bugReporter.reportMissingClass(classDescriptor);
        return false;
      }

      return Objects.equals(
          Type.getType(fieldDescriptor.getSignature()),
          Type.getType(classDescriptor.getSignature()));
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      Nullness nullnessOfReturnedValue = nullness == null ? defaultNullness : nullness;
      Type type = Type.getType(fieldDescriptor.getSignature());
      if ((isEnumField() || !canBeNull(type))
          && nullnessOfReturnedValue.isSetExplicitly()
          && nullnessOfReturnedValue != Nullness.NOT_NULL) {
        log.debug("{} is annotated as nullable, but {} cannot be null", fieldDescriptor, type);
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
    /** The signature of this method, could be null if the method is not generic. */
    @Nullable private final String signature;
    /** Nullness specified by the annotation on this method's return value. */
    @Nullable private Nullness nullness;

    MethodVisitor(
        int api, MethodDescriptor methodDescriptor, String signature, Nullness defaultNullness) {
      super(api);
      this.signature = signature;
      this.defaultNullness = defaultNullness;
      this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void visitParameter(String name, int access) {
      log.debug("visitParameter: signature {}, name {}", methodDescriptor.getSignature(), name);
      super.visitParameter(name, access);
    }

    private Type getAnnotated(TypeReference typeRef, TypePath typePath) {
      TypeArgument tempType = null;
      switch (typeRef.getSort()) {
        case TypeReference.METHOD_RETURN:
          return Type.getReturnType(methodDescriptor.getSignature());

        case TypeReference.METHOD_FORMAL_PARAMETER:
          int typeParamIndex = typeRef.getTypeParameterIndex();
          if (typePath == null) {
            // means the type of parameter is directly annotated, like `@Nullness int`
            return Type.getArgumentTypes(methodDescriptor.getSignature())[typeParamIndex];
          }
          log.debug("method is {}, signature is {}", methodDescriptor, signature);
          ParameterType param =
              ParameterTypeFinder.create(api, signature, methodDescriptor)
                  .getParameterType(typeParamIndex);

          int length = typePath.getLength();
          for (int i = 0; i < length; ++i) {
            switch (typePath.getStep(i)) {
              case TypePath.TYPE_ARGUMENT:
                int index = typePath.getStepArgument(i);
                tempType = param.getTypeArguments().get(index);
                break;

              case TypePath.ARRAY_ELEMENT:
                // means an element of array is annotated, like `@Nullness int[]`
                // TODO support nested array like int[][]
                if (tempType instanceof TypeArgumentWithType) {
                  return ((TypeArgumentWithType) tempType).getType().getElementType();
                }

                Type arrayType =
                    Type.getArgumentTypes(methodDescriptor.getSignature())[typeParamIndex];
                return arrayType.getElementType();
            }
          }
          // fall through
        default:
          log.debug(
              ">> sort {}, TypeArgumentIndex {}, TypeParameterIndex {}, TypeParameterBoundIndex {}, typePath {}",
              typeRef.getSort(),
              typeRef.getTypeArgumentIndex(),
              typeRef.getTypeParameterIndex(),
              typeRef.getTypeParameterBoundIndex(),
              typePath);
      }
      return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String descriptor, boolean visible) {
      TypeReference typeRefObj = new TypeReference(typeRef);
      if (typeRefObj.getSort() == TypeReference.METHOD_RECEIVER) {
        if (Nullness.from(descriptor).map(Nullness::isSetExplicitly).orElse(Boolean.FALSE)) {
          bugReporter.reportBug(
              new BugInstance(
                      "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE", Priorities.HIGH_PRIORITY)
                  .addType(classDescriptor.getSignature())
                  .addClassAndMethod(methodDescriptor));
        }
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
      }
      Type annotated = getAnnotated(typeRefObj, typePath);
      if (annotated != null && !canBeNull(annotated) && Nullness.from(descriptor).isPresent()) {
        bugReporter.reportBug(
            new BugInstance(
                    "JSPECIFY_NULLNESS_INTRINSICALLY_NOT_NULLABLE", Priorities.HIGH_PRIORITY)
                .addType(annotated.getDescriptor())
                .addClassAndMethod(methodDescriptor));
      }
      return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(
        int parameter, String descriptor, boolean visible) {
      Type[] types = Type.getArgumentTypes(methodDescriptor.getSignature());
      Type type = types[parameter];
      log.debug(
          "visitParameterAnnotation: {} method parameter ({}) is type {} and annotated with {}",
          methodDescriptor,
          parameter,
          type,
          descriptor);
      return null;
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      Nullness nullnessOfReturnedValue = nullness == null ? defaultNullness : nullness;
      Type returnType = Type.getReturnType(methodDescriptor.getSignature());
      if (!canBeNull(returnType)
          && nullnessOfReturnedValue.isSetExplicitly()
          && nullnessOfReturnedValue != Nullness.NOT_NULL) {
        log.debug(
            "{} is annotated as nullable, but {} cannot be null", methodDescriptor, returnType);
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
