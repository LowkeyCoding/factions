package io.icker.factions.config;

import java.util.List;

public class Zone {
    private Type type;
    private String message;
    private Constraint x;
    private Constraint z;
    private List<String> includedDimensions;
    private List<String> excludedDimensions;

    public static enum Type {
        DEFAULT,
        WILDERNESS,
        ADMIN;
    }

    public Zone(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public void setConstraints (Constraint x, Constraint z) {
        this.x = x;
        this.z = z;
    }

    public void setDimensions(List<String> included, List<String> excluded) {
        includedDimensions = included;
        excludedDimensions = excluded;
    }

    public boolean isApplicable(String dimension, int xPos, int zPos) {
        return matchDimension(dimension) && x.validate(xPos) && z.validate(zPos);
    }

    public boolean matchDimension(String dimension) {
        boolean included = includedDimensions.contains("*") || includedDimensions.contains(dimension);
        return excludedDimensions.contains(dimension) ? false : included;
    }

    public String getFailMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }
}
