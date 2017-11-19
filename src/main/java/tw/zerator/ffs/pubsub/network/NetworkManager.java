package tw.zerator.ffs.pubsub.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import tv.zerator.ffs.pubsub.Server;
import tw.zerator.ffs.pubsub.network.packet.PacketRegistry;

import java.io.IOException;
import java.net.URISyntaxException;

public class NetworkManager {

    private final Server mServer;
    private EventLoopGroup mBossGroup;
    private EventLoopGroup mWorkerGroup;
    private EventExecutorGroup mExecutorGroup;
    private Channel mChannel;

    public NetworkManager(Server server) {
        this.mServer = server;
    }
    
    public void start() throws IOException, InterruptedException, URISyntaxException {
        // Server part
        mBossGroup = new NioEventLoopGroup();
        mWorkerGroup = new NioEventLoopGroup();
        mExecutorGroup = new DefaultEventExecutorGroup(Integer.parseInt(mServer.getConfig().getProperty("netty.executor_threads", "512")));
        
        ServerBootstrap b = new ServerBootstrap();
        b.group(mBossGroup, mWorkerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ClientInitializer(mServer, mExecutorGroup));

        int port = Integer.parseInt(mServer.getConfig().getProperty("server.port"));
        mChannel = b.bind(port).sync().channel();

        Server.LOGGER.info("Server started on port " + port + ".");
    }

    public boolean shutdown() {
        if (mChannel == null) {
            return false;
        }

        try {
            mChannel.close().sync();
            return true;
        } catch (InterruptedException ex) {
            return false;
        } finally {
            mBossGroup.shutdownGracefully();
            mWorkerGroup.shutdownGracefully();
            mExecutorGroup.shutdownGracefully();
        }
    }

    private static class ClientInitializer extends ChannelInitializer<SocketChannel> {

        private final Server mServer;
        private final EventExecutorGroup mExecutorGroup;

        public ClientInitializer(Server server, EventExecutorGroup executorGroup) {
            this.mServer = server;
            mExecutorGroup = executorGroup;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            try {
                ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                ch.config().setOption(ChannelOption.IP_TOS, 0x18);
            } catch (ChannelException ex) {
                // IP_TOS not supported by platform, ignore
            }
            ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);
            
            PacketRegistry r = new PacketRegistry();

            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, 30));
            ch.pipeline().addLast(new HttpServerCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(65536));
            ch.pipeline().addLast(new WebSocketHandler());
            ch.pipeline().addLast(new PacketDecoder(r));
            ch.pipeline().addLast(new PacketEncoder(r));
            ch.pipeline().addLast(mExecutorGroup, "serverHandler", new ClientHandler(mServer));
        }
    }
}
