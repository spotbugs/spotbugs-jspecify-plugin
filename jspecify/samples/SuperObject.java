/*
 * Copyright 2020 The jspecify Authors.
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

import org.jspecify.annotations.NullAware;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullnessUnspecified;

@NullAware
class SuperObject {
  void foo(
      Lib<? super Object> lib,
      Object t,
      @NullnessUnspecified Object tUnspec,
      @Nullable Object tUnionNull) {
    lib.useT(t);

    // NOT-ENOUGH-INFORMATION
    lib.useT(tUnspec);

    // MISMATCH
    lib.useT(tUnionNull);

    //

    lib.useTUnspec(t);

    // NOT-ENOUGH-INFORMATION
    lib.useTUnspec(tUnspec);

    // NOT-ENOUGH-INFORMATION
    lib.useTUnspec(tUnionNull);

    //

    lib.useTUnionNull(t);

    lib.useTUnionNull(tUnspec);

    lib.useTUnionNull(tUnionNull);
  }

  interface Lib<T extends @Nullable Object> {
    void useT(T t);

    void useTUnspec(@NullnessUnspecified T t);

    void useTUnionNull(@Nullable T t);
  }
}
