package com.sharkev.client.module;

public enum Category {
    MOVEMENT("Movement"),
    COMBAT("Combat"),
    VISUAL("Visual"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
