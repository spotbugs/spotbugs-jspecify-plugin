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

import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.DescriptorFactory;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.analysis.AnnotationValue;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.internalAnnotations.SlashedClassName;
import edu.umd.cs.findbugs.util.ClassName;

import java.util.Optional;

class NullnessDatabase {
  Optional<Nullness> findNullnessOf(XClass clazz, XMethod method, IAnalysisCache cache) {
    // TODO cache
    if (!method.isReturnTypeReferenceType()) {
      return Optional.empty();
    }

    return findNullnessOfMethod(method)
        .or(() -> findDefaultNullnessOfClass(clazz))
        .or(() -> findDefaultNullnessOfPackage(clazz.getClassDescriptor().getPackageName(), cache));
  }

  private Optional<Nullness> findNullnessOfMethod(XMethod method) {
    return method.getAnnotationDescriptors().stream()
        .map(desc -> Nullness.from(desc.getClassName()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  // TODO check interface and super classes
  private Optional<Nullness> findDefaultNullnessOfClass(XClass clazz) {
    AnnotationValue annotation =
        clazz.getAnnotation(
            DescriptorFactory.createClassDescriptor("org/jspecify/annotations/DefaultNonNull"));
    if (annotation != null) {
      return Optional.of(Nullness.NOT_NULL);
    } else {
      return Optional.empty();
    }
  }

  private Optional<Nullness> findDefaultNullnessOfPackage(
      @DottedClassName String packageName, IAnalysisCache cache) {
    @SlashedClassName
    String packageInfoClassName = ClassName.toSlashedClassName(packageName) + "/package-info";
    try {
      XClass clazz =
          cache.getClassAnalysis(
              XClass.class, DescriptorFactory.createClassDescriptor(packageInfoClassName));
      if (clazz == null) {
        return Optional.empty();
      }
      return findDefaultNullnessOfClass(clazz);
    } catch (CheckedAnalysisException e) {
      throw new RuntimeException(e);
    }
  }
}
