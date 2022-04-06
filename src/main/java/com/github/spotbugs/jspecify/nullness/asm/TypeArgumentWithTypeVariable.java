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

public final class TypeArgumentWithTypeVariable extends TypeArgument {
  private final String typeVariable;

  TypeArgumentWithTypeVariable(char wildcard, String typeVariable) {
    super(wildcard);
    this.typeVariable = Objects.requireNonNull(typeVariable);
  }

  public String getTypeVariable() {
    return typeVariable;
  }

  @Override
  TypeArgument replaceWildcard(char wildcard) {
    return new TypeArgumentWithTypeVariable(wildcard, typeVariable);
  }

  @Override
  TypeArgument replaceType(Function<String, String> replace) {
    return new TypeArgumentWithTypeVariable(getWildcard(), replace.apply(typeVariable));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TypeArgumentWithTypeVariable that = (TypeArgumentWithTypeVariable) o;

    return Objects.equals(typeVariable, that.typeVariable);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (typeVariable != null ? typeVariable.hashCode() : 0);
    return result;
  }
}
