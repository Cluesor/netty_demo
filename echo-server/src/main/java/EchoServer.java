import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 1){
            System.err.println("Usage:" + EchoServer.class.getSimpleName() + "<port>");
            return;
        }
        int port =Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws Exception{
        final EchoServerhandler serverhandler = new EchoServerhandler();
        EventLoopGroup group = new NioEventLoopGroup();  // 创建EventLoopGroup

        try {
            ServerBootstrap b = new ServerBootstrap();  //创建ServerBootstrap ,以引导和绑定服务器
            b.group(group).
                    channel(NioServerSocketChannel.class).   // 使用所使用的NIO传输Channel
                    localAddress(new InetSocketAddress(port)).  // 使用指定的端口设置套接字地址
                    childHandler(new ChannelInitializer<SocketChannel>() {   // 添加一个EchoServerHandler到子Channel的ChannelPipeline

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(serverhandler);   // EchoServerhandler被标注为@Sharable，所以我们总是可以使用同样的实例
                }
            });
            ChannelFuture f = b.bind().sync();   // 异步绑定服务器，调用sync()方法阻塞等待直到绑定完成
            f.channel().closeFuture().sync();   //  获取Channel的CloseFuture，并且阻塞当前线程直到它完成

        }finally {
            group.shutdownGracefully().sync();  // 关闭EventLoopGroup，释放所有的资源
        }
    }

}
