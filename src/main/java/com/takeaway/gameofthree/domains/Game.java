package com.takeaway.gameofthree.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

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

    @CreatedDate
    private LocalDate createdDate;
}
