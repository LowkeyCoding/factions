package io.icker.factions.api;

import io.icker.factions.database.Column;
import io.icker.factions.database.Table;

@Table("Claim")
public class Claim {
    @Column("FactionName")
    private String faction;

    @Column("x")
    private int x;

    @Column("y")
    private int y;

    @Column("Dimension")
    private String dimension;
}
