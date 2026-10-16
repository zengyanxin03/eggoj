# eggoj
蛋壳OJ判题系统

用于在线评测编程题目代码的系统，能够根据用户提交的代码、出题人预先设置的题目输入和

输出用例，进行编译代码、运行代码、判断代码运行结果是否正确。

### 核心业务流程

<img width="1418" height="502" alt="image" src="https://github.com/user-attachments/assets/d2821ee0-1c24-420e-8b03-3e3c118e34e1" />

<img width="1964" height="986" alt="image" src="https://github.com/user-attachments/assets/bfe4e45e-5f06-4e7d-acff-fe64220b6248" />

### 系统功能
<img width="1300" height="620" alt="image" src="https://github.com/user-attachments/assets/309fa70b-8263-44b6-b4e2-ba1f9b385601" />

### 微服务划分
依赖服务：
注册中心：Nacos
微服务网关（eggoj-backend-gateway）：Gateway聚合所有的接口，统一接收前端的请求
公共模块：
