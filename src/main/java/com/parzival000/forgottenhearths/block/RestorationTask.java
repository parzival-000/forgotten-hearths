package com.parzival000.forgottenhearths.block;

import net.minecraft.util.StringRepresentable;

public enum RestorationTask implements StringRepresentable {
    KETTLE(1, "kettle"),
    CROCK(2, "crock"),
    QUILT(4, "quilt");

    public static final int ALL_MASK = KETTLE.mask | CROCK.mask | QUILT.mask;

    private final int mask;
    private final String serializedName;

    RestorationTask(int mask, String serializedName) {
        this.mask = mask;
        this.serializedName = serializedName;
    }

    public int mask() {
        return this.mask;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
