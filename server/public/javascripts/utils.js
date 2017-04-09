// 这是静态文件 utils.js

function switch_page(url) {
  // 切换页面
  // 删去目前页面，显示加载过程（有空做）
  // 加载新页面
  // 替换 #window
  $.get(url, function(data) {
    $('#window').html(data);
    window.router = url;
    window.decide_page();
  });
}


// 聊天功能
function chat() {
  var chat_num = 0;
  $('.chat.input').keypress(function(event) {
    // 回车键
    if (event.keyCode == 13 && $(this).val()) {
      var msg = window.username + ": " + $(this).val();
      $(this).val("");
      window.socket.emit('chat send', msg);
    }
  });

  window.socket.on('chat msg', function(msg) {
    var $item = $('<div>').addClass('item').text(msg);
    if(chat_num % 2 === 0) {
      $item.addClass('odd');
    }
    $('.chat.list').append($item);
    $('.chat.list').animate({ scrollTop: $('.chat.list')[0].scrollHeight }, 300);
    chat_num++;
  });
}


function settings() {
  $('.btn.settings').click(event => {
    $('.settings-box > input[name="username"]').val(window.username);
    $('.settings-box').fadeIn();
  });

  $('.settings-box .username.commit').click(event => {
    window.username = $('.settings-box > input[name="username"]').val();
    $('.settings-box').fadeOut();
  });

  $('.settings-title > .close').click(event => {
    $('.settings-box').fadeOut();
  })
}