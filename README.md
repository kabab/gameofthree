# Game Of Three

This is a two player game, the first player incepts a random number, that will be divide by three and sent to the other player,
the player can add or subtract one, or do nothing, the new number is also divided by three and sent to the other player,
the winner is the player who will do an operation that results to **1**.
The game can be played by to many players, each two player will join a single game.

## Running locally
You can start the game on your terminal, if you have this requirements:
- java > 8
- mongodb database
- maven

to start the game on the terminal point to the root of the repo and run this command:

```
$ mvn spring-boot:run
```

Or if you have docker with docker-compose, on the root of the repo run this command

```
$ docker-compose up -d
```

You can access the game from your browser on `http://localhost:8080`,
Use different browser sessions for different players.

## Technical description

The game is build using `spring-boot`, the game persists the state on `mongodb` database,
To join, rejoin or create a game POST request is sent to the API,
and for event communication is done with WebSocket and STOMP protocol.
The front is build using vanillajs, with STOMP client library, and a simple watcher library.

## TODO

- Write tests
- Use a message broker system (like rabbitmq or kafka) for queueing STOMP message, in order to easily scale the app.
- Enhance the front, by using a frontend framework or library (like vuejs or react).
- Enhance the user system to keep track of the score
- Secure the communication channels (REST and WebSocket)


## Demo

![Alt Text](https://media.giphy.com/media/U7n9gOOayAppCa3pgI/giphy.gif)
