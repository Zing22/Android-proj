// 主页路由
var index = function() {
  $('.start').click(function(event) {
    window.switch_page('/rooms');
  });
}



// 房间列表
var rooms = function() {
  console.log('Establish socket');
  window.socket = io('http://127.0.0.1');
  socket.on('set username', function(data) {
    console.log(data);
    window.username += '#' + data.num;
    window.user_id = data.user_id;
  });

  socket.on('rooms list', function(rooms) {
    $('.rooms.list *').remove();
    var keys = Object.keys(rooms);
    for (var i = keys.length - 1; i >= 0; i--) {
      var room = rooms[keys[i]];
      var r = $('<div>').addClass('item').text(room.owner + ' 的房间 [' + room.players.filter(item => !item.empty).length + '/4]');
      // 点击加入房间
      $(r).click(() => {
        socket.emit('join room', {
          room_id: room.id,
          username: window.username
        });
      });
      $('.rooms.list').append(r);
      if (i) {
        $('.rooms.list').append($('<div>').addClass('ui divider'));
      }
    }
  });

  $('.create-room').click(function() {
    socket.emit('create room', {
      username: window.username
    });
  });

  // 创建房间完成后，进入房间
  socket.on('room enter', function(ok) {
    if (ok === true) {
      window.switch_page('/singleRoom');
    } else {
      alert(ok);
    }
  });
}



// singleRoom 页
var singleRoom = function() {
  // 计算每个玩家位的宽度
  const player_border = 1;
  var width = $('#window').width() / 4 - player_border * 2;
  $('.single-room.player').width(width);
  $('.single-room.player').height(width);

  $('.chessman').width(width / 2).height(width / 2).css('border-radius', width / 2);

  // 告诉服务器我进房间了，可以发送房间信息给我
  socket.emit('in room');

  // 这个顺序很有意思，应该从绿色开始，顺时针转，就是这样的顺序
  window.player_colors = ['green', 'red', 'blue', 'yellow'];
  socket.on('players info', function(players) {
    console.log(players);
    for (var i = 0; i < players.length; i++) {
      // 空位
      if (players[i].empty) {
        $('.single-room.player.' + player_colors[i] + ' > .username').text('空位');
        continue;
      }
      // 非空位
      var name = '';
      if (players[i].host) {
        name = '[H]';
      } else if (players[i].ready) {
        name = '[RD]';
      }
      name += players[i].username;
      $('.single-room.player.' + player_colors[i] + ' > .username').text(name);

      // 设置房间标题
      if (players[i].host) {
        $('.title').text(players[i].username + ' 的房间');
      }

      if (players[i].user_id == window.user_id) {
        if (players[i].host) {
          // 我是房主
          $(".single-room.orange").addClass('start-game').text('开始游戏');
          continue;
        }
        // 这就是我，而且不是房主
        var $btn = $(".single-room.orange").removeClass('start-game');
        if (players[i].ready) {
          $btn.addClass('ready');
          $btn.text('取消准备');
        } else {
          $btn.removeClass('ready');
          $btn.text('准备');
        }
        $btn.click(function() {
          socket.emit('set ready', !$(this).hasClass('ready'));
        });
      }
    }
  });

  // 点击开始游戏
  $('.single-room.start-game').click(function(event) {
    if($(this).hasClass('start-game')) {
      socket.emit('wanna start game');
    }
  });

  // 服务器返回是否可以开始游戏
  socket.on('game not ready', function() {
    alert('有玩家木有准备好');
  });
  socket.on('game started', function() {
    // 切换到game页
    window.switch_page('/game');
  });


  // 启动聊天功能
  chat();
}

// game 页
var game = function() {
  // 调整页面宽度
  var width = $('#window').width();
  var board_width = width * 0.7;
  $('.game.board').width(board_width).height(board_width);
  const chessman_div_borad = 0.04; // 棋子多宽
  // 调整棋子宽度
  $('.game.chessman').width(board_width * chessman_div_borad)
    .height(board_width * chessman_div_borad)
    .css('border-radius', board_width * chessman_div_borad);

  var task_width = width * (1 - 0.7);
  // 设置任务栏宽高
  $('.game.tasks-list').width(task_width).height(board_width);
  var chat_height = $('#window').height() - $('#window > .title').height() - board_width;
  // 设置棋子detail的宽高
  $('.game.chess-detail').width(board_width).height(chat_height);
  // 设置聊天框
  $('.game.chat-area').width(task_width).height(chat_height);

  socket.emit('game loaded');

  // 监听我的回合
  socket.on('my turn', function() {
    $('.game.dice').addClass('active').text('掷骰子');
  });

  $('.game.dice').click(function(event) {
    if(!$(this).hasClass('active')) return false;
    socket.emit('wanna dice');
    $(this).removeClass('active').text('等待结果...');
  });

  var toggle_chess_active = function(color) {
    var player = player_colors.indexOf(color);
    for (var i = 0; i < 4; i++) {
      $('.game.chessman.' + color + '.num-' + i).toggleClass('active');
    }
  }

  var now_turn = -1; // 当前玩家
  socket.on('dice result', function(data) {
    $('.game.dice').text('选择棋子前进 ' + data.dice + '步');
    toggle_chess_active(player_colors[data.player]);
    now_turn = data.player; // 存起来
  });

  $('.game.chessman').click(function(event) {
    if(!$(this).hasClass('active')) return false;
    socket.emit('move chessman', $(this).attr('num'));
    toggle_chess_active(player_colors[now_turn]);
    $('.game.dice').text('等待回合开始...');
  });

  socket.on('chess moved', function(ok) {
    // 如果不ok，说明出bug了
    if(ok)
      alert('棋子移动啦');
    else
      alert('出bug啦');
  });
}



// 切换路由对应功能
window.decide_page = function() {
  // 移除所有socket监听
  if (window.socket) {
    window.socket.removeAllListeners();
  }
  if (window.router == '/index') {
    index();
  } else if (window.router == '/rooms') {
    rooms();
  } else if (window.router == '/singleRoom') {
    singleRoom();
  }  else if (window.router == '/game') {
    game();
  } else {
    alert('Unknow page!');
  }
}



$(document).ready(function() {
  window.username = 'Zing'; // 目前不实现设置页
  window.router = '/index';

  const max_width = 2048;
  const window_ratio = 1/1;
  const border_width = 0;

  function init_window() {
    var width = Math.min($(window).width(), max_width);
    var height = Math.min($(window).height(), width * window_ratio);
    width = height / window_ratio;
    height -= border_width * 2; // 减去上下两个边框

    $("#window").width(width);
    $("#window").height(height);

  }
  init_window();
  window.decide_page();

  // for drawing UI
  var debug = function() {}

  debug();
});