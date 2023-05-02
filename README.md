# java-cs-chat
简易的java聊天程序，采用C/S结构，客户端和服务端多线程处理发送和接收信息。
## 服务端server
在本机8888号端口进行监听；连接成功后，启动发送和接收线程。
### 发送send
设置运行标记`running`用于决定是否关闭线程。
while循环检查是否退出。
创建BufferedReader对象获取键盘输入，并检查输入是否可用。可用则通过`BufferedWriter`对象发送；不可用则线程进入休眠100ms，然后检查`running`。
如果服务端发送的是`quit`，则设置`running`为`false`，发送完信息后立刻关闭发送线程。
###接收receive
同样设置`running`。
通过BufferedReader对象获取客户端通过socket发送的信息并输出在控制台；如果客户端发送的是`quit`，则关闭线程同时设置`send`线程的`running`为`false`以便关闭`send`线程.
