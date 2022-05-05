package io.icker.factions.util;

import io.icker.factions.api.Faction;
import io.icker.factions.api.Player;

public class ExampleEvent {

    public interface FUNC {
        boolean run();
    }

    public ExampleEvent() {
        Event e = new Event(FUNC::run);
        e.register((player) -> {
            return Event.Result.PASS;
        });

    }

}
