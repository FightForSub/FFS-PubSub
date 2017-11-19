package tw.zerator.ffs.pubsub.network.packet.outbound;

import tw.zerator.ffs.pubsub.network.packet.Packet;

public class ResponsePacket extends Packet {
	public String nonce;
	public Error error;
	
	public ResponsePacket(String nonce, Error error) {
		this.nonce = nonce;
		this.error = error;
	}
	
	public enum Error {
		ERR_BADMESSAGE,
		ERR_BADAUTH,
		ERR_SERVER,
		ERR_BADTOPIC
	}
}
