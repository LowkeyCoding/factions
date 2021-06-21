package io.icker.factions.database;

import java.util.ArrayList;
import java.util.UUID;

public class Invite {
    public UUID playerId;
    private String factionName;

    public static Invite get(UUID playerId, String factionName) {
        Query query = new Query("SELECT * FROM Invite WHERE player = ? AND faction = ?;")
            .set(playerId, factionName)
            .executeQuery();

        if (!query.success) return null;
        return new Invite(playerId, factionName);
    }

    public static ArrayList<Invite> get(UUID playerId) {
        Query query = new Query("SELECT faction FROM Invite WHERE player = ?;")
            .set(playerId)
            .executeQuery();

        if (!query.success) return null;
        ArrayList<Invite> invites = new ArrayList<Invite>();

        while (query.next()) {
            invites.add(new Invite(playerId, query.getString("faction")));
        }
        return invites;
    }

    public static ArrayList<Invite> get(String factionName) {
        Query query = new Query("SELECT player FROM Invite WHERE faction = ?;")
            .set(factionName)
            .executeQuery();
            
        if (!query.success) return null;
        ArrayList<Invite> invites = new ArrayList<Invite>();

        while (query.next()) {
            invites.add(new Invite((UUID) query.getObject("player"), factionName));
        }
        return invites;
    }

    public static Invite add(UUID playerId, String factionName) {
        Query query = new Query("INSERT INTO Invite VALUES (?, ?);")
            .set(playerId, factionName)
            .executeUpdate();

        if (!query.success) return null;
        return new Invite(playerId, factionName);
    }

    public Invite(UUID playerId, String factionName) {
        this.playerId = playerId;
        this.factionName = factionName;
    }

    public Faction getFaction() {
        return Faction.get(factionName);
    }

    public void remove() {
        new Query("DELETE FROM Invite WHERE player = ? AND faction = ?;")
            .set(playerId, factionName)
            .executeUpdate();
    }
}