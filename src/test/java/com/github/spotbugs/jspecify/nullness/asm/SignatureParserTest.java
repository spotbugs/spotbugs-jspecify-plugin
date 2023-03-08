/*
 * Copyright (c) 2021-2022 The SpotBugs team.
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
package com.github.spotbugs.jspecify.nullness.asm;

import edu.umd.cs.findbugs.classfile.engine.asm.FindBugsASM;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

class SignatureParserTest {
  @Nested
  static class ParseReturnValueTest {
    @Test
    void voidMethod() {
      SignatureParser parser = new SignatureParser(FindBugsASM.ASM_VERSION, "()V");
      Assertions.assertEquals(
          Optional.of(new TypeArgumentWithType('?', Type.VOID_TYPE)), parser.getReturnedType());
    }

    @Test
    void primitiveMethod() {
      SignatureParser parser = new SignatureParser(FindBugsASM.ASM_VERSION, "()I");
      Assertions.assertEquals(
          Optional.of(new TypeArgumentWithType('?', Type.INT_TYPE)), parser.getReturnedType());
    }
  }

  @Nested
  static class ParseParameterTypeTest {
    @Test
    void noParameterType() {
      SignatureParser parser =
          new SignatureParser(FindBugsASM.ASM_VERSION, "(Ljava/lang/Object;)V");
      Assertions.assertTrue(parser.getParameterType(0).getTypeArguments().isEmpty());
    }

    @Test
    void singleParameterType() {
      SignatureParser parser =
          new SignatureParser(FindBugsASM.ASM_VERSION, "(Ljava/util/Set<Ljava/lang/Object;>;)V");
      TypeArgumentWithType typeArgument =
          (TypeArgumentWithType) parser.getParameterType(0).getTypeArguments().get(0);
      Assertions.assertEquals(Type.getType("Ljava/lang/Object;"), typeArgument.getType());
      Assertions.assertEquals('=', typeArgument.getWildcard());
    }

    @Test
    void doubleParameterType() {
      SignatureParser parser =
          new SignatureParser(FindBugsASM.ASM_VERSION, "(Ljava/util/Set<TT;+TT;>;)V");
      List<TypeArgument> typeArguments = parser.getParameterType(0).getTypeArguments();
      Assertions.assertEquals(2, typeArguments.size());
      Assertions.assertEquals(
          "T", ((TypeArgumentWithTypeVariable) typeArguments.get(0)).getTypeVariable());
    }

    @Test
    void singleParameterTypeWithWildcard() {
      SignatureParser parser =
          new SignatureParser(FindBugsASM.ASM_VERSION, "(LAnnotatedWildcard<*>;)V");
      Assertions.assertEquals(
          Type.getType("LAnnotatedWildcard;"), parser.getParameterType(0).getType());

      TypeArgumentWithType typeArgument =
          (TypeArgumentWithType) parser.getParameterType(0).getTypeArguments().get(0);
      Assertions.assertEquals('*', typeArgument.getWildcard());
    }

    @Test
    void singleParameterTypeWithExtends() {
      SignatureParser parser =
          new SignatureParser(FindBugsASM.ASM_VERSION, "(Ljava/util/Set<+Ljava/lang/Object;>;)V");
      TypeArgumentWithType typeArgument =
          (TypeArgumentWithType) parser.getParameterType(0).getTypeArguments().get(0);
      Assertions.assertEquals(Type.getType("Ljava/lang/Object;"), typeArgument.getType());
      Assertions.assertEquals('+', typeArgument.getWildcard());
    }

    @Test
    void singleParameterTypeWithSuper() {
      SignatureParser parser =
          new SignatureParser(FindBugsASM.ASM_VERSION, "(Ljava/util/Set<-Ljava/lang/Object;>;)V");
      TypeArgumentWithType typeArgument =
          (TypeArgumentWithType) parser.getParameterType(0).getTypeArguments().get(0);
      Assertions.assertEquals(Type.getType("Ljava/lang/Object;"), typeArgument.getType());
      Assertions.assertEquals('-', typeArgument.getWildcard());
    }
  }

  @Test
  void complexCase1() {
    SignatureParser parser =
        new SignatureParser(
            FindBugsASM.ASM_VERSION,
            "<T:Ljava/lang/Object;>(LMultiplePathsToTypeVariable$TBounded<TT;+TT;>;)Ljava/lang/Object;");
    List<TypeArgument> typeArguments = parser.getParameterType(0).getTypeArguments();
    Assertions.assertEquals(2, typeArguments.size());
    Assertions.assertEquals(
        "T", ((TypeArgumentWithTypeVariable) typeArguments.get(0)).getTypeVariable());
    Assertions.assertEquals('=', typeArguments.get(0).getWildcard());
    Assertions.assertEquals(
        "T", ((TypeArgumentWithTypeVariable) typeArguments.get(0)).getTypeVariable());
    Assertions.assertEquals('+', typeArguments.get(1).getWildcard());
  }

  @Test
  void complexCase2() {
    SignatureParser parser =
        new SignatureParser(
            FindBugsASM.ASM_VERSION,
            "(LNotNullMarkedAnnotatedInnerOfParameterized<TT;>.Nested;LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested;LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested;LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested.DoublyNested;LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested.DoublyNested;LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested.DoublyNested;LNotNullMarkedAnnotatedInnerOfParameterized$Lib<LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested.DoublyNested;>;LNotNullMarkedAnnotatedInnerOfParameterized$Lib<LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested.DoublyNested;>;LNotNullMarkedAnnotatedInnerOfParameterized$Lib<LNotNullMarkedAnnotatedInnerOfParameterized<*>.Nested.DoublyNested;>;)V");
  }

  @Test
  void complexCase3() {
    SignatureParser parser =
        new SignatureParser(FindBugsASM.ASM_VERSION, "(LArraySameType$Lib<[Ljava/lang/Object;>;)V");
    TypeArgumentWithType typeArgument =
        (TypeArgumentWithType) parser.getParameterType(0).getTypeArguments().get(0);
    Assertions.assertEquals(
        Type.getType("Ljava/lang/Object;"), typeArgument.getType().getElementType());
  }
}
