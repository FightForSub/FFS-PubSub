package tw.zerator.ffs.pubsub.network.packet.outbound;

import tw.zerator.ffs.pubsub.network.packet.Packet;

public class MessagePacket extends Packet {
	public Data data = new Data();
	public long timestamp;
	
	public MessagePacket(String topic, String message, long timestamp) {
		data.topic = topic;
		data.message = message;
		this.timestamp = timestamp;
	}
	
	public static class Data {
		public String topic;
		public String message;
	}
}
