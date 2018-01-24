package cn.pangpython.jscp.server;

import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 服务器端
 *
 *
 */
public class JscpServer {
    private  Integer serverPort;
    private ByteBuffer buffer = ByteBuffer.allocate(1024*1024);

    private Map<SelectionKey, FileChannel> fileMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        JscpServer jscpServer = new JscpServer(8088);
        jscpServer.start();
    }

    public JscpServer(Integer serverPort) {
        this.serverPort = serverPort;
    }
    public void start() throws IOException {
        //start server
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(serverPort));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器已开启...");
        while (true) {
            int num = selector.select();
            if (num == 0) continue;
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            String filePath = null;
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel1 = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverChannel1.accept();
                    if (socketChannel == null) continue;
                    //设置非阻塞
                    socketChannel.configureBlocking(false);
                    //注册
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println(socketChannel.getRemoteAddress() + "连接成功...");
                    //get file path
                    int len = 0;
                    byte[] bytes = new byte[1024];
                    buffer.clear();
                    while((len = socketChannel.read(buffer))!=-1){
                        buffer.flip();
                        buffer.get(bytes,0,len);
                        filePath = new String(bytes, 0 ,len);
                        System.out.println("get "+filePath);
                        buffer.clear();
                    }
                    //发送文件
                    sendFile(filePath,socketChannel);
                    socketChannel.shutdownOutput();
                }
                if (key.isReadable()){

                }
                if(key.isWritable()){

                }
                it.remove();
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
    private void sendFile(String filePath,SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(102400);
        if(filePath != null){
            System.out.printf("filePath:"+filePath);
            File file = new File(filePath);
            if(file.exists() && file.isFile()){
                FileChannel fileChannel = new FileInputStream(file).getChannel();
                int num2 = 0;
                while ((num2=fileChannel.read(buffer)) >0) {
                    buffer.flip();
                    while (buffer.hasRemaining()){
                        socketChannel.write(buffer);
                    }
                    buffer.clear();
                }
                if (num2 == -1) {
                    fileChannel.close();
//                socketChannel.shutdownOutput();
                }
            }

        }
    }

    private void writeToClient(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put((socketChannel.getRemoteAddress() + "连接成功").getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
    }
    private void readData(SelectionKey key) throws IOException  {
        FileChannel fileChannel = fileMap.get(key);
        buffer.clear();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int num = 0;
        try {
            while ((num = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                // 写入文件
                fileChannel.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return;
        }
        // 调用close为-1 到达末尾
        if (num == -1) {
            fileChannel.close();
            System.out.println("上传完毕");
            buffer.put((socketChannel.getRemoteAddress() + "上传成功").getBytes());
            buffer.clear();
            socketChannel.write(buffer);
            key.cancel();
        }
    }
}
