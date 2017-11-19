package tw.zerator.ffs.pubsub.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import tv.zerator.ffs.pubsub.Server;
import tw.zerator.ffs.pubsub.network.packet.Packet;

import java.util.logging.Level;

public class ClientHandler extends SimpleChannelInboundHandler<Packet> {
	
	private final Server mServer;
	private ClientConnection mConnection;

    public ClientHandler(Server server) {
    	mServer = server;
	}

	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	mConnection = new ClientConnection(mServer, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    	if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) ctx.disconnect();
    	super.userEventTriggered(ctx, evt);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
    	mConnection.handle(packet);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Server.LOGGER.log(Level.SEVERE, "Encountered exception in pipeline for client at " + ctx.channel().remoteAddress() + "; disconnecting client.", cause);
        ctx.channel().close();
    }
}
