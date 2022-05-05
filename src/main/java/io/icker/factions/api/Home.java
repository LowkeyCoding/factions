package io.icker.factions.api;

import io.icker.factions.database.Column;
import io.icker.factions.database.Table;

@Table("Home")
public class Home {
    @Column("HomeName")
    private String name;

    @Column("FactionName")
    private String faction;

    @Column("x")
    private double x;

    @Column("y")
    private double y;

    @Column("z")
    private double z;

    @Column("Yaw")
    private float yaw;

    @Column("Pitch")
    private float pitch;

    @Column("Dimension")
    private String dimension; 
}
