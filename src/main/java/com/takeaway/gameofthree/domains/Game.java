package com.takeaway.gameofthree.domains;

import com.takeaway.gameofthree.enumerations.GameMove;
import com.takeaway.gameofthree.enumerations.GameState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Game {
    @Id
    private String id;
    private Integer count;
    private String playerOne;
    private String playerTwo;
    private Integer playerTurn;
    private GameState state = GameState.GAME_CREATED;

    private List<GameChange> gameChanges;

    @CreatedDate
    private LocalDate createdDate;

    public boolean isOver() {
        return state.equals(GameState.GAME_OVER);
    }


    public boolean isPlayerTurn(String playerId) {
        return (playerId.equals(playerOne) && playerTurn == 1) ||
                (playerId.equals(playerTwo) && playerTurn == 2);
    }


    public void over() {
        state = GameState.GAME_OVER;
    }

    public void nextPlayer() {
        playerTurn = 1 + (playerTurn % 2);
    }

    public Integer getPlayerNum (String playerId) {
        if (playerId.equals(playerOne)) return 1;
        if (playerId.equals(playerTwo)) return 2;
        return -1;
    }

    public void addChange(GameMove move, Integer count, Integer turn) {
        if (gameChanges == null)
            gameChanges = new ArrayList<>();
        GameChange change = new GameChange();
        change.setMove(move);
        change.setNewCount(count);
        change.setPlayer(turn);
        gameChanges.add(change);
    }
}
