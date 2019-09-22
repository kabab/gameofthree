package com.takeaway.gameofthree.domains;

import com.takeaway.gameofthree.enumerations.GameMove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameChange {
    @Id
    private String id;
    private GameMove move;
    private Integer player;
    private Integer newCount;
    @CreatedDate
    private LocalDate createdDate;
}
