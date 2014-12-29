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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jp.ac.keio.sfc.ht.memsys.ghost.nqueen.NQueenTaskImpl;
import jp.ac.keio.sfc.ht.memsys.ghost.nqueen.NQueenUtil;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostRequestTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostResponseTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.*;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.util.Util;
import org.infinispan.client.hotrod.RemoteCache;

/**
 * Handler implementation for the object echo client.  It initiates the
 * ping-pong traffic between the object echo client and server by sending the
 * first message to the server.
 */
public class ObjectEchoClientHandler extends ChannelInboundHandlerAdapter {
    RemoteCacheContainer cacheContainer = RemoteCacheContainer.getInstance();
    RemoteCache<String, OffloadableData> mDataCache = cacheContainer.getCache(CacheKeys.DATA_CACHE);
    RemoteCache<String, OffloadableTask> mTaskCache = cacheContainer.getCache(CacheKeys.TASK_CACHE);
    RemoteCache<String, OffloadableData> mResultCache  = cacheContainer.getCache(CacheKeys.RESULT_CACHE);

    //    private final List<Integer> firstMessage;
    private final GhostRequest mes;
    private String appId;
    private String taskId;
    private final String taskName = "NQUEEN";

    /**
     * Creates a client-side handler.
     */
    public ObjectEchoClientHandler() {
        Bundle bundle = new Bundle();
        bundle.putData(BundleKeys.APP_NAME, "NQUEENAPP");
        mes = new GhostRequest(GhostRequestTypes.INIT, bundle);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Send the first message if this handler is a client-side handler.
//        ctx.writeAndFlush(firstMessage);
        //INIT APP
//        ctx.writeAndFlush(mes);
        ctx.writeAndFlush(mes);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the server.
//        System.out.println(m);
        GhostResponse in = (GhostResponse) msg;
        GhostRequest req = null;
        System.out.println("HOGEHOGE");
        if (in.STATUS.equals(GhostResponseTypes.SUCCESS)) {
            if (in.REQUESTID.equals(GhostRequestTypes.INIT)) {
                // save appid and taskid
                appId = in.MESSAGE.getData(BundleKeys.APP_ID);
                taskId = Util.taskPathBuilder(appId, taskName);
                System.out.println("App ID:" + appId);
                System.out.println("Task ID:" + taskId);

                OffloadableTask task = new NQueenTaskImpl();
                // Cache Task
                mTaskCache.put(taskId, task);

                // Register Task
                Bundle bundle = new Bundle();
                bundle.putData(BundleKeys.APP_ID, appId);
                bundle.putData(BundleKeys.TASK_ID, taskId);
                req = new GhostRequest(GhostRequestTypes.REGISTERTASK, bundle);

            } else if (in.REQUESTID.equals(GhostRequestTypes.REGISTERTASK)) {
                // Cache Data
                String seq = "0";
                int num = 8;
                OffloadableData data = NQueenUtil.genData(taskId, seq, num);
                String path = Util.dataPathBuilder(taskId, seq);
                mDataCache.put(path, data);

                // Execute Task
                Bundle bundle = new Bundle();
                bundle.putData(BundleKeys.APP_ID, appId);
                bundle.putData(BundleKeys.TASK_ID, taskId);
                bundle.putData(BundleKeys.DATA_SEQ, seq);

                req = new GhostRequest(GhostRequestTypes.EXECUTE, bundle);
            } else if (in.REQUESTID.equals(GhostRequestTypes.EXECUTE)) {
                // TODO if SUCCESS

            } else {
                // do nothing
            }
        }
        else {
            System.out.println("SOME KIND OF FAILURE...: " + in.STATUS);
        }
        ctx.write(req);
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