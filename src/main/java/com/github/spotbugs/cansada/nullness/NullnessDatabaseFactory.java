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
package com.github.spotbugs.cansada.nullness;

import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IDatabaseFactory;

class NullnessDatabaseFactory implements IDatabaseFactory<NullnessDatabase> {

  @Override
  public NullnessDatabase createDatabase() throws CheckedAnalysisException {
    return new NullnessDatabase();
  }

  @Override
  public void registerWith(IAnalysisCache analysisCache) {
    throw new AssertionError("Should be unachievable");
  }
}
