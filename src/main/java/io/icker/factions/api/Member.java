package io.icker.factions.api;

import java.util.UUID;

import io.icker.factions.database.Column;
import io.icker.factions.database.Query;
import io.icker.factions.database.Table;

@Table("Member")
public class Member {
    @Column("PlayerID")
    private UUID player;

    @Column("FactionName")
    private String faction;

    @Column("Role")
    private int role;

    @Column("Inviter")
    private UUID inviter;

    public static Member get(UUID player, String faction) {
        return new Query("SELECT * FROM Member WHERE PlayerID = ? AND FactionName = ?;", player, faction).first(Member.class);
    }

    public static Member get(UUID player) {
        return new Query("SELECT * FROM Member WHERE PlayerID = ? AND Role >= 1;", player).first(Member.class);
    }

    public Player getPlayer() {
        return Player.get(player);
    }

    public Faction getFaction() {
        return Faction.get(faction);
    }

    public boolean isMember() {
        return role >= 1;
    }

    public boolean isCommander() {
        return role >= 2;
    }

    public boolean isLeader() {
        return role == 3;
    }

    public Player getInviter() {
        return Player.get(inviter);
    }
}
