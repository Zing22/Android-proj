// 主页路由
var index = function() {
  $('.start').click(function(event) {
    window.switch_page('/rooms');
  });
}

// 房间列表
var rooms = function() {
  console.log('switch to rooms!');
}

// 切换路由对应功能
window.decide_page = function() {
  if (window.router == '/index') {
    index();
  } else if (window.router == '/rooms') {
    rooms();
  }
}

$(document).ready(function() {
  
  window.router = '/index';

  const max_width = 760;
  const window_ratio = 16.0 / 9;
  const border_width = 5;
  function init_window() {
      var width = Math.min($(window).width(), max_width);
      var height = Math.min($(window).height(), width*window_ratio);
      width = height/window_ratio;
      height -= border_width*2; // 减去上下两个边框

      $("#window").width(width);
      $("#window").height(height);
  }


  init_window();
  window.decide_page();
});