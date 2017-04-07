var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var favicon = require('serve-favicon');

// 子页面们
var index = require('./routes/index');

// 主程序
var app = express();
var utils = require('./utils.js');

// 加载系统设置
var config = require('./config.js');

// 网页logo
app.use(favicon(path.join(__dirname,'public','images','favicon.ico')));

// 页面模板
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// 设置子页面路由
app.use('/', index);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});



/**
 * Get port from environment and store in Express.
 */

var port = config.serverPort;
app.set('port', port);

/**
 * Create HTTP server.
 */

var server = require('http').createServer(app);

/**
 * Listen on provided port, on all network interfaces.
 */

server.listen(port);
server.on('error', utils.onError);
server.on('listening', utils.onListening);

var io = require('socket.io').listen(server);


module.exports = {
    app: app,
    io: io,
};

var socket = require('./socket.js');