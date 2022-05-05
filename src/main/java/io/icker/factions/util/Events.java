package io.icker.factions.util;

import java.util.List;
import java.util.function.Function;

import io.icker.factions.api.Faction;

public enum Events {
    FACTION_ADD {
        public List<Function<Class<?>, EventHandler.Result>> listeners;


        public void on(Function<Faction, EventHandler.Result> func) {
            listeners.add(func);
        }
    },
    FACTION_REMOVE;
}

/*
enum NodeTypes { NUMBER, OPERATOR };

new Node('+',
    new Node('*',
        new Node(3),
        new Node('/',
            new Node('+',
                new Node(7),
                new Node(1)
            ),
            new Node(4)
        )
    ),
    new Node('-',
        new Node(17),
        new Node(5)
    )
)

public double calculateNode(Node parent) {
    if (parent.type == NodeTypes.NUMBER) {
        return parent.number;
    }

    double left = calculateNode(parent.left);
    double right = calculateNode(parent.right);

    switch(parent.operator) {
        case '+':
            return left + right;
        case '-':
            return left - right;
        case '*':
            return left * right;
        case '/':
            return left / right;
        default:
            return 0;
      }
}
*/