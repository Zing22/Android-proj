var app = require('./app.js').app;
var io = require('./app.js').io;
var game = require('./game.js');

// console.log(require('./app.js'));

// 这个函数的正确性，建立在每个socket只能在一个room中
var get_room_of = function(socket) {
  return socket.rooms[Object.keys(socket.rooms)[0]];
}

io.sockets.on('connection', function(socket) {
  socket.emit('set username', {
    num: Math.ceil(Math.random() * 20000)
  });

  socket.emit('rooms list', game.rooms_pool);

  // 创建房间
  socket.on('create room', function(data) {
    var room_id = game.createRoom(socket.id, data.username);
    socket.join(room_id);
    // 这里应该加入房间了
    io.emit('rooms list', game.rooms_pool);
  });

  // 加入房间
  socket.on('join room', function(data) {
    socket.join(data.room_id);
  });

  socket.on('disconnect', function(data) {
    console.log(socket.id + ' disconnected.');
  });
});