package com.takeaway.gameofthree.services;

import com.takeaway.gameofthree.domains.Game;
import com.takeaway.gameofthree.enumerations.GameMove;
import com.takeaway.gameofthree.exceptions.GameNotFoundException;
import com.takeaway.gameofthree.enumerations.GameState;
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

    /**
     * Rejoin, Join or create a new game
     * the method accept two parameters, if they both exist, it will check if the game exists
     * otherwise it will check if there's an available game to join
     * if not a new game is created and returned
     * @param gameId
     * @param playerId
     * @return
     */
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

    /**
     * The method will initialize a already created game
     * checking if the player has the right to initialize the game
     * it will change the player turn after initialization
     * @param gameId
     * @param value
     * @param playerId
     * @return
     * @throws GameNotFoundException
     */
    public Game init(String gameId, Integer value, String playerId) throws GameNotFoundException {
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (!gameOpt.isPresent())
            throw new GameNotFoundException();


        Game game = gameOpt.get();

        if (!game.getPlayerOne().equals(playerId))
            return game;

        game.setCount(value);
        game.addChange(GameMove.MOVE_INIT, value, 1);
        game.setPlayerTurn(2);
        gameRepository.save(game);
        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), game);
        return game;
    }

    /**
     *  This method will take a move, playerId and a gameId and decide the next state of the game
     *  it will check the turn of the player if it's not it will do nothing
     *  if the game is over it will do nothing
     *  if the move is accepted, the a new state of the game is created with new count and the flipping of the player turn
     *  if the move is accepted, the next state is broadcasted to the gameId topic
     * @param gameId
     * @param move
     * @param playerId
     * @return new state of the game
     * @throws GameNotFoundException
     * @
     */
    public Game move(String gameId, GameMove move, String playerId) throws GameNotFoundException {

        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (!gameOpt.isPresent())
            throw new GameNotFoundException();

        Game game = gameOpt.get();

        if (!game.isPlayerTurn(playerId) || game.isOver()) {
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
            game.over();
        } else {
            game.nextPlayer();
        }

        game.addChange(move, game.getCount(), game.getPlayerNum(playerId));

        gameRepository.save(game);
        game.setPlayerOne(null);
        game.setPlayerTwo(null);
        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), game);
        return game;
    }


}
