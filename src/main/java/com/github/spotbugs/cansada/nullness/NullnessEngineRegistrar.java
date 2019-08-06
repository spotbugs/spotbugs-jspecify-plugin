package com.github.spotbugs.cansada.nullness;

import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IAnalysisEngineRegistrar;

class NullnessEngineRegistrar implements IAnalysisEngineRegistrar {

    @Override
    public void registerAnalysisEngines(IAnalysisCache analysisCache) {
        analysisCache.registerDatabaseFactory(NullnessDatabase.class, new NullnessDatabaseFactory());
    }

}
