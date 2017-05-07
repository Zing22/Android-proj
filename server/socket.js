var app = require('./app.js').app;
var io = require('./app.js').io;
var game = require('./game.js');


// 发送系统消息
var sys_chat = function(room_id, msg, title = '系统') {
  io.in(room_id).emit('chat msg', '[' + title + ']' + msg);
}

// 每个socket只能在一个room中
var get_room_of = function(socket) {
  // return Object.keys(socket.rooms).filter(item => item != socket.id);
  return game.socket_in_room[socket.id];
}

var update_room_info = function(socket, room_id, left) {
  if (!room_id) {
    room_id = get_room_of(socket);
  }

  if (game.rooms_pool[room_id]) {
    io.in(room_id).emit('players info', game.rooms_pool[room_id].players);
  }

  if (left) {
    sys_chat(room_id, left + ' 离开了')
  }
}

//AI的移动
var AI_action = function(socket, room_id, player) {
  game.AI_wait(room_id);
  io.in(room_id).emit('turn dice', player);
  update_room_info(socket, room_id);
}

io.sockets.on('connection', function(socket) {
  console.log(socket.id + ' 连接');
  socket.on('random name', function(old_name) {
    console.log('要名字');
    socket.emit('new username', {
      new_name: old_name + '#' + Math.ceil(Math.random() * 2000),
      user_id: socket.id
    });
  });

  // 想要房间列表
  socket.on('need rooms list', function() {
    socket.emit('rooms list', game.rooms_pool);
  })

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
    game.joinRoom(socket.id, data.username, data.room_id, false);
    socket.emit('room enter', true);
  });
  
  ///玩家添加AI
  socket.on('add AI', function(data) {
    room_id = get_room_of(socket);
    
    //console.log(game);
    var p_index = game.rooms_pool[room_id].players.findIndex(item => item.user_id === socket.id);
    var player = game.rooms_pool[room_id].players[p_index];
    if(player.host === false){
      socket.emit('add AI response','not host');
    }
    else if(game.rooms_pool[room_id].size() >= 4){
      socket.emit('add AI response','room full');
    }
    else{
      var AI_id = game.getAIID();
      game.joinRoom(AI_id, AI_id, room_id, true);
      game.set_ready(room_id, AI_id, true);
      update_room_info(socket, room_id, false);
      io.in(room_id).emit('chat msg', AI_id);
      io.emit('rooms list', game.rooms_pool);
    }
    //var res = game.set_ready(room_id, "AI123", true);
    // if (res) {
    //   update_room_info(socket);
    // }
  });

  socket.on('remove AI', function(AI_id) {
    room_id = get_room_of(socket);    
    game.leaveRoom(AI_id, room_id);

    update_room_info(socket, room_id, false);
    io.emit('rooms list', game.rooms_pool);
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
    var res = game.set_ready(room_id, socket.id, ready);
    if (res) {
      update_room_info(socket);
    }
  });


  // 玩家想换位子
  socket.on('swap chair', function(dist) {
    var room_id = get_room_of(socket);
    var success = game.swap_chair(room_id, socket.id, dist);
    if (success) {
      update_room_info(socket);
    }
  });


  // 玩家点击返回键退出房间（不是游戏）
  socket.on('leave room', function() {
    var room_id = get_room_of(socket);
    socket.leave(room_id); // 从socket的rooms list里面删去
    var change = game.leaveRoom(socket.id, room_id);
    console.log("**"+change);
    if(change === "onlyAI"){
      //已经没有玩家
      var noPlayer = game.no_player(room_id);
      if(noPlayer){
        delete game.rooms_pool[room_id]; //room_id没有了无法执行此操作！！
        io.emit('rooms list', game.rooms_pool);
        return;
      }
    }

    if (change) {
      // 通知一下在房间里的人
      update_room_info(socket, room_id, change.username);
      // 群发一下房间列表
      io.emit('rooms list', game.rooms_pool);
      console.log("Here:"+change.next_player);
      if (change.next_player) {
        // 说明当前回合的玩家离开了
        if (change.next_player.game_over) {
          io.in(room_id).emit('game over', change.next_player);
        } else {
          console.log("Here!");
          game.AI_wait(room_id);
          io.in(room_id).emit('turn dice', change.next_player);
        }
      }
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
    var room_id = get_room_of(socket);
    if (game.start_game(room_id)) {
      io.in(room_id).emit('game started');
      // 群发一下房间列表
      io.emit('rooms list', game.rooms_pool);
      console.log(game.rooms_pool);
    } else {
      socket.emit('game not ready');
    }
  });


  // 玩家加载完游戏界面
  socket.on('game loaded', function() {
    var room_id = get_room_of(socket);
    var turn = game.set_gaming(socket.id, room_id);
    if (turn !== -1) {
      // 返回玩家的user_id 和 username
      var player = game.turn_to_user(room_id);
      game.AI_wait(room_id);
      io.in(room_id).emit('turn dice', player);
      update_room_info(socket, room_id);  //这句话能去除多余玩家
    }
  });
  // AI接受前端turn dice处理完信号
  var ai_dice;
  socket.on('turn dice done', function() {
    var room_id = get_room_of(socket);
    var p_index = game.now_turn(room_id);
    var player = game.rooms_pool[room_id].players[p_index];
    if(!player.ai) return;

    if(game.AI_takeAction(room_id) === 0) {
      var dice = game.random_dice(room_id); // 它会存到room里的
      ai_dice = dice;
      var ai_available = game.get_available(room_id);
      game.AI_wait(room_id);
      // 广播掷骰子结果
      io.in(room_id).emit('dice result', {
        user_id: player.user_id,
        dice: dice,
        player_num: game.now_turn(room_id),
        available: ai_available, // 放哪些棋子是可以move的，前端要用
      });
      //console.log('AI dice:' + dice);
      var username = game.turn_to_user(room_id).username;
      sys_chat(room_id, username + ' 掷出 ' + dice);
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

  // AI接受前端dice result处理完信号
  socket.on('dice result done', function() {
    var room_id = get_room_of(socket);
    var p_index = game.now_turn(room_id);
    var player = game.rooms_pool[room_id].players[p_index];
    if(!player.ai) return;

    if(game.AI_takeAction(room_id) === 0) {
      var dice = ai_dice // 它会存到room里的
      var ai_available = game.get_available(room_id);
      if(ai_available.length !== 0){
        var num = game.get_ai_chess(room_id, dice);
        var res = game.get_movement_path(room_id, num);
        io.in(room_id).emit('chess move', {
          player_num: game.now_turn(room_id),
          chess_num: num,
          move_path: res[0],
          enemy_pair: res[1],
        });
      }
    }
  });

  // 玩家选择移动棋子
  socket.on('move chessman', function(num) {
    // 知道玩家要移动第几个棋子，也知道玩家
    var room_id = get_room_of(socket);
    var res = game.get_movement_path(room_id, num);
    // 返回玩家序号，棋子序号，棋子的移动路径
    io.in(room_id).emit('chess move', {
      player_num: game.now_turn(room_id),
      chess_num: num,
      move_path: res[0],
      enemy_pair: res[1],
    });
  });


  // 玩家发送“移动棋子”结束的信号
  socket.on('chess move done', function() {
    //console.log("chess move done!");
    var room_id = get_room_of(socket);
    // TODO: 新增的回合阶段从这里开始
    // 这里标志着普通的移动回合结束了
    // 如果要结算任务什么的应该从这里开始
    // 但是现在没有任务，所以直接进入下一回合
    var next_player = game.next_turn(room_id);
    if (!next_player) { // 说明还在等人
      return false;
    }
    if (next_player.game_over) {
      io.in(room_id).emit('game over', next_player);
      sys_chat(room_id, next_player.username + ' 获胜！');
    } else {
      game.AI_wait(room_id);
      io.in(room_id).emit('turn dice', next_player);
    }
  });


  // 断开连接
  socket.on('disconnect', function() {
    var room_id = get_room_of(socket);
    var change = game.leaveRoom(socket.id, room_id);
    console.log(change + ' disconnected.');
    if (change) {
      // 通知一下在房间里的人
      update_room_info(socket, room_id, change.username);
      // 群发一下房间列表
      io.emit('rooms list', game.rooms_pool);

      if (change.next_player) {
        // 说明当前回合的玩家离开了
        if (change.next_player.game_over) {
          io.in(room_id).emit('game over', change.next_player);
        } else {
          game.AI_wait(room_id);
          io.in(room_id).emit('turn dice', change.next_player);
        }
      }
    }
  });
});