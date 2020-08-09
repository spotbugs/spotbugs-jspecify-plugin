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
class MultiBoundTypeVariableUnspecToObjectUnionNull {
  <T extends Object & Lib> @Nullable Object x0(@NullnessUnspecified T x) {
    return x;
  }

  <T extends Object & @NullnessUnspecified Lib> @Nullable Object x1(@NullnessUnspecified T x) {
    return x;
  }

  <T extends Object & @Nullable Lib> @Nullable Object x2(@NullnessUnspecified T x) {
    return x;
  }

  <T extends @NullnessUnspecified Object & Lib> @Nullable Object x3(@NullnessUnspecified T x) {
    return x;
  }

  <T extends @NullnessUnspecified Object & @NullnessUnspecified Lib> @Nullable Object x4(
      @NullnessUnspecified T x) {
    return x;
  }

  <T extends @NullnessUnspecified Object & @Nullable Lib> @Nullable Object x5(
      @NullnessUnspecified T x) {
    return x;
  }

  <T extends @Nullable Object & Lib> @Nullable Object x6(@NullnessUnspecified T x) {
    return x;
  }

  <T extends @Nullable Object & @NullnessUnspecified Lib> @Nullable Object x7(
      @NullnessUnspecified T x) {
    return x;
  }

  <T extends @Nullable Object & @Nullable Lib> @Nullable Object x8(@NullnessUnspecified T x) {
    return x;
  }

  interface Lib {}
}
