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
