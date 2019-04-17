package io.mycat.route.function;


import junit.framework.Assert;
import org.junit.Test;


/**
 * ?????????????
 *
 * @author XiaoSK
 */
public class PartitionByJumpConsistentHashTest {
    @Test
    public void test() {
        int[] expect = new int[]{ 1, 2, 1, 0, 0, 2, 1, 1, 1, 0, 2, 1, 1, 2, 1, 0, 0, 2, 1, 0, 0, 0, 2, 1 };
        PartitionByJumpConsistentHash jch = new PartitionByJumpConsistentHash();
        jch.setTotalBuckets(3);
        jch.init();
        for (int i = 1; i <= (expect.length); i++) {
            Assert.assertEquals(true, ((expect[(i - 1)]) == (jch.calculate((i + "")))));
        }
    }
}
