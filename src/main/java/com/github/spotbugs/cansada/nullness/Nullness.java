package com.github.spotbugs.cansada.nullness;

import codeanalysis.experimental.annotations.Nullable;

enum Nullness {
    UNKNOWN(true), NULLABLE(true), NOT_NULL(false);

    private final boolean canBeNull;

    Nullness(boolean canBeNull) {
        this.canBeNull = canBeNull;
    }

    boolean canBeNull() {
        return this.canBeNull;
    }

    @Nullable
    public static Nullness from(@Nullable String descriptor) {
        if (descriptor == null) {
            return null;
        }

        switch (descriptor) {
        case "Lcodeanalysis/experimental/annotations/NullnessUnknown;":
        case "codeanalysis/experimental/annotations/NullnessUnknown":
        case "codeanalysis.experimental.annotations.NullnessUnknown":
            return UNKNOWN;
        case "Lcodeanalysis/experimental/annotations/Nullable;":
        case "codeanalysis/experimental/annotations/Nullable":
        case "codeanalysis.experimental.annotations.Nullable":
            return NULLABLE;
        case "Lcodeanalysis/experimental/annotations/NotNull;":
        case "codeanalysis/experimental/annotations/NotNull":
        case "codeanalysis.experimental.annotations.NotNull":
            return NOT_NULL;
        default:
            return null;
        }
    }
}
