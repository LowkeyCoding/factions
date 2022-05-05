package io.icker.factions.api;

import java.util.UUID;

import io.icker.factions.database.Column;
import io.icker.factions.database.Query;
import io.icker.factions.database.Table;

@Table("Player")
public class Player {
    @Column("PlayerID")
    private UUID id;

    @Column("ChatMode")
    private int chat;

    @Column("Bypass")
    private boolean bypass;

    public static enum ChatMode {
        FOCUS,
        FACTION,
        GLOBAL;
    }

    public static Player get(UUID id) {
        return new Query("SELECT * FROM Player WHERE PlayerID = ?;", id).first(Player.class);
    }

    public UUID getId() {
        return id;
    }

    public Faction getFaction() {
        return new Query("""
        SELECT Faction.* FROM Faction
        INNER JOIN Member ON Faction.FactionName = Member.FactionName
        INNER JOIN Player ON Player.PlayerID = Member.PlayerID
        WHERE Player.PlayerID = ? AND Member.Role >= 1;
        """, id).first(Faction.class);
    }

    public ChatMode getChatMode() {
        return ChatMode.values()[chat];
    }

    public boolean shouldBypass() {
        return bypass;
    }
}