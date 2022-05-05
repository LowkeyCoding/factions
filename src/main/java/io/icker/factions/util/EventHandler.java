package io.icker.factions.util;

import java.util.List;
import java.util.function.Function;

import io.icker.factions.api.Faction;

public class EventHandler {
    public static enum Result {
        FAIL,
        SUCCESS,
        PASS;
    }

    public static enum Event {
        FACTION_ADD {
            public void on(Function<Faction, EventHandler.Result> func) {
                listeners.add(func);
            }
        },
        FACTION_REMOVE;

        private static List<Function<Class<?>, Result>> listeners;
    }

    // This is basically all shit and needs to be redone

    public EventHandler() {}

    //private List<Function<Class<?>[], Result>> listeners;

    public void on(Event event, Function<Class<?>[], Result> listener) {
        listeners.add(listener);
    }

    public void call(Class<?> ...params) {
        for (Function<Class<?>[], Result> listener : listeners) {
            listener.apply(params);
        }
        //Method
    }
}

/*
FactionsMod.Events.ON_CLAIM.addListener(claim -> print(claim))

public interface Events {
    static ON_CLAIM = Event.create()
}

*/