// 本文件定义一些游戏里的设置、参数、规则

// 外环的个数
var round_length = 52;

// 绿色玩家能够走到的地方。格式为：起点，终点，最终跑道的起点，最终跑道的终点
var player_path_green = [51, 48, 52, 72];
var player_path_red = [12, 9, 53, 73];
var player_path_blue = [25, 22, 54, 74];
var player_path_yellow = [38, 35, 55, 75];

// 飞机场撞人点
var air_hit_point = [62, 63, 60, 61];

// 所有玩家的飞行路径
var player_path = [player_path_green, player_path_red, player_path_blue, player_path_yellow];

// 允许起飞的骰子点数
var ready_allow = [2, 4, 6];

// 允许再来一回合的数字
var allow_another_turn = [6,];

module.exports = {
    player_path: player_path,
    ready_allow: ready_allow,
    round_length: round_length,
    allow_another_turn: allow_another_turn,
    air_hit_point: air_hit_point,
}