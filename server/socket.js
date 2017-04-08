var app = require('./app.js').app;
var io = require('./app.js').io;
var game = require('./game.js');

// 每个socket只能在一个room中
var get_room_of = function(socket) {
  // return Object.keys(socket.rooms).filter(item => item != socket.id);
  return game.socket_in_room[socket.id];
}

var update_room_info = function(socket, room_id) {
  if(!room_id) {
    room_id = get_room_of(socket);
  }

  if(game.rooms_pool[room_id]) {
    io.in(room_id).emit('players info', game.rooms_pool[room_id].players);  
  }
}


var sys_chat = function(room_id, msg) {
  io.in(room_id).emit('chat msg', msg);
}


io.sockets.on('connection', function(socket) {
  socket.emit('set username', {
    num: Math.ceil(Math.random() * 2000),
    user_id: socket.id
  });

  socket.emit('rooms list', game.rooms_pool);

  // 创建房间
  socket.on('create room', function(data) {
    var room_id = game.createRoom(socket.id, data.username);
    socket.join(room_id);
    // 群发一下新房间创建了
    io.emit('rooms list', game.rooms_pool);
    // 通知房主加入房间
    socket.emit('room enter', true);
  });

  // 玩家点击加入房间
  socket.on('join room', function(data) {
    // 判断房间满不满
    if (game.rooms_pool[data.room_id].size() >= 4) {
      socket.emit('room enter', '房间满啦');
    }
    socket.join(data.room_id);
    // 注意：socket.join比较耗时，异步执行时不能马上体现到socket.rooms上
    game.joinRoom(socket.id, data.username, data.room_id);
    socket.emit('room enter', true);
  });

  // 玩家加载完房间页面
  socket.on('in room', function() {
    var room_id = get_room_of(socket);
    update_room_info(socket);
    // socket.emit('players info', game.rooms_pool[room_id].players);
  });

  // 玩家准备 or 取消准备
  socket.on('set ready', function(ready) {
    var room_id = get_room_of(socket);
    var res = game.rooms_pool[room_id].setReady(socket.id, ready);
    if(res) {
      update_room_info(socket);
    }
  });


  // 玩家发送聊天信息
  socket.on('chat send', function(msg) {
    // 转发给房间里所有人
    room_id = get_room_of(socket);
    io.in(room_id).emit('chat msg', msg); // 包括自己
  });


  // 玩家点击 开始游戏
  socket.on('wanna start game', function() {
    if(game.start_game(get_room_of(socket))) {
      socket.emit('game started');
    } else {
      socket.emit('game not ready');
    }
  });


  // 玩家加载完游戏界面
  socket.on('game loaded', function() {
    var room_id = get_room_of(socket);
    var turn = game.set_gaming(socket.id, room_id);
    if(turn !== -1) {
      // 返回玩家的user_id 和 username
      var player = game.turn_to_user(room_id);
      io.in(room_id).emit('turn dice', player);
    }
  });


  // 玩家要掷骰子，只有能掷骰子的玩家才能发送这个
  socket.on('wanna dice', function() {
    var room_id = get_room_of(socket);

    var dice = game.random_dice(room_id); // 它会存到room里的
    // 广播掷骰子结果
    io.in(room_id).emit('dice result', {
      user_id: socket.id,
      dice: dice,
      player_num: game.now_turn(room_id),
      available: game.get_available(room_id), // 放哪些棋子是可以move的，前端要用
    });

    var username = game.turn_to_user(room_id).username;
    sys_chat(room_id, username + ' 掷出 ' + dice);
  });


  // 玩家选择移动棋子【有死循环！】
  socket.on('move chessman', function(num) {
    // 知道玩家要移动第几个棋子，也知道玩家
    var room_id = get_room_of(socket);
    // 返回玩家序号，棋子序号，棋子的移动路径
    io.in(room_id).emit('chess move', {
      player_num: game.now_turn(room_id),
      chess_num: num,
      move_path: game.get_movement_path(room_id, num),
    });
  });


  // 玩家发送“移动棋子”结束的信号
  socket.on('chess move done', function() {
    var room_id = get_room_of(socket);
    var player = game.turn_to_user(room_id);
    if(socket.id === player.user_id) {
      // 当前回合的玩家的棋子移动动画结束了
      // TODO: 新增的回合阶段从这里开始
      // 这里标志着普通的移动回合结束了
      // 如果要结算任务什么的应该从这里开始
      // 但是现在没有任务，所以直接进入下一回合
      var next_player = game.next_turn(room_id);
      if(next_player.game_over) {
        io.in(room_id).emit('game over', next_player);
      } else {
        io.in(room_id).emit('turn dice', next_player);
      }
    }
  });


  // 断开连接
  socket.on('disconnect', function() {
    var room_id = get_room_of(socket);
    var change = game.leaveRoom(socket.id, room_id);
    console.log(socket.id + ' disconnected.' + change);
    if (change) {
      // 通知一下在房间里的人
      update_room_info(socket, room_id);
      // 群发一下房间列表
      io.emit('rooms list', game.rooms_pool);
    }
  });
});