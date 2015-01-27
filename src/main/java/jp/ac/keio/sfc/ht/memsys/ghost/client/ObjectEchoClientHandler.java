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
import jp.ac.keio.sfc.ht.memsys.ghost.cache.RemoteCacheContainer;
import jp.ac.keio.sfc.ht.memsys.ghost.image.ImageSample;
import jp.ac.keio.sfc.ht.memsys.ghost.image.SIFTUtil;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostRequestTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostResponseTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.*;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.util.Util;
import jp.ac.keio.sfc.ht.memsys.ghost.sift.*;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.util.concurrent.FutureListener;
import org.infinispan.commons.util.concurrent.NotifyingFuture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler implementation for the object echo client.  It initiates the
 * ping-pong traffic between the object echo client and server by sending the
 * first message to the server.
 */
public class ObjectEchoClientHandler extends ChannelInboundHandlerAdapter {
    RemoteCacheContainer cacheContainer = null;
    RemoteCache<String, OffloadableData> mDataCache;
    RemoteCache<String, OffloadableTask> mTaskCache;
    RemoteCache<String, OffloadableData> mResultCache;

    private final GhostRequest mes;
    private String appId;
    private String taskId;
    private String cacheIP;
    private final String taskName = "SIFT";
    private long start;
    private long mid;
    private long end;


    // constants for SIFT
    private int steps = 5;
    private float initial_sigma = 1.6f;
    private int fdsize = 4;
    private int fdbins = 8;
    private int min_size = 64;
    private int max_size = 1024;
    private AtomicInteger counter;
    private int octNum = 0;
    private ImageSample imageContainer;

    // for all results
    private Vector<Feature> features = new Vector<Feature>();
    private String fileName;
    private long time;

    /**
     * Creates a client-side handler.
     */
    public ObjectEchoClientHandler(String ID, AtomicInteger c, String fn, String size, long t) {
        Bundle bundle = new Bundle();
        bundle.putData(BundleKeys.APP_NAME, "SIFTAPP" + ID);
        mes = new GhostRequest(GhostRequestTypes.INIT, bundle);
        counter = c;
        fileName = fn;
        imageContainer = new ImageSample(size);
        this.time = t;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        mid = System.currentTimeMillis();
        ctx.writeAndFlush(mes);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, UnknownHostException, ExecutionException {
        GhostResponse in = (GhostResponse) msg;
        GhostRequest req = null;
        if (in.STATUS.equals(GhostResponseTypes.SUCCESS)) {
            if (in.REQUESTID.equals(GhostRequestTypes.INIT)) {
                // save appid and taskid
                appId = in.MESSAGE.getData(BundleKeys.APP_ID);
                taskId = Util.taskPathBuilder(appId, taskName);
                cacheIP = in.MESSAGE.getData(BundleKeys.IP_ADDR);
//                System.out.println(cacheIP);
//                System.out.println("App ID:" + appId);
//                System.out.println("Task ID:" + taskId);

                // initialize cache from ip address
                cacheContainer = RemoteCacheContainer.getInstance(cacheIP);
                mDataCache = cacheContainer.getCache(CacheKeys.DATA_CACHE);
                mTaskCache = cacheContainer.getCache(CacheKeys.TASK_CACHE);
                mResultCache = cacheContainer.getCache(CacheKeys.RESULT_CACHE);

                OffloadableTask task = new SIFTTask();
                // Cache Task
                mTaskCache.put(taskId, task);

                // Register Task
                Bundle bundle = new Bundle();
                bundle.putData(BundleKeys.APP_ID, appId);
                bundle.putData(BundleKeys.TASK_ID, taskId);
                req = new GhostRequest(GhostRequestTypes.REGISTERTASK, bundle);

            } else if (in.REQUESTID.equals(GhostRequestTypes.REGISTERTASK)) {
                // SIFT Algorithm
                BufferedImage image = imageContainer.getImage();
                int[] pixels = SIFTUtil.getPixelsTab(image);
                FloatArray2D fa = SIFT.ArrayToFloatArray2D(image.getWidth(), image.getHeight(), pixels);

                final FloatArray2DSIFT sift = new FloatArray2DSIFT(fdsize, fdbins);
                Filter.enhance(fa, 1.0f);

                fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 0.25));
                sift.init(fa, steps, initial_sigma, min_size, max_size);

                octNum = sift.getOctaves().length;
                counter.set(octNum);
                start = System.currentTimeMillis();

                ExecutorService es = Executors.newCachedThreadPool();

                final ChannelHandlerContext responseCtx = ctx;
                for (int o=0; o<octNum; o++) {
                    final int oct = o;
                    es.execute(new Runnable() {
                        @Override
                        public void run() {
                            String seq = String.valueOf(oct);
                            //                    System.out.println(seq);
                            OffloadableData data = new OffloadableData(taskId, seq);
                            FloatArray2DScaleOctave octave = sift.getOctave(oct);
                            //                    System.out.println(octave.build());
                            data.putData("OCTAVE", octave);

                            data.putData("SEQ", seq);
                            //                    System.out.println(data.getData("OCTAVE"));
                            String path = Util.dataPathBuilder(taskId, seq);
                            long s = System.currentTimeMillis();
                            mDataCache.put(path, data);
//                            System.out.println("cache time:" + (System.currentTimeMillis() - s));
                            //                    System.out.println(path);
                        }
                    });
                }
                es.shutdown();
                boolean finshed = es.awaitTermination(10, TimeUnit.SECONDS);

                for (int o=0; o<octNum; o++) {
                    String seq = String.valueOf(o);
                    Bundle bundle = new Bundle();
                    bundle.putData(BundleKeys.APP_ID, appId);
                    bundle.putData(BundleKeys.TASK_ID, taskId);
                    bundle.putData(BundleKeys.DATA_SEQ, seq);

                    GhostRequest request = new GhostRequest(GhostRequestTypes.EXECUTE, bundle);
                    responseCtx.write(request);
                }
                req = null;

            } else if (in.REQUESTID.equals(GhostRequestTypes.EXECUTE)) {
                counter.getAndDecrement();
                octNum = octNum - 1;
                final String resultSeq = in.MESSAGE.getData(BundleKeys.DATA_SEQ);
                System.out.println(resultSeq);
                while(mResultCache.get(Util.dataPathBuilder(taskId, resultSeq)) == null) {
                    Thread.sleep(10);
                }

                OffloadableData feature = mResultCache.get(Util.dataPathBuilder(taskId, resultSeq));
                features.addAll((Vector<Feature>)feature.getData("FEATURE"));
                if (counter.get() == 0) {
                    end = System.currentTimeMillis();
                    System.out.println("TIME: " + (end - start));
                    ctx.close();
                }

//                final NotifyingFuture f = mResultCache.getAsync(Util.dataPathBuilder(taskId, resultSeq));
                /*
                f.attachListener(new FutureListener<OffloadableData>() {
                    @Override
                    public void futureDone(Future<OffloadableData> future) {
                        try {
                            System.out.println("hoge");
                            System.out.println((OffloadableData)future.get());
                            features.addAll((Vector<Feature>) future.get().getData("FEATURE"));
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }

                        if (counter.get() == 0) {
                            end = System.currentTimeMillis();
                            //                  System.out.println(start - time);
                            System.out.println((end - start));
                            //                   imageContainer.showImage(features);
                            try {
                                File file = new File(fileName);
                                FileWriter filewriter = new FileWriter(file, true);
                                filewriter.write(String.valueOf(end - time) + "\n");
                                filewriter.close();
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            System.exit(0);

                        }
                    }
                });
                */
//                ctx.flush();

//                OffloadableData feature = mResultCache.getAsync(Util.dataPathBuilder(taskId, resultSeq)).get();
//                features.addAll((Vector<Feature>) feature.getData("FEATURE"));
//                System.out.println(counter.get() + " " + String.valueOf(System.currentTimeMillis() - start));
//                ctx.close();

            } else {
                // do nothing
            }
        }
        else {
            System.out.println("SOME KIND OF FAILURE...: " + in.STATUS);
        }

        // send request
        if (req != null) {
            ctx.write(req);
        }
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
