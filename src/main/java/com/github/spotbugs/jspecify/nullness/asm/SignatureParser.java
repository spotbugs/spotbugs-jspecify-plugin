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

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Consumer;
import org.jspecify.nullness.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that parses signature (in context of the ASM), to split them into {@link
 * org.objectweb.asm.Type} instances. It is similar to the {@code
 * edu.umd.cs.findbugs.ba.SignatureParser} provided by SpotBugs core, but provides more features
 * especially the generics support.
 *
 * @see edu.umd.cs.findbugs.ba.SignatureParser
 */
class SignatureParser {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MySignatureVisitor visitor;

  SignatureParser(int api, String signature) {
    Objects.requireNonNull(signature);
    log.debug("analysing signature {}", signature);
    SignatureReader reader = new SignatureReader(signature);
    visitor = new MySignatureVisitor(api);
    reader.accept(visitor);
  }

  public Optional<? extends TypeArgument> getReturnedType() {
    return Optional.ofNullable(visitor.returnType);
  }

  public ParameterType getParameterType(int argument) {
    assert visitor.parameterTypes.size() == visitor.typeArguments.size();
    if (0 <= argument && argument < visitor.parameterTypes.size()) {
      TypeArgument typeArgument = visitor.parameterTypes.get(argument);
      if (typeArgument instanceof TypeArgumentWithType) {
        TypeArgumentWithType withType = (TypeArgumentWithType) typeArgument;
        return new ParameterType(withType.getType(), visitor.typeArguments.get(argument));
      } else if (typeArgument instanceof TypeArgumentWithTypeVariable) {
        // TODO treat type variable properly
        TypeArgumentWithTypeVariable withTypeVariable = (TypeArgumentWithTypeVariable) typeArgument;
        return new ParameterType(
            Type.getObjectType(withTypeVariable.getTypeVariable()),
            visitor.typeArguments.get(argument));
      }
    }
    throw new IndexOutOfBoundsException(
        String.format(
            "The target method has only %d arguments but requested to get %dth parameter",
            visitor.parameterTypes.size(), argument));
  }

  public Optional<String> getFormalTypeParameter() {
    return Optional.ofNullable(visitor.formalTypeParameter);
  }

  private static final class MySignatureVisitor extends SignatureVisitor {
    @Nullable private String formalTypeParameter;
    @Nullable private TypeArgument returnType;
    /**
     * List of name of parameter types, e.g. {@code
     * "CaptureConvertedUnionNullToOtherUnionNull$Lib"}.
     */
    private final List<TypeArgument> parameterTypes = new ArrayList<>();

    private final List<List<TypeArgument>> typeArguments = new ArrayList<>();

    MySignatureVisitor(int api) {
      super(api);
    }

    @Override
    public SignatureVisitor visitReturnType() {
      return new TypeFinder(api, type -> this.returnType = type);
    }

    @Override
    public SignatureVisitor visitParameterType() {
      typeArguments.add(new ArrayList<>());

      return new TypeFinder(
          api,
          new Consumer<>() {
            private boolean isFirst = true;

            @Override
            public void accept(TypeArgument typeArgument) {
              if (isFirst) {
                parameterTypes.add(typeArgument);
                isFirst = false;
              } else {
                getLastTypeArguments().add(typeArgument);
              }
            }
          });
    }

    private List<TypeArgument> getLastTypeArguments() {
      return typeArguments.get(typeArguments.size() - 1);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
      formalTypeParameter = name;
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
      return new TypeFinder(api, type -> getLastTypeArguments().add(type));
    }
  }
}
