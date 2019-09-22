package com.takeaway.gameofthree.controllers;

import com.takeaway.gameofthree.GameMoveHelper;
import com.takeaway.gameofthree.domains.Game;
import com.takeaway.gameofthree.enumerations.GameMove;
import com.takeaway.gameofthree.exceptions.GameNotFoundException;
import com.takeaway.gameofthree.services.GameService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("games")
@Log4j2
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/{gameId}")
    public ResponseEntity join(
            @PathVariable("gameId") String gameId,
            @RequestParam("playerId") String playerId) throws Exception {
        return ResponseEntity.ok(gameService.join(gameId, playerId));
    }

    @PostMapping(value = "/", consumes = {"application/json"})
    public ResponseEntity create() throws Exception {
        return ResponseEntity.ok(gameService.join(null, null));
    }

    @MessageMapping("/move/{gameId}")
    public String move(@DestinationVariable String gameId, String message) {
        log.info("move for GameId {}, message {} ", gameId, message);
        try {
            String[] split = message.split("\\|");
            if (split.length != 2) return "ERROR";
            GameMove move = GameMoveHelper.fromString(split[0]);
            gameService.move(gameId, move, split[1]);
            log.info("Move accepted {}, playerId {}", move, split[1]);
            return "OK";
        } catch (GameNotFoundException e) {
            log.error("Game not found exception");
            return "ERROR_NOT_FOUND";
        }
    }

    @MessageMapping("/init/{gameId}")
    public String init(@DestinationVariable String gameId, String message) {
        log.info("init for GameId {}, message {} ", gameId, message);
        try {
            String[] split = message.split("\\|");
            if (split.length != 2) return "ERROR";

            gameService.init(gameId, Integer.parseInt(split[0]), split[1]);
            return "OK";
        } catch (GameNotFoundException e) {
            log.error("Game not found exception");
            return "ERROR_NOT_FOUND";
        }
    }


    @SendTo("/topic/game/{gameId}")
    public Game gameTopic(Game game) throws Exception {
        return game;
    }
}
