var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', (req, res, next) => {
  res.render('index', { title: '飙车飞行棋' });
});

router.get('/rooms', (req, res, next) => {
  res.render('rooms', { title: '房间列表' });
});

module.exports = router;