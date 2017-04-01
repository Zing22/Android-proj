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