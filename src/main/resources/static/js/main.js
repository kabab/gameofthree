
var API = "http://localhost:8080/"

function initButtons() {
    var plusButton = document.getElementById("plus");
    var minusButton = document.getElementById("minus");
    var zeroButton = document.getElementById("zero");
    var initButton = document.getElementById("initButton");

    plusButton.disabled = false;
    minusButton.disabled = false;
    zeroButton.disabled = false;

    plusButton.onclick = () => sendEvent('MOVE_ADD');
    minusButton.onclick = () => sendEvent('MOVE_SUB');
    zeroButton.onclick = () => sendEvent('MOVE_ZERO');
    initButton.onclick = () => {
        var value = document.getElementById("inputValue").value;
        initGame(value);
    }
}



function registerGame(gameId, playerId) {
    window.localStorage.setItem("gameId", gameId);
    window.localStorage.setItem("playerId", playerId);
}


function getGameId() {
    var gameid = window.localStorage.getItem("gameId");
    return gameid;
}

function getPlayerId() {
    var playerId = window.localStorage.getItem("playerId");
    return playerId;
}

function remoteCreateOrJoinGame(gameId, playerId, cb) {
    var xhr = new XMLHttpRequest();   // new HttpRequest instance
    xhr.addEventListener("load", function() {
        var game = JSON.parse(this.responseText);
        cb(game);
    });

    var url =  API + "games/";
    console.log(gameId, typeof playerId);
    if (gameId && playerId)
        url += gameId + "?playerId=" + playerId;

    xhr.open("POST", url);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xhr.send();
}

const GAME_STATES = {
    GAME_INIT: "GAME_INIT",
    GAME_CREATED: "GAME_CREATED",
    GAME_STARTED: "GAME_STARTED",
    GAME_OVER: "GAME_OVER",
}

var stompClient = null;
var autoTimeout = null;

const Game = {
    autoPlay: true,
    state: null,
    playerId: null,
    turn: null,
    playerNumber: null,
    id: null,
    count: null,
    connected: false,
    subscribed: false
};

watch(Game,["connected", "subscribed", "count", "state", "autoPlay", "turn", "playerNumber"], function(){
    if (Game.subscribed) {
        initButtons();
    }

    if (Game.autoPlay) {
        document.getElementById("man-auto").innerText = "MAN";
    } else {
        document.getElementById("man-auto").innerText = "AUTO";
    }

    if (Game.count == 1) {
        document.getElementById("control").style.display = "none"
        document.getElementById("status").innerText = "Game Over";
        document.getElementById("value").innerText = Game.count;
        document.getElementById("new_game").style.display = "inline-block";
        if (Game.turn == Game.playerNumber) {
            document.getElementById("winner_board").innerText = "You Win";
            document.getElementById("winner_board").classList.add("win");
        } else {
            document.getElementById("winner_board").innerText = "You Lose";
            document.getElementById("winner_board").classList.add("lose");
        }
        document.getElementById("winner_board").style.display = "block";
        return
    }

    if (Game.turn ==  Game.playerNumber) {
        document.getElementById("status").innerText = "Your turn";
        if (Game.count && Game.count > 1 && Game.autoPlay) {
            const events = ["MOVE_ADD", "MOVE_SUB", "MOVE_ZERO"]
            clearTimeout(autoTimeout);
            autoTimeout = setTimeout(sendEvent, 1000, events[Math.floor(Math.random() * 3)]);
        }
    } else {
        document.getElementById("status").innerText = "Waiting for the other player";
    }

    if (Game.count || Game.turn != Game.playerNumber) {
        document.getElementById("init").style.display = "none";
        document.getElementById("value").innerText = Game.count;
        document.getElementById("control").style.display = "block";
    } else {
        document.getElementById("control").style.display = "none";
        document.getElementById("init").style.display = "block";
    }

});

document.getElementById("man-auto").onclick = function() {
    Game.autoPlay = !Game.autoPlay;
}

function sendEvent(event) {
    console.log('send =>', event);
    stompClient.send("/app/move/" + Game.id , {}, event + "|" + Game.playerId);
}

function initGame(value) {
    console.log('send init =>', value);
    stompClient.send("/app/init/" + Game.id , {}, value + "|" + Game.playerId);
}


function subscribeToGame(gameId) {
    stompClient.subscribe('/topic/game/' + gameId, function(data) {
        var game = JSON.parse(data.body);
        Game.count = game.count;
        Game.turn = game.playerTurn;
    });
    console.log('subscribed');
    Game.subscribed = true;
}

function createOrJoinGame() {
    var gameId = getGameId();
    var playerId = getPlayerId();

    remoteCreateOrJoinGame(gameId, playerId, function(game) {
        console.log(game);
        subscribeToGame(game.id);
        Game.id = game.id;
        Game.playerId = game.playerOne || game.playerTwo;
        Game.state = game.state;
        Game.count = game.count;
        Game.turn = game.playerTurn;
        Game.playerNumber = game.playerOne ? 1 : 2;
        registerGame(Game.id, Game.playerId);
        console.log(Game);
    });
}

function init() {
    Game.state = GAME_STATES.GAME_INIT;
    var socket = new SockJS('http://localhost:8080/mywebsockets');
    stompClient = StompJs.Stomp.over(socket);
    stompClient.debug = () => {};
    stompClient.connect({}, function (frame) {
        Game.connected = true;
        createOrJoinGame();
    });
}

init();
