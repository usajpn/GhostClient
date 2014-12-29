package jp.ac.keio.sfc.ht.memsys.ghost.nqueen;

import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask;

/**
 * jp.ac.keio.sfc.ht.memsys.ghost.nqueen.NQueenTaskImpl
 * Created on 12/17/14.
 */
public class NQueenTaskImpl implements OffloadableTask {
    private static final String TASK_NAME = "NQUEEN";

    /***********************************************************************
     * Return true if queen placement q[n] does not conflict with
     * other queens q[0] through q[n-1]
     ***********************************************************************/
    public static boolean isConsistent(int[] q, int n) {
        for (int i = 0; i < n; i++) {
            if (q[i] == q[n])             return false;   // same column
            if ((q[i] - q[n]) == (n - i)) return false;   // same major diagonal
            if ((q[n] - q[i]) == (n - i)) return false;   // same minor diagonal
        }
        return true;
    }

    /***********************************************************************
     * Print out N-by-N placement of queens from permutation q in ASCII.
     ***********************************************************************/
    public static void printQueens(int[] q) {
        int N = q.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (q[i] == j) System.out.print("Q ");
                else           System.out.print("* ");
            }
            System.out.println();
        }
        System.out.println();
    }


    /***********************************************************************
     *  Try all permutations using backtracking
     ***********************************************************************/
    @Override
    public OffloadableData run(OffloadableData offloadableData) {
        int N = (Integer)offloadableData.getData("nqueen_data");
        int[] a = new int[N];
        enumerate(a, 0);

        return null;
    }

    public static void enumerate(int[] q, int n) {
        int N = q.length;
        if (n == N) /*printQueens(q)*/;
        else {
            for (int i = 0; i < N; i++) {
                q[n] = i;
                if (isConsistent(q, n)) enumerate(q, n+1);
            }
        }
    }


    @Override
    public String getName() {
        return TASK_NAME;
    }
}
