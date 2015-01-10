package jp.ac.keio.sfc.ht.memsys.ghost.client;/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public final class ObjectEchoClient implements Runnable {

//    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final String HOST = System.getProperty("host", "133.27.171.12");
    static final int PORT = Integer.parseInt(System.getProperty("port", "2555"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
    private String num = "";
    private int queenNum = 0;

    public ObjectEchoClient(String n, int qn) {
        this.num = n;
        this.queenNum = qn;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java -jar EXECUTABLE [queenNum] [clientNum]");
            System.exit(0);
        }

        int queenNum = Integer.parseInt(args[0]);
        int clientNum = Integer.parseInt(args[1]);

        for (int i=0; i<clientNum; i++) {
            Thread t = new Thread(new ObjectEchoClient(String.valueOf(i), queenNum));
            t.start();
            Thread.sleep(10);
        }
    }

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEchoClientHandler(num, queenNum));
                        }
                    });

            // Start the connection attempt.
            b.connect(HOST, PORT).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
