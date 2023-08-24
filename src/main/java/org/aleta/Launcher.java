package org.aleta;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.aleta.handler.PacketHandler;

import io.netty.bootstrap.Bootstrap;

import io.netty.channel.ChannelOption;

import io.netty.channel.nio.NioEventLoopGroup;

import io.netty.channel.socket.nio.NioDatagramChannel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Launcher {

    public static void main(String[] args) {
        if (args.length != 2) {
            log.error("Usage: java -jar aleta.jar <host> <port>");
            System.exit(1);
        }
        var eventLoop = new NioEventLoopGroup();
        var bootstrap = new Bootstrap().group(eventLoop)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new PacketHandler());

        try {
            bootstrap.bind(InetAddress.getByName(args[0]), Integer.parseInt(args[1]))
                    .sync()
                    .channel()
                    .closeFuture()
                    .await();
        } catch (NumberFormatException | UnknownHostException | InterruptedException e) {
            log.error("Failed to start Aleta server", e);
        } finally {
            eventLoop.shutdownGracefully();
        }
        log.info("Aleta server started on {}:{}", args[0], args[1]);
    }

}
