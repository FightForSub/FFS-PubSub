package tw.zerator.ffs.pubsub.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import tv.zerator.ffs.pubsub.Server;
import tw.zerator.ffs.pubsub.network.exceptions.UnknownPacketException;
import tw.zerator.ffs.pubsub.network.packet.Packet;
import tw.zerator.ffs.pubsub.network.packet.PacketRegistry;

import java.util.List;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.mrbean.MrBeanModule;

public class PacketDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    private PacketRegistry mReg;
	private static final ObjectMapper mMapper;
    
	static {
		mMapper = new ObjectMapper().enable(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY).enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
		        .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL).setVisibility(JsonMethod.FIELD, Visibility.ANY)
		        /*.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)*/;
	    mMapper.registerModule(new MrBeanModule());
	    mMapper.disableDefaultTyping();
	}
	
    public PacketDecoder(PacketRegistry reg) {
        this.mReg = reg;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        String payload = frame.content().toString(CharsetUtil.UTF_8);
        if (payload.length() == 0) return;

        Packet p = mMapper.readValue(payload, Packet.class);
        Class<? extends Packet> clazz = mReg.SERVERBOUND.getPacketClass(p.type);

        if (clazz == null) throw new UnknownPacketException("Unknown packet: " + p.type.name());
        
        Packet packet = mMapper.readValue(payload, clazz);


        Server.LOGGER.finest("Received packet '" + p.type.name() + "' (" + packet.getClass().getSimpleName() + ") from " + ctx.channel().remoteAddress());

        out.add(packet);
    }

}
