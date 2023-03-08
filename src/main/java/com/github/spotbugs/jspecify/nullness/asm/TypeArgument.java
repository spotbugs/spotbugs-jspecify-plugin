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

import java.util.function.Function;

public abstract class TypeArgument {
  private final char wildcard;

  TypeArgument(char wildcard) {
    this.wildcard = wildcard;
  }

  /**
   * Returns '=' for the {@code instanceof}, '+' for {@code extends}, and '-' for {@code super} type
   * argument.
   *
   * @see <a
   *     href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-WildcardIndicator">JVMS
   *     §4.7.9.1. Signatures</a>
   */
  public char getWildcard() {
    return wildcard;
  }

  abstract TypeArgument replaceWildcard(char wildcard);

  abstract TypeArgument replaceType(Function<String, String> replace);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypeArgument that = (TypeArgument) o;

    return wildcard == that.wildcard;
  }

  @Override
  public int hashCode() {
    return wildcard;
  }
}
