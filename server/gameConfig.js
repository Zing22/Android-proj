// 本文件定义一些游戏里的设置、参数、规则

// 绿色玩家能够走到的地方。格式为：起点，终点，最终跑道的起点，最终跑道的长度
var player_path_green = [0, 58, 52, 6];

// 所有玩家的飞行路径
var player_path = [player_path_green];

// 允许起飞的骰子点数
var ready_allow = [2, 4, 6];

module.exports = {
    player_path: player_path,
    ready_allow: ready_allow,
}