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
  });
  socket.on('any', function(data) {
    console.log(data);
  });

  socket.on('rooms list', function(rooms) {
    $('.rooms.list *').remove();
    var keys = Object.keys(rooms);
    for (var i = keys.length - 1; i >= 0; i--) {
      var room = rooms[keys[i]];
      var r = $('<div>').addClass('item').text(room.owner + ' 的房间 [' + room.players.length + '/4]').attr({
        _id: room.id
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
  socket.on('room created', function(room) {
    window.switch_page('/singleRoom');
  });
}



// 切换路由对应功能
window.decide_page = function() {
  // 移除所有socket监听
  if(window.socket) {
    window.socket.removeAllListeners();
  }
  if (window.router == '/index') {
    index();
  } else if (window.router == '/rooms') {
    rooms();
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
});