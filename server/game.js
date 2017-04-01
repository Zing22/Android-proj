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
    } while(!!rooms_pool[room.id]);
    room.owner = username;
    room.players.push(user_id);
    rooms_pool[room.id] = room;

    // 返回给上个函数，给socket加入room
    return room.id;
}

module.exports = {
    createRoom: createRoom,
    rooms_pool: rooms_pool,
}