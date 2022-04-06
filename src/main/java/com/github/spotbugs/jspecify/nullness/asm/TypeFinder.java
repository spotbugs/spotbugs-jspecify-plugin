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
import java.util.function.Consumer;
import org.jspecify.nullness.NullMarked;
import org.jspecify.nullness.Nullable;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * A helper class that find type in signature of returned type, parameter type, etc. It is designed
 * to use as returned value of methods in {@link SignatureVisitor}.
 */
@NullMarked
final class TypeFinder extends SignatureVisitor {
  private final Consumer<@Nullable TypeArgument> callback;

  TypeFinder(int api, Consumer<@Nullable TypeArgument> callback) {
    super(api);
    this.callback = Objects.requireNonNull(callback);
  }

  @Override
  public void visitBaseType(char descriptor) {
    callback.accept(new TypeArgumentWithType('?', Character.toString(descriptor)));
  }

  @Override
  public void visitInnerClassType(String name) {
    String innerName = "L" + name + ";";
    callback.accept(new TypeArgumentWithType('?', innerName));
  }

  @Override
  public void visitClassType(String name) {
    String innerName = "L" + name + ";";
    callback.accept(new TypeArgumentWithType('?', innerName));
  }

  @Override
  public void visitTypeVariable(String name) {
    callback.accept(new TypeArgumentWithTypeVariable('?', name));
  }

  @Override
  public SignatureVisitor visitArrayType() {
    return new TypeFinder(api, type -> callback.accept(type.replaceType(t -> "[" + t)));
  }

  @Override
  public SignatureVisitor visitTypeArgument(char wildcard) {
    return new TypeFinder(
        api, typeArgument -> callback.accept(typeArgument.replaceWildcard(wildcard)));
  }

  @Override
  public void visitTypeArgument() {
    // TODO treat type argument with a wildcard in a special way?
    callback.accept(new TypeArgumentWithType('*', "Ljava/lang/Object;"));
  }
}
