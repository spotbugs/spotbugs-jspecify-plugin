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

import java.util.Optional;
import org.jspecify.nullness.Nullable;

enum Nullness {
  UNKNOWN(true),
  NULLABLE(true),
  NOT_NULL(false);

  private final boolean canBeNull;

  Nullness(boolean canBeNull) {
    this.canBeNull = canBeNull;
  }

  boolean canBeNull() {
    return this.canBeNull;
  }

  // TODO support TypeQualifierNickname
  public static Optional<Nullness> from(@Nullable String descriptor) {
    if (descriptor == null) {
      return Optional.empty();
    }

    switch (descriptor) {
      case "Lorg/jspecify/nullness/NullMarked;":
      case "org/jspecify/nullness/NullMarked":
      case "org.jspecify.nullness.NullMarked":
        return Optional.of(NOT_NULL);
      case "Lorg/jspecify/nullness/NullnessUnspecified;":
      case "org/jspecify/nullness/NullnessUnspecified":
      case "org.jspecify.nullness.NullnessUnspecified":
        return Optional.of(UNKNOWN);
      case "Lorg/jspecify/nullness/Nullable;":
      case "org/jspecify/nullness/Nullable":
      case "org.jspecify.nullness.Nullable":
        return Optional.of(NULLABLE);
      default:
        return Optional.empty();
    }
  }
}
