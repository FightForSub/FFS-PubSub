package tw.zerator.ffs.pubsub.network.packet;

import java.util.HashMap;
import java.util.Map;

import tw.zerator.ffs.pubsub.network.packet.inbound.PingPacket;
import tw.zerator.ffs.pubsub.network.packet.inbound.SubscribeUnsubscribePacket;
import tw.zerator.ffs.pubsub.network.packet.outbound.MessagePacket;
import tw.zerator.ffs.pubsub.network.packet.outbound.PongPacket;
import tw.zerator.ffs.pubsub.network.packet.outbound.ResponsePacket;

public class PacketRegistry {
    
    public final ProtocolDirection CLIENTBOUND = new ProtocolDirection("CLIENTBOUND");
    public final ProtocolDirection SERVERBOUND = new ProtocolDirection("SERVERBOUND");

    public PacketRegistry() {
    	CLIENTBOUND.registerPacket(Packet.Type.MESSAGE, MessagePacket.class);
    	CLIENTBOUND.registerPacket(Packet.Type.RESPONSE, ResponsePacket.class);
    	CLIENTBOUND.registerPacket(Packet.Type.PONG, PongPacket.class);
    	
    	SERVERBOUND.registerPacket(Packet.Type.SUBSCRIBE, SubscribeUnsubscribePacket.class);
    	SERVERBOUND.registerPacket(Packet.Type.UNSUBSCRIBE, SubscribeUnsubscribePacket.class);
    	SERVERBOUND.registerPacket(Packet.Type.PING, PingPacket.class);
    }
    
    public static class ProtocolDirection {

        private final Map<Packet.Type, Class<? extends Packet>> mPacketClasses = new HashMap<>();
        private final Map<Class<? extends Packet>, Packet.Type> mReverseMapping = new HashMap<>();
        private final String mName;

        public ProtocolDirection(String name) {
            this.mName = name;
        }

        public String getName() {
            return mName;
        }

        public void registerPacket(Packet.Type packetType, Class<? extends Packet> clazz) {
            if (mPacketClasses.containsKey(packetType)) {
                throw new IllegalArgumentException("Packet '" + packetType.name() + "' is already registered for " + this + "!");
            }

            if (mReverseMapping.containsKey(clazz)) {
                throw new IllegalArgumentException("Packet with class " + clazz + " is already registered for " + this + "!");
            }

            mPacketClasses.put(packetType, clazz);
            mReverseMapping.put(clazz, packetType);
        }

        public Packet.Type getPacketType(Class<? extends Packet> clazz) {
            return mReverseMapping.get(clazz);
        }

        public Class<? extends Packet> getPacketClass(Packet.Type type) {
            return mPacketClasses.get(type);
        }

        public Packet constructPacket(Packet.Type type) {
            Class<? extends Packet> clazz = getPacketClass(type);
            if (clazz == null) {
                return null;
            }

            try {
                return clazz.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String toString() {
            return "ProtocolDirection{" + "name=" + mName + '}';
        }
    }
}
