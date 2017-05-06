// 主页路由
var index = function() {
  settings(); // 启动settings

  if(!window.socket) {
    window.socket = io(); // 建立连接
    socket.emit('random name', window.username);
  }
  socket.on('new username', function(data) {
    console.log(data);
    window.username = data.new_name;
    window.user_id = data.user_id;
  });


  $('.start').click(function(event) {
    window.switch_page('/rooms');
  });
}



// 房间列表
var rooms = function() {
  socket.emit('need rooms list');

  socket.on('rooms list', function(rooms) {
    $('.rooms.list *').remove();
    var keys = Object.keys(rooms);
    for (var i = keys.length - 1; i >= 0; i--) {
      var room = rooms[keys[i]];
      var text = room.owner + ' 的房间 [' + room.players.filter(item => !item.empty).length + '/4]';
      var r = $('<div>').addClass('item').attr('room_id', room.id);
      // 点击加入房间
      if (!room.gaming) {
        $(r).click(function() {
          socket.emit('join room', {
            room_id: $(this).attr('room_id'),
            username: window.username
          });
        });
      } else {
        text += ' [游戏中]'
      }

      r.text(text);

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
      swal(ok);
    }
  });

  // 返回主页
  $('.return.icons').click(function(event) {
    window.switch_page('/index');
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

  // 这个顺序很有意思，应该从右上角绿色开始，顺时针转，就是这样的顺序
  window.player_colors = ['green', 'red', 'blue', 'yellow'];
  socket.on('players info', function(players) {
    console.log(players);
		
		var isHost = false;
		for (var i = 0; i < players.length; i++) {
			if (players[i].user_id == window.user_id) {
				isHost = players[i].host;
			}
		}
		
    for (var i = 0; i < players.length; i++) {
			// 隐藏删除AI的按钮
			var close_icon = $('.single-room.player.' + player_colors[i] + ' > .close');
			close_icon.addClass('hidden');
			close_icon.unbind('click');
			
      // 空位
      if (players[i].empty) {
        $('.single-room.player.' + player_colors[i]).addClass('empty');
				$('.single-room.player.' + player_colors[i] + ' > .chessman').click(function(event) {
          socket.emit('swap chair', $(this).parent().attr('chair'));
        });
        $('.single-room.player.' + player_colors[i] + ' > .username').text('空位');
        continue;
      }
      $('.single-room.player.' + player_colors[i]).removeClass('empty');
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
        $('.title > span').text(players[i].username + ' 的房间');
      }
			
			// 添加删除AI的按钮
			if (isHost && players[i].ai) {
				close_icon.removeClass('hidden').click(function(event){
					socket.emit('remove AI', players[$(this).parent().attr('chair')].user_id);
				})
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
    if ($(this).hasClass('start-game')) {
      socket.emit('wanna start game');
    }
  });

  // add AI
  $('.single-room.add-ai').click(function(event){
	socket.emit('add AI');
  });

  // add AI response
  socket.on('add AI response', function(err) {
	if (err === 'not host') {
	  swal('只有房主可以添加AI');
	} else if (err === 'room full') {
	  swal('房间已满');
	}
  });

  // 服务器返回是否可以开始游戏
  socket.on('game not ready', function() {
    swal('还有玩家木有准备');
  });
  socket.on('game started', function() {
    // 切换到game页
    window.switch_page('/game');
  });

  // 启动聊天功能
  chat();

  // 点击返回键
  $('.return.icons').click(function(event) {
    socket.emit('leave room');
    window.switch_page('/rooms');
  });
}

// game 页
var game = function() {
  // 调整页面宽度
  var width = $('#window').width();
  var board_width = width * 0.7;
  $('.game.board').width(board_width).height(board_width);
  const chessman_div_board = 0.04; // 棋子多宽
  const dicebox_div_board = 0.15; // 骰子框多宽
  // 调整棋子宽度
  $('.game.chessman').width(board_width * chessman_div_board)
    .height(board_width * chessman_div_board)
    .css('border-radius', board_width * chessman_div_board);

  $('.game.dice-box').width(board_width * dicebox_div_board)
    .height(board_width * dicebox_div_board)
    .css('border-radius', board_width * chessman_div_board * 0.3);

  var task_width = width * (1 - 0.7);
  // 设置任务栏宽高
  $('.game.tasks-list').width(task_width).height(board_width);
  var chat_height = $('#window').height() - $('#window > .title').height() - board_width;
  // 设置棋子detail的宽高
  $('.game.chess-detail').width(board_width).height(chat_height);
  // 设置聊天框
  $('.game.chat-area').width(task_width).height(chat_height);

  window.chat();

  socket.emit('game loaded');

  var remove_player = function(num) {
    var color = player_colors[num];
    for (var i = 0; i < 4; i++) {
      $('.game.chessman.' + color + '.num-' + i).hide();
    }
  }

  // 监听玩家列表，移除没有玩家的棋子
  socket.on('players info', function(players) {
    for (var i = 0; i < players.length; i++) {
      if (players[i].empty) {
        remove_player(i);
      }
    }
  });

  // 监听新的回合开始
  socket.on('turn dice', function(data) {
    console.log(data);
    $('.game.dice-box > .dice').removeClass().addClass('dice');
    $('.game.dice-box').fadeIn();
    if (data.user_id === socket.id) {
      // 当前回合是我
      $('.game.dice-box').addClass('active');
      $('.title > span').text('我的回合！');
    } else {
      // 多此一举
      $('.game.dice-box').removeClass('active');
      // 谁的回合
      $('.title > span').text(data.username + ' 的回合...');
    }
    socket.emit('turn dice done');
  });

  $('.game.dice-box').click(function(event) {
    if (!$(this).hasClass('active')) return false;
    // 等待结果从服务器返回
    socket.emit('wanna dice');
    $(this).removeClass('active')
  });

  var set_chess_active = function(color, available) {
    var player = player_colors.indexOf(color);
    for (var i = 0; i < available.length; i++) {
      $('.game.chessman.' + color + '.num-' + available[i]).addClass('active');
    }
  }

  var unset_chess_active = function(color) {
    var player = player_colors.indexOf(color);
    var chess_list = [0, 1, 2, 3];
    for (var i = 0; i < chess_list.length; i++) {
      $('.game.chessman.' + color + '.num-' + chess_list[i]).removeClass('active');
    }
  }

  var now_turn = -1; // 当前玩家
  socket.on('dice result', function(data) {
    console.log('dice result: ', data);
    // 设置棋子的点数
    $('.game.dice-box > .dice').addClass('dice-' + data.dice);
    // 骰子旋转完之后隐藏起来
    setTimeout(function() {
      $('.game.dice-box').fadeOut("fast", function() {
        // 骰子已经停止旋转了，隐藏好了
        // 如果不能移动，直接下一轮
        if (data.available.length === 0) {
          socket.emit('chess move done');
          return true;
        }
        if (data.user_id === socket.id) {
          // 如果是自己才设置棋子可以点击
          set_chess_active(player_colors[data.player_num], data.available);
          now_turn = data.player_num; // 存起来下面用
        }
        socket.emit('dice result done');
      });
    }, 1000);
  });

  $('.game.chessman').click(function(event) {
    if (!$(this).hasClass('active')) return false;
    socket.emit('move chessman', $(this).attr('num'));
    unset_chess_active(player_colors[now_turn]);
  });

  var chess_move_inter;
  socket.on('chess move', function(data) {
    console.log(data);
    var color = player_colors[data.player_num];
    var move_path = data.move_path;
    var $chess = $('.game.chessman.' + color + '.num-' + data.chess_num);
		var enemy_num = data.enemy_num;
		var attacked_chess = data.enemy_array;
    var i = 1;
    var inter = setInterval(function() {
      if (i >= move_path.length) {
        // 移动完了
				if (attacked_chess.length) { 	//有棋子被踩
					for (var j = 0; j < attacked_chess.length; j++) {
						var $achess = $('.game.chessman.' + player_colors[enemy_num] + '.num-' + attacked_chess[j]);
						$achess.removeClass(move_path[i-1]).addClass('not-ready');
					}
				}
				
        socket.emit('chess move done'); // 注意，四个玩家都会发这个给服务器
        clearInterval(inter); // 停止无限循环
      } else {
        $chess.removeClass(move_path[i - 1]).addClass(move_path[i]);
        i++;
      }
    }, 300);
    console.log(inter);
    chess_move_inter = inter;
  });

  socket.on('game over', function(data) {
    swal('游戏结束', data.username + ' 赢得了最终胜利！', 'success');
  });

  // 点击返回键
  $('.return.icons').click(function(event) {
    socket.emit('leave room');
    if (chess_move_inter) {
      chess_move_inter = clearInterval(chess_move_inter);
    }
    window.switch_page('/rooms');
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
  } else if (window.router == '/game') {
    game();
  } else {
    swal('Unknow page!');
  }
}



$(document).ready(function() {
  window.username = '玩家'; // 初始玩家名
  window.router = '/index';
  const max_width = 2048;
  const window_ratio = 1 / 1;
  const border_width = 0;

  function init_window() {
    var width = Math.min($(window).width(), max_width);
    var height = Math.min($(window).height(), width * window_ratio);
    width = height / window_ratio;
    height -= border_width * 2; // 减去上下两个边框

    $("#window").width(width);
    $("#window").height(height);

    var setting_width = $('.settings-box').width();
    $('.settings-box').css('left', ($(window).width() - setting_width)/2).hide();

  }
  init_window();

  window.switch_page('/index');

  // for drawing UI
  var debug = function() {}

  debug();
});
