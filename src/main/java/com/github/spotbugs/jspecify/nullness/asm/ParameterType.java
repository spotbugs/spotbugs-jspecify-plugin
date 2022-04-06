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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.Type;

public final class ParameterType {
  private final List<TypeArgument> typeArguments;
  private final Type type;

  ParameterType(Type type, List<TypeArgument> typeArguments) {
    this.type = Objects.requireNonNull(type);
    this.typeArguments = Collections.unmodifiableList(typeArguments);
  }

  public List<TypeArgument> getTypeArguments() {
    return typeArguments;
  }

  public Type getType() {
    return type;
  }
}
