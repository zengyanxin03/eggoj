# eggoj
蛋壳OJ判题系统

用于在线评测编程题目代码的系统，能够根据用户提交的代码、出题人预先设置的题目输入和

输出用例，进行编译代码、运行代码、判断代码运行结果是否正确。

### 核心业务流程

<img width="1418" height="502" alt="image" src="https://github.com/user-attachments/assets/d2821ee0-1c24-420e-8b03-3e3c118e34e1" />

<img width="1964" height="986" alt="image" src="https://github.com/user-attachments/assets/bfe4e45e-5f06-4e7d-acff-fe64220b6248" />

### 系统功能
<img width="1322" height="626" alt="image" src="https://github.com/user-attachments/assets/1f0f90af-4691-4f95-9878-f7edaff2be90" />

### 判题模块和代码沙箱的关系
<img width="1008" height="424" alt="image" src="https://github.com/user-attachments/assets/00888e2f-46fd-4e4e-b97e-3bf68aeda56f" />

### 微服务的划分

依赖服务：

- 注册中心：Nacos
- 微服务网关（eggoj-backend-gateway）：Gateway 聚合所有的接口，统一接受处理前端

的请求

公共模块：

- common 公共模块（eggoj-backend-common）：全局异常处理器、请求响应封装类、

公用的工具类等

- model 模型模块（eggoj-backend-model）：很多服务公用的实体类

- 公用接口模块（eggoj-backend-service-client）：只存放接口，不存放实现（多个服务之

间要共享）

业务功能：

- 用户服务（eggoj-backend-user-service）：

​	a. 注册

​	b. 登录

​	c. 用户管理

- 题目服务（eggoj-backend-question-service）

​	a. 创建题目（管理员）

​	b. 删除题目（管理员）

​	c. 修改题目（管理员）

​	d. 搜索题目（用户）

​	e. 在线做题（题目详情页）

​	**f.** **题目提交**

- 判题服务（eggoj-backend-judge-service）

​	a. 执行判题逻辑

​	b. 错误处理（内存溢出、安全性、超时）

​	c. **自主实现** 代码沙箱（安全沙箱）

​	d. 开放接口（提供一个独立的新服务）

代码沙箱服务本身就是独立的，不用纳入 Spring Cloud 的管理
