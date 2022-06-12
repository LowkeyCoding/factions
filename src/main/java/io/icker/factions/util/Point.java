package io.icker.factions.util;

import io.icker.factions.database.Field;
import io.icker.factions.database.Name;

@Name("Point")
public class Point {
    @Field("X")
    private int x;
    @Field("Y")
    private int y;

    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    public Point(){}

    public int getX() { return x; }

    public int getY() { return y; }

    public void setX(int x) { this.x = x; }

    public void setY(int y) { this.y = y; }

    public void setLocation(int x, int y) { this.x = x; this.y = y;}
}
