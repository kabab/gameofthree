package com.takeaway.gameofthree.services;

import com.takeaway.gameofthree.domains.Game;
import com.takeaway.gameofthree.domains.GameMove;
import com.takeaway.gameofthree.domains.GameNotFoundException;
import com.takeaway.gameofthree.domains.GameState;
import com.takeaway.gameofthree.repositories.GameRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Log4j2
@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Game join(String gameId, String playerId) {
        log.info("gameId {} playerId {}", gameId, playerId);
        // Rejoin an old started game
        if (gameId != null && playerId != null) {
            Optional<Game> gameOpt = gameRepository.findById(gameId);
            if (gameOpt.isPresent() && !gameOpt.get().getState().equals(GameState.GAME_OVER)) {
                log.info("rejoin a game, {}", gameOpt.get());
                Game game = gameOpt.get();
                if (game.getPlayerOne().equals(playerId)) {
                    game.setPlayerTwo(null);
                    return game;
                }

                if (game.getPlayerTwo().equals(playerId)) {
                    game.setPlayerOne(null);
                    return game;
                }
            }
        // New player Join a created game
        }

        List<Game> games = gameRepository.findAllByPlayerTwoIsNull();
        if (games.size() > 0) {
            log.info("Join already created game, game found {}", games.size());
            Game game = games.get(0);
            game.setPlayerTwo(UUID.randomUUID().toString());
            game.setState(GameState.GAME_STARTED);
            Game save = gameRepository.save(game);
            // Hide the player one ID, for a very basic security check
            save.setPlayerOne(null);
            return save;
        }

        // Create a new game

        Game game = new Game();
        game.setPlayerOne(UUID.randomUUID().toString());
        game.setPlayerTurn(1);
        return gameRepository.save(game);
    }


    public Game init(String gameId, Integer value, String playerId) throws GameNotFoundException {
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (!gameOpt.isPresent())
            throw new GameNotFoundException();


        Game game = gameOpt.get();

        if (!game.getPlayerOne().equals(playerId))
            return game;

        game.setCount(value);
        game.setPlayerTurn(2);
        gameRepository.save(game);
        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), game);
        return game;
    }


    public Game move(String gameId, GameMove.Moves move, String playerId) throws GameNotFoundException {

        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (!gameOpt.isPresent())
            throw new GameNotFoundException();

        Game game = gameOpt.get();

        if (game.getState().equals(GameState.GAME_OVER)) {
            log.info("Game Over");
            game.setPlayerOne(null);
            game.setPlayerTwo(null);
            return game;
        }

        if ( (playerId.equals(game.getPlayerOne()) && game.getPlayerTurn() == 2) ||
            (playerId.equals(game.getPlayerTwo()) && game.getPlayerTurn() == 1)) {
            log.info("Not player turn");
            game.setPlayerOne(null);
            game.setPlayerTwo(null);
            return game;
        }


        switch (move) {
            case MOVE_ADD:
                game.setCount(game.getCount() + 1);
                break;
            case MOVE_SUB:
                game.setCount(game.getCount() - 1);
                break;
        }

        if (game.getCount() % 3 == 0) {
            game.setCount(game.getCount() / 3);
        }

        if (game.getCount() == 1) {
            game.setState(GameState.GAME_OVER);
        }

        if (!game.getState().equals(GameState.GAME_OVER))
            game.setPlayerTurn(1 + ( game.getPlayerTurn() % 2) );
        gameRepository.save(game);
        game.setPlayerOne(null);
        game.setPlayerTwo(null);
        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), game);
        return game;
    }


}
