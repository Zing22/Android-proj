## Server文件目录

1. **public**: Static files
    - images: Image files
    - javascripts: JS files
        - libs: Third part libraries
    - stylesheets: CSS files
    - others: Something else
2. routes: Routers of web pages
3. views: Web pages in `.pug` form
4. app.js: Where the application defined
5. config.js: Application configurations
6. **game.js**: Game's definitions and operations
7. **gameConfig.js**: Game's configurations
8. **socket.js**: Socket network APIs
9. package.json: npm packages list and etc.
10. utils.js: Commonly used functions
11. ecosystem.json: Configuration file for `pm2`


## 本地部署

### 首次部署
0. 安装 node v6.10.2 LTS，打开命令行
1. `cd /YOUR PATH/Android-proj/server`
2. `npm install`
3. `npm install nodev -g`

### 启动服务
0. 打开命令行
1. `npm start`


## 服务器部署
0. 打开**git bash**
1. `ssh syzxb@dragracing.tech` 然后输入服务器密码
2. `cd ~/Android-proj/server`
3. `git pull`
4. `pm2 delete 0 && pm2 start ecosystem.json`