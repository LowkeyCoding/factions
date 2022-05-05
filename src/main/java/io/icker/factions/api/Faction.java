package io.icker.factions.api;

import java.util.List;

import io.icker.factions.database.Column;
import io.icker.factions.database.Query;
import io.icker.factions.database.Table;

@Table("Faction")
public class Faction {
    @Column("FactionName")
    private String name;

    @Column("Description")
    private String description;

    @Column("Color")
    private String color; // TODO

    @Column("Open")
    private boolean open;

    @Column("Power")
    private int power;

    public static Faction get(String name) {
        return new Query("SELECT * FROM Faction WHERE FactionName = ?;", name).first(Faction.class);
    }

    public static List<Faction> all() {
        return new Query("SELECT * FROM Faction;").get(Faction.class);
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public boolean isOpen() {
        return open;
    }

    public int getPower() {
        return power;
    }

    public void setDescription(String description) {
        new Query("UPDATE Faction SET Description = ? WHERE FactionName = ?;", description, name)
            .update();
    }

    public void setColor(String color) {
        new Query("UPDATE Faction SET Color = ? WHERE FactionName = ?;", color, name)
            .update();
    }

    public void setOpen(boolean open) {
        new Query("UPDATE Faction SET Open = ? WHERE FactionName = ?;", open, name)
            .update();
    }

    public void setPower(int power) {
        new Query("UPDATE Faction SET Power = ? WHERE FactionName = ?;", power, name)
            .update();
    }
}
