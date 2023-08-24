package org.aleta.handler;

import java.nio.charset.StandardCharsets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    /**
     * Login Packets
     * All packets start with a single byte that identifies the packet type, the rest of the packet follows it. Please note that packets 0x09 through 0x13 are not documented (yet).
     */

    /**
     * Magic
     */
    private static final byte[] OFFLINE_MESSAGE_DATA_ID = {
            0x00,
            (byte) 0xff,
            (byte) 0xff,
            0x00,
            (byte) 0xfe,
            (byte) 0xfe,
            (byte) 0xfe,
            (byte) 0xfe,
            (byte) 0xfd,
            (byte) 0xfd,
            (byte) 0xfd,
            (byte) 0xfd,
            0x12,
            0x34,
            0x56,
            0x78
    };

    /**
     * Packet ID	Field Name	  Field Type	Example	            Notes
     * 0x01	        Ping ID	      int64	        0x00000000003c6d0d	Time since start in Milliseconds
     *              MAGIC	      MAGIC
     * Total Size:	25 Bytes
     */
    private static final byte ID_CONNECTED_PING_OPEN_CONNECTIONS = 0x01;

    private static final byte ID_OPEN_CONNECTION_REQUEST_1 = 0x05;

    /**
     * Packet ID	Field Name	   Field Type	Example	                     Notes
     * 0x1C	        Ping ID	       int64	    0x00000000003c6d0d	         Time since start in Milliseconds
     *              Server ID	   int64	    0x00000000372cdc9e	
     *              MAGIC	       MAGIC		
     *              Identifier	   string	    "MCPE;Steve;2 7;0.11.0;0;20" Used to send the username, format: MCPE;<Server name>;<Protocol version>;<MCPE Version>;<Players>;<Max Players>
     *               
     * Total Size:	35 Bytes + Server name length
     */
    private static final byte ID_UNCONNECTED_PING_OPEN_CONNECTIONS = 0x1C;

    /**
     * Types
     *        Size	Range	                            Notes
     * byte	  1	    -128 to 127	                        Signed, two's complement
     * short  2	    -32768 to 32767	                    Signed, two's complement
     * int32  4	    -2147483648 to 2147483647           Signed, two's complement
     * int64  8		Maybe a double?
     * MAGIC  16	0x00ffff00fefefefefdfdfdfd12345678	always those hex bytes, corresponding to RakNet's default OFFLINE_MESSAGE_DATA_ID
     * string =1	N/A	                                Prefixed by a short containing the length of the string in characters. It appears that only the following ASCII characters can be displayed: !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~
     */

    @Override
    protected void channelRead0(final ChannelHandlerContext context, final DatagramPacket message) throws Exception {
        var buffer = message.content();
        var packetId = buffer.readByte();

        switch(packetId) {
            case ID_CONNECTED_PING_OPEN_CONNECTIONS -> {
                log.info("Received ID_CONNECTED_PING_OPEN_CONNECTIONS");
                var pingId = buffer.readLongLE();
                var magic = buffer.readBytes(OFFLINE_MESSAGE_DATA_ID.length);
                log.info("Ping ID: {}", pingId);
                log.info("Magic: {}", magic);
                log.info("Sending ID_UNCONNECTED_PONG_OPEN_CONNECTIONS");
                buffer.writeByte(ID_UNCONNECTED_PING_OPEN_CONNECTIONS);
                buffer.writeLongLE(pingId);
                buffer.writeLongLE(0);
                buffer.writeBytes(OFFLINE_MESSAGE_DATA_ID);
                buffer.writeCharSequence("MCPE;Aleta;113 113;1.1.5;0;20", StandardCharsets.UTF_8);
                break;
            }
            case ID_OPEN_CONNECTION_REQUEST_1 -> {
                break;
            }
            default -> {
                log.info("Received unknown packet with id: {}", packetId);
            }

        }
    }

}
