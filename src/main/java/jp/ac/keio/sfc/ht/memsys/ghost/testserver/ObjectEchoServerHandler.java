package jp.ac.keio.sfc.ht.memsys.ghost.testserver;/*
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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostRequestTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostResponseTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.Bundle;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.BundleKeys;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.GhostRequest;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.GhostResponse;

import java.util.List;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class ObjectEchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the client.
//        ctx.write(msg);
//        List<Integer> m = (List<Integer>) msg;
//        System.out.println(m);

        GhostRequest m = (GhostRequest)msg;
        GhostResponse res = null;
        if (m.TYPE.equals(GhostRequestTypes.INIT)) {
//            String appId = gateway.registerApplication(m.PARAMS.getData(BundleKeys.APP_NAME));
            String appId = "UEEEEEEEEEI";
            Bundle bundle = new Bundle();
            bundle.putData(BundleKeys.APP_ID, appId);
            res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.INIT, bundle);
        } else if (m.TYPE.equals(GhostRequestTypes.REGISTERTASK)) {
            System.out.println(m.TASK);
        }
        ctx.write(res);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
