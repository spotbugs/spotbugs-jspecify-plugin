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

import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import java.util.Collections;
import java.util.Objects;
import org.jspecify.nullness.Nullable;
import org.objectweb.asm.Type;

public abstract class ParameterTypeFinder {
  public static ParameterTypeFinder create(
      int api, @Nullable String signature, MethodDescriptor methodDescriptor) {
    if (signature == null) {
      return new BasedOnMethodDescriptor(methodDescriptor);
    } else {
      return new BasedOnSignature(new SignatureParser(api, signature));
    }
  }

  public abstract ParameterType getParameterType(int argument);

  static class BasedOnSignature extends ParameterTypeFinder {
    private final SignatureParser signatureParser;

    BasedOnSignature(SignatureParser signatureParser) {
      this.signatureParser = Objects.requireNonNull(signatureParser);
    }

    @Override
    public ParameterType getParameterType(int argument) {
      return signatureParser.getParameterType(argument);
    }
  }

  static class BasedOnMethodDescriptor extends ParameterTypeFinder {
    private final Type[] argumentTypes;

    BasedOnMethodDescriptor(String signature) {
      this.argumentTypes = Type.getArgumentTypes(Objects.requireNonNull(signature));
    }

    BasedOnMethodDescriptor(MethodDescriptor methodDescriptor) {
      this(Objects.requireNonNull(methodDescriptor).getSignature());
    }

    @Override
    public ParameterType getParameterType(int argument) {
      return new ParameterType(argumentTypes[argument], Collections.emptyList());
    }
  }
}
