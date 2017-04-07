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
      var player = game.turn_to_user_id(room_id, turn);
      io.in(player).emit('my turn');
    }
  });


  // 玩家要掷骰子
  socket.on('wanna dice', function() {
    var room_id = get_room_of(socket);
    io.in(room_id).emit('dice result', {
      dice: game.random_dice(room_id),
      player: game.now_turn(room_id),
    });
  });


  // 玩家选择移动棋子
  socket.on('move chessman', function() {
    var room_id = get_room_of(socket);
    // TODO
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