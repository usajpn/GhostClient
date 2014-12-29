package jp.ac.keio.sfc.ht.memsys.ghost.nqueen;
/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;

/**
 * HeapSortUtil
 * Created on 11/30/14.
 */
public class NQueenUtil {

    public static OffloadableData genData(String ID, String seq, int num){

        OffloadableData data = new OffloadableData(ID, seq);

//        double[] body = new double[10240];
//
//        Random rnd = new SecureRandom();
//        for(int i = 0; i<body.length; i++){
//            body[i] = rnd.nextDouble();
//        }
//        int num = new SecureRandom().nextInt(9);
//        int num = 12;
        data.putData(NQueenTaskKeys.DATA, num);

        return data;
    }
}
