var app = require('./app.js').app;
var io = require('./app.js').io;
var game = require('./game.js');


// 这个函数的正确性，建立在"每个socket只能在一个room中"之上
var get_rooms_of = function(socket) {
  return Object.keys(socket.rooms).filter(item => item != socket.id);
}

var update_room_info = function(socket) {
  var room_id = get_rooms_of(socket)[0];
  io.in(room_id).emit('players info', game.rooms_pool[room_id].players);
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
    var room_id = get_rooms_of(socket)[0];
    update_room_info(socket);
    // socket.emit('players info', game.rooms_pool[room_id].players);
  });

  // 玩家准备 or 取消准备
  socket.on('set ready', function(ready) {
    var room_id = get_rooms_of(socket)[0];
    var res = game.rooms_pool[room_id].setReady(socket.id, ready);
    if(res) {
      update_room_info(socket);
    }
  });


  // 断开连接
  socket.on('disconnect', function() {
    var room_id = get_rooms_of(socket)[0];
    game.leaveRoom(socket.id, room_id);
    console.log(socket.id + ' disconnected.');
  });
});