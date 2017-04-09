var io = require('./app.js').io;

var gConfig = require('./gameConfig.js');

var Room = {
  new: function() {
    var room = {};
    room.id = Math.random().toString(36).substr(2);
    room.owner = '';
    room.players = [{
      empty: true
    }, {
      empty: true
    }, {
      empty: true
    }, {
      empty: true
    }]; // 用fill()会有bug啊！
    room.size = function() {
      return room.players.filter(item => item.empty != true).length;
    }

    // 返回所有不为空的玩家
    room.getPlayers = function() {
      return room.players.filter(item => !item.empty);
    }

    room.gaming = false;
    room.nowTurn = -1; // 当前是谁的回合
    // 已经加载完的人数
    room.gamingCount = function() {
      return room.players.filter(item => item.empty != true && item.gaming).length;
    }

    room.waitingFor = 0; // 每个阶段要等待多少个人（播放动画等），用来同步

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
    gaming: false, // 开局后，如果它又变成false，就是离线了
    // ！不要问我为什么不用Array.fill()，被坑过才知道
    chessman: [{
      status: CHESS_STATUS.NOT_READY,
      position: -1, // 知道not ready后，通过棋子下标判断在哪个停机坪
    }, {
      status: CHESS_STATUS.NOT_READY,
      position: -1, // 知道not ready后，通过棋子下标判断在哪个停机坪
    }, {
      status: CHESS_STATUS.NOT_READY,
      position: -1, // 知道not ready后，通过棋子下标判断在哪个停机坪
    }, {
      status: CHESS_STATUS.NOT_READY,
      position: -1, // 知道not ready后，通过棋子下标判断在哪个停机坪
    }],
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
    var player = old.players[p_index];
    var isHost = player.host; // 记录一下是不是房主退出
    var username = player.username;

    if (player.gaming && old.nowTurn === p_index) {
      // 跳过他的回合
      var next_player = next_turn(room_id);
    }

    if (player.gaming && old.waitingFor > 0) {
      // TODO, 目前是不管三七二十一就当作已经完成当前回合
      old.waitingFor--;
    }

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
    return {
      username: username,
      next_player: next_player
    };
  } else {
    return false;
  }
}


var set_ready = function(room_id, user_id, ready) {
  var room = rooms_pool[room_id];
  var i = room.players.findIndex(item => item.user_id == user_id);
  room.players[i].ready = ready;
  return true;
}


// 房主点击 开始游戏
var start_game = function(room_id) {
  if (!rooms_pool[room_id]) {
    console.log('房间不存在: ' + room_id);
    return false;
  }
  // 判断是否可以开始
  var players = rooms_pool[room_id].getPlayers(); // 返回所有不为空的玩家
  for (var i = 0; i < players.length; i++) {
    // 有玩家没有准备
    if (!players[i].ready) {
      return false;
    }
  }

  // 否则就启动游戏
  rooms_pool[room_id].gaming = true;

  return true;
}


// 玩家加载完了游戏界面
var set_gaming = function(user_id, room_id) {
  var room = rooms_pool[room_id];
  // 找到这个说加载好了的玩家
  var p = room.players.findIndex(item => item.user_id == user_id);
  room.players[p].gaming = true; // 标记成游戏中
  if (room.gamingCount() === room.size()) {
    // 大家都加载完了
    room.nowTurn = room.players.findIndex(item => !item.empty);
    return room.nowTurn;
  } else {
    // 还有没加载完的玩家
    return -1;
  }
}


// 获取当前游戏的回合是谁
var now_turn = function(room_id) {
  return rooms_pool[room_id].nowTurn;
}


// 通过下标获取玩家id
var turn_to_user = function(room_id) {
  var turn = rooms_pool[room_id].nowTurn;
  return {
    user_id: rooms_pool[room_id].players[turn].user_id,
    username: rooms_pool[room_id].players[turn].username,
  };
}


// 获得一个掷骰子结果，目前是完全随机
var random_dice = function(room_id) {
  var room = rooms_pool[room_id];
  var dice = Math.ceil(Math.random() * 6);
  room.dice = dice;
  room.waitingFor = room.gamingCount();
  return dice;
}


// 获得当前房间可行的棋子，基于玩家序号和骰子点数（已经有了）
var get_available = function(room_id) {
  var room = rooms_pool[room_id];
  var player = room.players[room.nowTurn];
  var game_path = gConfig.player_path[room.nowTurn];

  var res = [];
  console.log("DICE: " + room.dice);
  for (var i = 0; i < player.chessman.length; i++) {
    if (player.chessman[i].status === CHESS_STATUS.READY ||
      player.chessman[i].status === CHESS_STATUS.FLYING) {
      res.push(i);
    } else if (player.chessman[i].status === CHESS_STATUS.ARRIVED) {
      continue;
    } else if (gConfig.ready_allow.indexOf(room.dice) !== -1) {
      res.push(i);
    }
  }
  return res;
}


// 获取一个棋子的移动路径
var get_movement_path = function(room_id, chess) {
  var room = rooms_pool[room_id];
  var dice = room.dice;
  var player = room.players[room.nowTurn];
  var game_path = gConfig.player_path[room.nowTurn];
  var chessman = player.chessman[chess];
  var status = chessman.status;
  var pos = chessman.position;

  // 最终要返回的路径
  var res_path = [];
  // 把原始路径给它塞进去先
  if (status === CHESS_STATUS.NOT_READY) {
    res_path.push('not-ready');
  } else if (status === CHESS_STATUS.READY) {
    res_path.push('ready');
  } else {
    res_path.push('pos-' + pos);
  }

  if (status === CHESS_STATUS.NOT_READY) {
    status = CHESS_STATUS.READY;
    res_path.push('ready');
  } else if (status === CHESS_STATUS.READY) {
    // 已经是准备好的飞机
    status = CHESS_STATUS.FLYING;
    dice--;
    pos = game_path[0];
    res_path.push('pos-' + pos);
  }

  while (status === CHESS_STATUS.FLYING && dice) {
    // 如果踩在外环终点上，就进入直线跑道
    if (pos === game_path[1]) {
      pos = game_path[2];
      res_path.push('pos-' + pos);
    } else if (pos >= game_path[3]) {
      // 在终点上了
      status = CHESS_STATUS.ARRIVED;
      res_path.push('arrived');
    } else if (pos >= game_path[2]) {
      // 如果在直线跑道上了，前移一个跑道
      pos += 4;
      res_path.push('pos-' + pos);
    } else {
      // 在普通外环上
      pos = parseInt((pos + 1) % gConfig.round_length);
      res_path.push('pos-' + pos);
    }
    dice--;
  }

  // 判断能不能同色跳
  if (status === CHESS_STATUS.FLYING &&
    pos % 4 === room.nowTurn &&
    pos < game_path[2] &&
    pos != game_path[1]) {
    // 先判断在不在加油站起点
    if (pos === (game_path[0] + 1 + 4 * 4) % gConfig.round_length) {
      // 在加油站起点
      pos = (pos + 4 * 3) % gConfig.round_length; // 飞过去
      res_path.push('pos-' + pos);
    }
    // 跳一下
    pos = (pos + 4) % gConfig.round_length;
    res_path.push('pos-' + pos);
    // 再判断在不在加油站起点
    if (pos === (game_path[0] + 1 + 4 * 4) % gConfig.round_length) {
      // 在加油站起点
      pos = (pos + 4 * 3) % gConfig.round_length; // 飞过去
      res_path.push('pos-' + pos);
    }
  }

  // 写回棋子的状态和位置
  rooms_pool[room_id].players[room.nowTurn].chessman[chess].status = status;
  rooms_pool[room_id].players[room.nowTurn].chessman[chess].position = pos;

  // 返回值
  return res_path;
}


// 启动新回合，标记是否game over
var next_turn = function(room_id) {
  var room = rooms_pool[room_id];

  room.waitingFor--; // 说明来了一个人
  // 先判断一下还等不等人
  if (room.waitingFor > 0) {
    console.log('还要等: ', room.waitingFor);
    return false;
  }

  var now_turn = room.nowTurn;
  var old_turn = now_turn;

  // 在这里要判断一个玩家是不是结束了
  // 只有当前玩家才有可能结束游戏（如果以后要改就放到下面的while循环里，不建议改）
  if (room.players[old_turn].chessman.filter(item => item.status === CHESS_STATUS.ARRIVED).length === 4) {
    return {
      game_over: true,
      user_id: room.players[old_turn].user_id,
      username: room.players[old_turn].username,
    }; // 返回游戏结束的信息
  }

  do {
    now_turn = (now_turn + 1) % 4; // 最大是四个玩家嘛
    var player = room.players[now_turn];
    if (player.gaming) {
      break; // 决定就是这个玩家了！
    }

  } while (now_turn != old_turn);

  // 判断一下会不会当前玩家已经跪了，如果是就返回游戏结束
  if (!room.players[now_turn].gaming) {
    return {
      game_over: true,
      username: '房间炸了',
    }; // 返回游戏结束的信息
  }

  rooms_pool[room_id].nowTurn = now_turn; // 写回

  return {
    game_over: false,
    user_id: room.players[now_turn].user_id,
    username: room.players[now_turn].username,
  };
}

var swap_chair = function(room_id, user_id, dist_chair) {
  var players = rooms_pool[room_id].players;

  var src_chair = players.findIndex(item => item.user_id === user_id);

  console.log(dist_chair);

  if (src_chair !== -1 && players[dist_chair] && players[dist_chair].empty) {
    var temp = players[dist_chair]; // 讲道理这里应该是空
    players[dist_chair] = players[src_chair];
    players[src_chair] = temp;
    return true;
  } else {
    return false;
  }
}

module.exports = {
  rooms_pool: rooms_pool,
  createRoom: createRoom,
  joinRoom: joinRoom,
  leaveRoom: leaveRoom,

  set_ready: set_ready, // 设置玩家的准备状态
  swap_chair: swap_chair, // 玩家换位到空位上

  socket_in_room: socket_in_room,
  start_game: start_game,
  random_dice: random_dice,
  set_gaming: set_gaming,
  turn_to_user: turn_to_user, // room_id => user
  now_turn: now_turn,
  get_available: get_available,
  get_movement_path: get_movement_path, // 获取棋子的移动路径

  next_turn: next_turn, // 开启新回合
}