package tw.zerator.ffs.pubsub.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import tv.zerator.ffs.pubsub.Server;
import tw.zerator.ffs.pubsub.network.packet.Packet;
import tw.zerator.ffs.pubsub.network.packet.PacketRegistry;

import java.util.List;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.mrbean.MrBeanModule;

public class PacketEncoder extends MessageToMessageEncoder<Packet> {
    PacketRegistry mReg;
	private static final ObjectMapper mMapper;
    
	static {
		mMapper = new ObjectMapper().enable(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY).enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
		        .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL).setVisibility(JsonMethod.FIELD, Visibility.ANY);
	    mMapper.registerModule(new MrBeanModule());
	    mMapper.disableDefaultTyping();
	}
    
    public PacketEncoder(PacketRegistry r) {
        this.mReg = r;
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, @SuppressWarnings("rawtypes") List out) throws Exception {
        Packet.Type packetType = mReg.CLIENTBOUND.getPacketType(packet.getClass());
        if (packetType == null) throw new IllegalArgumentException("Provided packet is not registered as a clientbound packet!");
        
        packet.type = packetType;
        byte[] buffer = mMapper.writeValueAsBytes(packet);
        if (buffer.length == 0) return;
        
        out.add(new TextWebSocketFrame(Unpooled.wrappedBuffer(buffer)));
        
        Server.LOGGER.finest("Sent packet '" + packetType + "' (" + packet.getClass().getSimpleName() + ") to " + ctx.channel().remoteAddress());
    }

}
