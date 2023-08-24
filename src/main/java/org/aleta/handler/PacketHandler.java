package org.aleta.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.socket.DatagramPacket;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {

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

    private static final byte ID_UNCONNECTED_PING = 0x01;

    private static final byte ID_OPEN_CONNECTION_REQUEST_ONE = 0x05;

    private static final byte ID_UNCONNECTED_PONG = 0x1C;

    @Override
    protected void channelRead0(final ChannelHandlerContext context, final DatagramPacket message) throws Exception {

        var buf = message.content();

        switch (buf.readByte()) {
            case ID_UNCONNECTED_PING -> {
                log.info("UNCONNECTED_PING from {}", message.sender());
                var time = buf.readLong();
                var magic = buf.readBytes(OFFLINE_MESSAGE_DATA_ID.length);
                var clientGuid = buf.readLong();
                log.info(" Time: {}, Magic: {}, Client GUID: {}", time, ByteBufUtil.hexDump(magic), clientGuid);
                break;
            }
            case ID_OPEN_CONNECTION_REQUEST_ONE -> {
                log.info("OPEN_CONNECTION_REQUEST_ONE from {}", message.sender());
                var magic = buf.readBytes(OFFLINE_MESSAGE_DATA_ID.length);
                var protocol = buf.readByte();
                var mtu = buf.alloc().buffer(46 + (1432));
                log.info(" Magic: {}, Protocol: {}, Mtu: {}", ByteBufUtil.hexDump(magic), protocol, mtu);
                break;
            }
            default -> {
                log.info("Unknown packet ID: {}", buf.readByte());
                break;
            }
        }

    }

}
