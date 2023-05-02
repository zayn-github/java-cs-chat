package pro2.multi;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author zayn
 * @date 2023/4/7/19:00
 * TCP 聊天程序服务器端
 */
public class Server {
    static ServerSocket serverSocket;
    static Socket socket;
    static int port = 8888;

    public static void main(String[] args) throws IOException, InterruptedException {
        serverSocket = new ServerSocket(port);
        System.out.println("服务端在8888端口监听");
        socket = serverSocket.accept();
        System.out.println("客户端连接成功");

        //创建线程并启动
        ServerSend send = new ServerSend();
//        send.setDaemon(true);//设置为守护线程
        send.start();

        ServerReceive receive = new ServerReceive();
//        receive.setDaemon(true);//设置为守护线程
        receive.start();

        //线程插队执行
        send.join();
        receive.join();

        //关闭流
        ServerSend.close();
        ServerReceive.close();
        socket.close();
        serverSocket.close();

        System.out.println("服务端退出成功");
    }
}

class ServerSend extends Thread {
    //运行标记
    private static boolean running = true;
    //输出流
    static BufferedWriter writer;
    //键盘输入流
    static BufferedReader kb;
    //接收输入流
    static BufferedReader reader;
    static final int TIMEOUT = 100;

    static {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(Server.socket.getOutputStream()));
            kb = new BufferedReader(new InputStreamReader(System.in));
            reader = new BufferedReader(new InputStreamReader(Server.socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (running) {
            String str;
            try {
                if (kb != null && kb.ready()) {//检查键盘输入是否可用
                    str = kb.readLine();//获取键盘输入
                    if (!str.equals("")) {//判断是否为空
                        send(str);//发送信息至客户端
                        if (str.equals("quit")) {//判断是否退出
                            running = false;
                            close();
                            ServerReceive.setRunning();
                            break;
                        }
                    }
                } else {
                    //等待指定的超时时间
                    Thread.sleep(TIMEOUT);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 发送信息
     *
     * @param str 要发送的信息
     * @throws IOException IO异常
     */
    public static void send(String str) throws IOException {
        writer.write(str);
        writer.newLine();
        writer.flush();
    }

    /**
     * 关闭流
     */
    public static void close() {
        try {
            writer.close();
            kb.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setRunning() {
        running = false;
    }
}

class ServerReceive extends Thread {
    static BufferedReader reader;
    static Socket clientSocket;
    //运行标记
    private static boolean running = true;

    static {
        try {
            reader = new BufferedReader(new InputStreamReader(Server.socket.getInputStream()));
            clientSocket = Server.socket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 接收信息
     * 判断quit退出
     *
     */
    @Override
    public void run() {
        while (running) {
            String str = "";
            try {
                str = reader.readLine();//接收信息
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
            if (!str.equals("")) {//判断是否为空
                System.out.println("客户端：" + str);
            }
            if (str.equals("quit")) {//判断是否退出
                running = false;
                ServerSend.setRunning();
                break;
            }
        }
        close();
    }

    /**
     * 关闭流和socket
     */
    public static void close() {
        try {
            reader.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置运行标记
     */
    public static void setRunning() {
        running = false;
    }
}