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
      var r = $('<div>').addClass('item').text(room.owner + ' 的房间 [' + room.players.length + '/4]');
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
  var width = $('#window').width() / 2 - player_border * 2;
  $('.single-room.player').width(width);
  $('.single-room.player').height(width);

  $('.chessman').width(width / 2).height(width / 2).css('border-radius', width / 2);

  // 告诉服务器我进房间了，可以发送房间信息给我
  console.log('In room now');
  socket.emit('in room');

  const player_colors = ['green', 'red', 'blue', 'yellow'];
  socket.on('players info', function(players) {
    for (var i = 0; i < players.length; i++) {
      var name = '';
      if (players[i].host) {
        name = '[H]';
      } else if (players[i].ready) {
        name = '[RD]';
      }
      name += players[i].username;
      $('.single-room.player.' + player_colors[i] + ' > .username').text(name);

      if (players[i].user_id == window.user_id && !players[i].host) {
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
  } else {
    alert('Unknow page!');
  }
}



$(document).ready(function() {
  window.username = 'Zing'; // 目前不实现设置页
  window.router = '/index';

  const max_width = 760;
  const window_ratio = 16.0 / 9;
  const border_width = 5;

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

  // for drawing
  var debug = function() {}

  debug();
});