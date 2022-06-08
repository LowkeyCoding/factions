package io.icker.factions.api.events;

import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClaimEvents {
    public static final Event<Add> ADD = EventFactory.createArrayBacked(Add.class, callbacks -> (claim) -> {
        for (Add callback : callbacks) {
            callback.onAdd(claim);
        }
    });

    public static final Event<Remove> REMOVE = EventFactory.createArrayBacked(Remove.class, callbacks -> (x, y, z, level, faction) -> {
        for (Remove callback : callbacks) {
            callback.onRemove(x, y, z, level, faction);
        }
    });

    @FunctionalInterface
    public interface Add {
        void onAdd(Claim claim);
    }

    @FunctionalInterface
    public interface Remove {
        void onRemove(int x, int y, int z, String level, Faction faction);
    }
}
