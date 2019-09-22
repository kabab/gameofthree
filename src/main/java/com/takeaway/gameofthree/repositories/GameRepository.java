package com.takeaway.gameofthree.repositories;

import com.takeaway.gameofthree.domains.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
    public List<Game> findAllByPlayerTwoIsNull();
}
