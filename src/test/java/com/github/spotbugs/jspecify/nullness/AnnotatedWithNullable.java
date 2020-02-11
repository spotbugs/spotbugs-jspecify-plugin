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

import codeanalysis.experimental.annotations.NotNull;

class AnnotatedWithNullable {}

class AnnotatedWithNotNull {
  @NotNull
  Object method() {
    return null;
  }

  @NotNull
  Object needMerge() {
    Object result;
    if (System.currentTimeMillis() % 2 == 0) {
      result = null;
    } else {
      result = "not null";
    }
    return result;
  }
}
