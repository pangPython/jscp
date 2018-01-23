package cn.pangpython.jscp;

import cn.pangpython.jscp.client.JscpClient;
import cn.pangpython.jscp.server.JscpServer;

import java.io.IOException;

/**
 *
 * java nio 实现 类似scp功能
 *  端口暂时使用
 */
public class Main {
    private static final Integer port = 8099;
    public static void main(String[] args) {
        //如果有命令行参数说明是客户端 启动客户端模式
        if(args.length > 0){
            String serverIP = args[0];
            String remotePath = args[1];
            String localDir = args[2];

            JscpClient jscpClient = new JscpClient(port,serverIP,remotePath,localDir);
            try {
                jscpClient.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //没有命令行数 启动服务端模式
            JscpServer jscpServer = new JscpServer(port);
            try {
                jscpServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
