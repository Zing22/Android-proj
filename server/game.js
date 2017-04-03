var io = require('./app.js').io;

var Room = {
    new: function() {
        var room = {};
        room.id = Math.random().toString(36).substr(2);
        room.owner = '';
        room.players = Array(4).fill({empty: true});
        room.size = function() {
            return room.players.filter(item => item.empty != true).length;
        }

        room.setReady = function(user_id, ready) {
            var i = room.players.findIndex(item => item.user_id == user_id);
            room.players[i].ready = ready;
            return true;
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

// 把一个玩家塞到房间里
// 已经判断了可行性（有空位）
var insertPlayer = function(user_id, username, room_id, isHost) {
    var empty_index = rooms_pool[room_id].players.findIndex(item => item.empty === true);
    rooms_pool[room_id].players[empty_index] = {
        user_id: user_id,
        username: username,
        host: isHost,
        ready: isHost,
    }

    return true;
}


var createRoom = function(user_id, username) {
    do {
        var room = Room.new();
    } while (!!rooms_pool[room.id]);
    room.owner = username;

    console.log('Creater room: ' + room.id);
    rooms_pool[room.id] = room;

    var res = insertPlayer(user_id, username, room.id, isHost=true);

    socket_in_room[user_id] = room.id;

    // 返回给上个函数，给socket加入room
    return room.id;
}


var joinRoom = function(user_id, username, room_id) {
    insertPlayer(user_id, username, room_id, isHost=false);

    socket_in_room[user_id] = room_id;
}


var leaveRoom = function(user_id, room_id) {
    var old = rooms_pool[room_id];
    // 防止重启清理内存
    if (old) {
        // rooms_pool[room_id].players = old.players.filter(item => item.user_id != user_id);
        var p_index = old.players.findIndex(item => item.user_id === user_id);
        var isHost = old.players[p_index].host; // 记录一下是不是房主退出
        
        old.players[p_index] = {empty: true};
        if (rooms_pool[room_id].size() == 0) {
            delete rooms_pool[room_id];
        } else if (isHost) {
            var new_host = old.players.findIndex(item => !item.empty);
            old.players[new_host].host = true;
            old.players[new_host].ready = true;
        }
        delete socket_in_room[user_id];
        return true;
    }
    return false;
}

module.exports = {
    rooms_pool: rooms_pool,
    createRoom: createRoom,
    joinRoom: joinRoom,
    leaveRoom: leaveRoom,

    socket_in_room: socket_in_room,
}