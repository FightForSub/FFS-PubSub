package tw.zerator.ffs.pubsub.rabbitmq;

import alexmog.apilib.rabbitmq.packets.Packet;
import io.netty.buffer.ByteBuf;

public class TopicPacket extends Packet {
	public String topic;
	public String message;

	public TopicPacket() {
		packetId = 1;
	}
	
	@Override
	public void writeData(ByteBuf buf) {
		writeUTF8(buf, topic);
		writeUTF8(buf, message);
	}

	@Override
	public void readData(ByteBuf buf) {
		topic = readUTF8(buf);
		message = readUTF8(buf);
	}

}
