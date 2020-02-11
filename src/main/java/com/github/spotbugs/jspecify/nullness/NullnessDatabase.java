/*
 * Copyright (c) 2019-present The SpotBugs team.
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

import edu.umd.cs.findbugs.ba.XMethod;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class NullnessDatabase {
  Optional<Nullness> findNullnessOf(XMethod method) {
    // TODO cache
    if (!method.isReturnTypeReferenceType()) {
      return Optional.empty();
    }

    List<Nullness> nullnesses =
        method.getAnnotationDescriptors().stream()
            .map(desc -> Nullness.from(desc.getClassName()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    final Nullness nullness;
    if (nullnesses.isEmpty()) {
      nullness = Nullness.UNKNOWN;
    } else if (nullnesses.size() == 1) {
      nullness = nullnesses.get(0);
    } else {
      throw new RuntimeException("Found multiple nullness annotations on methods");
    }
    return Optional.of(nullness);
  }
}
