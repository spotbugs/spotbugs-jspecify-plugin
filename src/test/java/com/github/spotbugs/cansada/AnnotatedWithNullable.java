package com.github.spotbugs.cansada;

import codeanalysis.experimental.annotations.Nullable;

class AnnotatedWithNullable {
    @Nullable
    Object field;

    @Nullable
    Object method() {
        return null;
    }

    void method(@Nullable Object param) {
    }
}
