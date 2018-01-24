# jscp
## 功能
java nio实现类似于scp的功能

服务器监听端口,等待客户端连接,客户端连接之后发送文件路径给服务端，服务端直接写回文件，客户端接收完断开连接


## 使用
- 服务端：
java -jar jscp.jar
- 客户端:
java -jar jscp.jar 127.0.0.1 d:\\1.txt f:\\
