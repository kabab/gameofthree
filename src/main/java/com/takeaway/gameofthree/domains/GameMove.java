package com.takeaway.gameofthree.domains;

import java.util.HashMap;

public class GameMove {
    public enum Moves {
        MOVE_ZERO,
        MOVE_ADD,
        MOVE_SUB,
        MOVE_STOP
    }

    private static HashMap<String, Moves> litMap = new HashMap<String, Moves>() {{
        put("MOVE_ZERO", Moves.MOVE_ZERO);
        put("MOVE_ADD", Moves.MOVE_ADD);
        put("MOVE_SUB", Moves.MOVE_SUB);
        put("MOVE_STOP", Moves.MOVE_STOP);
    }};

    static public Moves fromString(String game) {
        return litMap.get(game);
    }
}
