package com.parzival000.forgottenhearths.block;

import net.minecraft.util.StringRepresentable;

public enum HearthStage implements StringRepresentable {
    FORGOTTEN(0, "forgotten"),
    DISCOVERED(1, "discovered"),
    REKINDLED(2, "rekindled"),
    RESTORED(3, "restored");

    private final int id;
    private final String serializedName;

    HearthStage(int id, String serializedName) {
        this.id = id;
        this.serializedName = serializedName;
    }

    public int id() {
        return this.id;
    }

    public boolean isWarm() {
        return this.id >= REKINDLED.id;
    }

    public static HearthStage byId(int id) {
        return switch (id) {
            case 1 -> DISCOVERED;
            case 2 -> REKINDLED;
            case 3 -> RESTORED;
            default -> FORGOTTEN;
        };
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
