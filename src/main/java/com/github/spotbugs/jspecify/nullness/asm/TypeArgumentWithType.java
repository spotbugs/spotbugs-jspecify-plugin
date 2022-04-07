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

import java.util.Objects;
import java.util.function.Function;
import org.objectweb.asm.Type;

public final class TypeArgumentWithType extends TypeArgument {
  private final Type type;

  public TypeArgumentWithType(char wildcard, String name) {
    super(wildcard);
    this.type = Type.getType(Objects.requireNonNull(name));
  }

  TypeArgumentWithType(char wildcard, Type type) {
    super(wildcard);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  @Override
  TypeArgument replaceWildcard(char wildcard) {
    return new TypeArgumentWithType(wildcard, type);
  }

  @Override
  TypeArgument replaceType(Function<String, String> replace) {
    Type newType = Type.getType(replace.apply(type.getDescriptor()));
    return new TypeArgumentWithType(getWildcard(), newType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypeArgumentWithType that = (TypeArgumentWithType) o;

    return Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "TypeArgumentWithType{" + "wildcard=" + getWildcard() + ", type=" + type + '}';
  }
}
