var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', (req, res, next) => {
  res.render('index', {
    title: '飙车飞行棋'
  });
});

router.get('/rooms', (req, res, next) => {
  res.render('rooms', {
    title: '房间列表'
  });
});

router.get('/singleRoom', (req, res, next) => {
  res.render('singleRoom', {
    title: '房间'
  });
});

router.get('/game', (req, res, next) => {
  res.render('game', {
    title: '游戏'
  });
});

module.exports = router;