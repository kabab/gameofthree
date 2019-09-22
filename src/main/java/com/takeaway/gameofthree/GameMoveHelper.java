package com.takeaway.gameofthree;

import com.takeaway.gameofthree.enumerations.GameMove;

import java.util.HashMap;

public class GameMoveHelper {
    private static HashMap<String, GameMove> litMap = new HashMap<String, GameMove>() {{
        put("MOVE_ZERO", GameMove.MOVE_ZERO);
        put("MOVE_ADD", GameMove.MOVE_ADD);
        put("MOVE_SUB", GameMove.MOVE_SUB);
    }};

    static public GameMove fromString(String game) {
        return litMap.get(game);
    }
}
