package cn.pangpython.jscp.client;

import cn.pangpython.jscp.log.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;


public class JscpClient {
    //    private static final Logger logger = Logger.getLogger(JscpClient.class.toString());
    private static final Logger logger = LogFactory.getGlobalLog();

    private  Integer port = 8088;
    private String serverIP;//服务器ip
    private String remoteFilePath;//请求的远程文件绝对路径
    private String localDir;//文件保存在本地的位置

    public JscpClient(Integer port,String serverIP, String remoteFilePath, String localDir) {
        this.port = port;
        this.serverIP = serverIP;
        this.remoteFilePath = remoteFilePath;
        this.localDir = localDir;
    }

    public static void main(String[] args) throws IOException {
        JscpClient jscpClient = new JscpClient(8088,"127.0.0.1","E:\\\\1.txt","F:\\\\");
        jscpClient.start();
    }

    public void start() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        //连接到服务器
        socketChannel.socket().connect(new InetSocketAddress(serverIP, port));
        ByteBuffer buffer = ByteBuffer.allocate(102400);
        buffer.clear();
        logger.info("正在请求" + remoteFilePath);
        System.out.println("正在请求"+remoteFilePath);
        //发送文件路径请求文件

        buffer.put(remoteFilePath.getBytes());
//        limit指针 移动到 position位置
        buffer.flip();
        // 当buffer中有足够空间，则写到buffer中
        while (buffer.hasRemaining()){
            socketChannel.write(buffer);
        }
        //关闭输出
        socketChannel.shutdownOutput();
        String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf(File.separator),remoteFilePath.length());
        //接收文件
        RandomAccessFile randomAccessFile = new RandomAccessFile(localDir+fileName, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        buffer.clear();
        int num = 0;
        while((num = socketChannel.read(buffer))>0){
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }
        if(num == -1){
            fileChannel.close();
        }
        System.out.println("文件接收完毕！");
        logger.info("******文件接收完毕！******");
        socketChannel.close();
    }
}
