package pro2.multi;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author zayn
 * @date 2023/4/7/18:58
 * TCP 聊天程序客户端
 */
public class Client {
    static Socket ClientSocket;
    static int port = 8888;

    public static void main(String[] args) throws IOException, InterruptedException {
        //创建客户端套接字
        ClientSocket = new Socket(InetAddress.getLocalHost(), port);
        System.out.println("客户端启动成功");
        System.out.println("连接服务器成功");

        //创建线程并启动
        ClientSend send = new ClientSend();
//        send.setDaemon(true);//设置为守护线程
        send.start();

        ClientReceive receive = new ClientReceive();
//        receive.setDaemon(true);//设置为守护线程
        receive.start();

        //线程插队
        send.join();
        receive.join();

        //关闭流
        ClientSend.close();
        ClientReceive.close();
        ClientSocket.close();

        System.out.println("客户端退出成功");
    }
}

class ClientSend extends Thread {
    //输出流
    static BufferedWriter writer;
    //键盘输入流
    static BufferedReader kb;
    //接收输入流
    static BufferedReader reader;
    //运行标记
    private static boolean running = true;
    static final int TIMEOUT = 100;

    static {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(Client.ClientSocket.getOutputStream()));
            kb = new BufferedReader(new InputStreamReader(System.in));
            reader = new BufferedReader(new InputStreamReader(Client.ClientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取键盘输入
     * 发送信息
     * 判断quit退出
     */
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
                            ClientReceive.setRunning();
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
     * 关闭流和套接字
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

    /**
     * 设置运行标记
     *
     */
    public static void setRunning() {
        running = false;
    }
}

class ClientReceive extends Thread {
    //输入流
    static BufferedReader reader;
    static Socket clientSocket;
    //运行标记
    private static boolean running = true;

    static {
        try {
            reader = new BufferedReader(new InputStreamReader(Client.ClientSocket.getInputStream()));
            clientSocket = Client.ClientSocket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 接收信息
     * 判断quit退出
     *
     * @throws IOException IO异常
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
            if (str != null) {
                if (!str.equals("")) {//判断是否为空
                    System.out.println("服务端：" + str);
                }
                if (str.equals("quit")) {//判断是否退出
                    running = false;
                    ClientSend.setRunning();//关闭发送线程
                    break;
                }
            }
        }
        close();
    }

    /**
     * 关闭流
     */
    public static void close() {
        try {
            reader.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setRunning() {
        running = false;
    }
}