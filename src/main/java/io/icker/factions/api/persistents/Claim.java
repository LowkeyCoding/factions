package io.icker.factions.api.persistents;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.icker.factions.api.events.ClaimEvents;
import io.icker.factions.database.Database;
import io.icker.factions.database.Field;
import io.icker.factions.database.Name;

@Name("Claim")
public class Claim {
    private static final HashMap<String, Claim> STORE = Database.load(Claim.class, Claim::getKey);

    @Field("X")
    public int x;

    @Field("Y")
    public int y;

    @Field("Z")
    public int z;

    @Field("Level")
    public String level;

    @Field("FactionID")
    public UUID factionID;

    public Claim(int x, int y, int z, String level, UUID factionID) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
        this.factionID = factionID;
    }

    public Claim() { ; }

    public String getKey() {
        return String.format("%s-%d-%d-%d", level, x, y, z);
    }

    public static Claim get(int x, int y, int z, String level) {
        return STORE.get(String.format("%s-%d-%d-%d", level, x, y, z));
    }

    public static List<Claim> getByFaction(UUID factionID) {
        return STORE.values()
            .stream()
            .filter(c -> c.factionID.equals(factionID))
            .toList();
    }

    public static void add(Claim claim) {
        STORE.put(claim.getKey(), claim);
        ClaimEvents.ADD.invoker().onAdd(claim);
    }

    public Faction getFaction() {
        return Faction.get(factionID);
    }

    public void remove() {
        STORE.remove(getKey());
        ClaimEvents.REMOVE.invoker().onRemove(x, y, z, level, Faction.get(factionID));
    }

    public static void save() {
        Database.save(Claim.class, STORE.values().stream().toList());
    }
}
