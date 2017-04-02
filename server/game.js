var io = require('./app.js').io;

var Room = {
    new: function() {
        var room = {};
        room.id = Math.random().toString(36).substr(2);
        room.owner = '';
        room.players = [];
        room.size = function() {
            return room.players.length;
        }
        return room;
    }
}

// 房间池
var rooms_pool = {};


var createRoom = function(user_id, username) {
    do {
        var room = Room.new();
    } while (!!rooms_pool[room.id]);
    room.owner = username;
    room.players.push({
        user_id: user_id,
        username: username,
        host: true,
        ready: true,
    });

    console.log('Creater room: ' + room.id);
    rooms_pool[room.id] = room;

    // 返回给上个函数，给socket加入room
    return room.id;
}


var joinRoom = function(user_id, username, room_id) {
    rooms_pool[room_id].players.push({
        user_id: user_id,
        username: username,
        host: false,
        ready: false,
    });
}


var leaveRoom = function(user_id, room_id) {
    var old = rooms_pool[room_id];
    // 防止重启清理内存
    if (old) {
        rooms_pool[room_id].players = old.players.filter(item => item.user_id != user_id);
        if (rooms_pool[room_id].size() == 0) {
            delete rooms_pool[room_id];
        }
    }
}

module.exports = {
    rooms_pool: rooms_pool,
    createRoom: createRoom,
    joinRoom: joinRoom,
    leaveRoom: leaveRoom,
}