#lts-job 轻量级分布式任务调度系统

##概述
主要针对数据中心复杂的任务依赖设计，主要参考开源项目azkaban(https://azkaban.github.io/)和xxl-job(http://www.xuxueli.com/xxl-job/)设计实现。

##编译
mvn clean package -DskipTests -U

### 部署
#### lts-console 部署
目前lts-console为单节点部署，只需要部署一个实例即可。

在指定目录下解压lts-console-server-0.0.1-SNAPSHOT.zip
修改 ${解压目录}/lts-console/conf/application.yml 配置文件。主要修改数据库配置，静态资源配置等信息
````
spring.resources.static-locations 配置指定静态文件地址，需要在该配置最后添加前端js项目根目录配置
例如：前端js项目部署目录为 /data/www/lts-job/web，则
spring.resources.static-locations: classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/,file:/data/www/lts-job/web,file:/data/www/lts-job/web/static

````
 
#### lts-executor 部署

在指定目录下解压lts-executor-0.0.1-SNAPSHOT.zip，
修改 ${解压目录}/lts-executor/conf/application.yml 配置文件,主要修改数据库配置等信息。

#### lts-job-web 部署
lts-job-web 前端js项目，单独打包，由lts-console 实例加载运行。


#### update 
ALTER TABLE `flow_task` 
ADD COLUMN `host` VARCHAR(45) NULL AFTER `trigger_mode`;
