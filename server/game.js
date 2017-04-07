var io = require('./app.js').io;

var gConfig = require('./gameConfig.js');

var Room = {
  new: function() {
    var room = {};
    room.id = Math.random().toString(36).substr(2);
    room.owner = '';
    room.players = Array(4).fill({
      empty: true
    });
    room.size = function() {
      return room.players.filter(item => item.empty != true).length;
    }

    room.setReady = function(user_id, ready) {
      var i = room.players.findIndex(item => item.user_id == user_id);
      room.players[i].ready = ready;
      return true;
    }

    room.getPlayers = function() {
      return room.players.filter(item => !item.empty);
    }

    room.gaming = false;
    room.nowTurn = -1; // 当前是谁的回合
    // 已经加载完的人数
    room.loadCount = function() {
      return room.players.filter(item => item.empty != true && item.gaming).length;
    }

    return room;
  }
}

// 房间池
var rooms_pool = {};

// 用来记录socket在哪个房间里
// 因为页面刷新时，socket.rooms会被清空
// 就无法退出了
var socket_in_room = {};

const CHESS_STATUS = {
  NOT_READY: 0,
  READY: 1,
  FLYING: 2,
  ARRIVED: 3,
}

// 把一个玩家塞到房间里
// 已经判断了可行性（有空位）
var insertPlayer = function(user_id, username, room_id, isHost) {
  var empty_index = rooms_pool[room_id].players.findIndex(item => item.empty === true);
  rooms_pool[room_id].players[empty_index] = {
    user_id: user_id,
    username: username,
    host: isHost,
    ready: isHost,
    chessman: Array(4).fill({
      status: CHESS_STATUS.NOT_READY,
      position: -1, // 知道not ready后，通过棋子下标判断在哪个停机坪
    }),
    gaming: false,
  }

  return true;
}


// 玩家创建房间
var createRoom = function(user_id, username) {
  do {
    var room = Room.new();
  } while (!!rooms_pool[room.id]);
  room.owner = username;

  console.log('Creater room: ' + room.id);
  rooms_pool[room.id] = room;

  var res = insertPlayer(user_id, username, room.id, isHost = true);

  socket_in_room[user_id] = room.id;

  // 返回给上个函数，给socket加入room
  return room.id;
}


// 有玩家加入房间
var joinRoom = function(user_id, username, room_id) {
  insertPlayer(user_id, username, room_id, isHost = false);

  socket_in_room[user_id] = room_id;
}


// 玩家离开房间
var leaveRoom = function(user_id, room_id) {
  var old = rooms_pool[room_id];
  // 防止重启清理内存
  if (old) {
    // rooms_pool[room_id].players = old.players.filter(item => item.user_id != user_id);
    var p_index = old.players.findIndex(item => item.user_id === user_id);
    var isHost = old.players[p_index].host; // 记录一下是不是房主退出

    old.players[p_index] = {
      empty: true
    };
    if (rooms_pool[room_id].size() == 0) {
      delete rooms_pool[room_id];
    } else if (isHost) {
      var new_host = old.players.findIndex(item => !item.empty);
      old.players[new_host].host = true;
      old.players[new_host].ready = true;
    }
    delete socket_in_room[user_id];
    return true;
  } else {
    return false;
  }
}


// 玩家开始游戏
var start_game = function(room_id) {
  if (!rooms_pool[room_id]) {
    console.log('房间不存在: ' + room_id);
    return false;
  }
  // 判断是否可以开始
  var players = rooms_pool[room_id].getPlayers();
  for (var i = 0; i < players.length; i++) {
    // 有玩家没有准备
    if (!players[i].empty && !players[i].ready) {
      return false;
    }
  }

  // 否则就启动游戏
  rooms_pool[room_id].gaming = true;

  return true;
}


// 玩家加载完了游戏界面
var set_gaming = function(user_id, room_id) {
  var p = rooms_pool[room_id].players.findIndex(item => item.user_id == user_id);
  rooms_pool[room_id].players[p].gaming = true;
  if(rooms_pool[room_id].loadCount() == rooms_pool[room_id].size()) {
    // 大家都加载完了
    rooms_pool[room_id].nowTurn = rooms_pool[room_id].players.findIndex(item => !item.empty);
    return rooms_pool[room_id].nowTurn;
  } else {
    // 还有没加载完
    return -1;
  }
}


// 获取当前游戏的回合是谁
var now_turn = function(room_id) {
  return rooms_pool[room_id].nowTurn;
}


// 通过下标获取玩家id
var turn_to_user_id = function(room_id, turn) {
  return rooms_pool[room_id].players[turn].user_id;
}


// 获得一个掷骰子结果，目前是完全随机
var random_dice = function(room_id) {
  var dice = Math.floor(Math.random() * 6);
  rooms_pool[room_id].dice = dice;
  return dice;
}

module.exports = {
  rooms_pool: rooms_pool,
  createRoom: createRoom,
  joinRoom: joinRoom,
  leaveRoom: leaveRoom,

  socket_in_room: socket_in_room,
  start_game: start_game,
  random_dice: random_dice,
  set_gaming: set_gaming,
  turn_to_user_id: turn_to_user_id,
  now_turn: now_turn,
}