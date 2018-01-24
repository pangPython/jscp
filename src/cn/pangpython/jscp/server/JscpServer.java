package cn.pangpython.jscp.server;


import cn.pangpython.jscp.log.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 服务器端
 *
 *
 */
public class JscpServer {
    //    private static final Logger logger = Logger.getLogger(JscpServer.class.toString());
    private static final Logger logger = LogFactory.getGlobalLog();

    private  Integer serverPort;
    private ByteBuffer buffer;
    private Selector selector;

    private Map<SelectionKey, FileChannel> fileMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        JscpServer jscpServer = new JscpServer(8088);
        jscpServer.listen();
    }

    public JscpServer(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public void init() {
        buffer = ByteBuffer.allocate(1024 * 1024);
        ServerSocketChannel serverSocketChannel;

        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(serverPort));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("********服务器已开启********");
            logger.info("********服务器已开启********");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void listen() throws IOException {

        while (true) {
            int num = selector.select();
            if (num == 0) continue;
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                handleKey(key);
            }
        }
    }

    /**
     * @param key
     */
    private void handleKey(SelectionKey key) throws IOException {
        SocketChannel socketChannel = null;
        String filePath = null;
        try {
            if (key.isAcceptable()) {
                ServerSocketChannel serverChannel1 = (ServerSocketChannel) key.channel();
                socketChannel = serverChannel1.accept();
                if (socketChannel == null) return;
                //设置非阻塞
                socketChannel.configureBlocking(false);
                //注册
                socketChannel.register(selector, SelectionKey.OP_READ);
                System.out.println(socketChannel.getRemoteAddress() + "连接成功...");
                logger.info(socketChannel.getRemoteAddress() + "连接成功...");
            } else if (key.isReadable()) {
                socketChannel = (SocketChannel) key.channel();
                //get file path
                int len = 0;
                byte[] bytes = null;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while ((len = socketChannel.read(buffer)) > 0) {
                    buffer.flip();
                    bytes = new byte[len];
                    buffer.get(bytes);
                    byteArrayOutputStream.write(bytes);
                    buffer.clear();
                }
                filePath = new String(byteArrayOutputStream.toByteArray()).trim();
                if (filePath != null && filePath != "" && filePath.length() != 0) {
                    System.out.println("get " + filePath);
                    logger.info("get " + filePath);
                    //发送文件
                    sendFile(filePath,socketChannel);

                    socketChannel.shutdownOutput();
                }
            }else {

                socketChannel.close();
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (socketChannel != null) {
                socketChannel.close();
            }
        }
    }

    /**
     *
     * send file to socketChannel
     *
     * @param filePath
     * @param socketChannel
     * @throws IOException
     */
    private void sendFile(String filePath, SocketChannel socketChannel) throws IOException {
        long startTime = System.currentTimeMillis();
        long fileSize = 0;
        ByteBuffer buffer = ByteBuffer.allocate(102400);
        if(filePath != null && filePath != "" && filePath.length() != 0) {
            System.out.println("filePath:" + filePath);
            logger.info("filePath:" + filePath);
            File file = new File(filePath);
            if(file.exists() && file.isFile()){
                fileSize = file.length();
                FileChannel fileChannel = new FileInputStream(file).getChannel();
                int num2 = 0;
                while ((num2=fileChannel.read(buffer)) >0) {
                    buffer.flip();
                    while (buffer.hasRemaining()){
                        socketChannel.write(buffer);
                    }
                    buffer.clear();
                }
                fileChannel.force(true);
                System.out.println("file " + filePath + " send success!");
                logger.info("file " + filePath + " send success!");
                if (num2 == -1) {
                    fileChannel.close();
//                socketChannel.shutdownOutput();
                }
            }

        }
        long endTime = System.currentTimeMillis();
        System.out.println("file " + filePath + " size:" + fileSize + " 耗时：" + (endTime - startTime) / 1000 + "s");
        logger.info("file " + filePath + " size:" + fileSize + " 耗时：" + (endTime - startTime) / 1000 + "s");
    }
}
