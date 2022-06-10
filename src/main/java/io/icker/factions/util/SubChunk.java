package io.icker.factions.util;

public class SubChunk {
    private int x;
    private int y;
    private int z;

    private String level;

    public SubChunk(int x, int y, int z, String level){
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    public String getLevel() { return this.level; }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setLevel(String level) { this.level = level; }
}
