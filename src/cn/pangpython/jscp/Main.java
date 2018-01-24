package cn.pangpython.jscp;

import cn.pangpython.jscp.client.JscpClient;
import cn.pangpython.jscp.log.LogFactory;
import cn.pangpython.jscp.server.JscpServer;

import java.io.IOException;
import java.util.logging.Logger;


/**
 *
 *
 * java nio 实现 类似scp功能
 *  端口暂时使用8099
 */
public class Main {
    private static final Logger logger = LogFactory.getGlobalLog();
    private static final Integer port = 8099;
    public static void main(String[] args) {
        //如果有命令行参数说明是客户端 启动客户端模式
        if(args.length > 0){
            logger.info("*****程序运行在客户端模式 端口:" + port + "*****");
            String serverIP = args[0];
            String remotePath = args[1];
            String localDir = args[2];
            logger.info("serverIP:" + serverIP + " remotePath:" + remotePath + " localDir:" + localDir);

            JscpClient jscpClient = new JscpClient(port,serverIP,remotePath,localDir);
            try {
                jscpClient.start();
            } catch (IOException e) {
                e.printStackTrace();
                logger.info(e.getMessage());
            }
        }else{
            logger.info("*****程序运行在服务端模式 端口:" + port + "*****");
            //没有命令行数 启动服务端模式
            JscpServer jscpServer = new JscpServer(port);
            jscpServer.init();
            try {
                jscpServer.listen();
            } catch (IOException e) {
                e.printStackTrace();
                logger.info(e.getMessage());
            }
        }
    }
}
