package cn.hutool.dfa.test;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.dfa.WordTree;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


/**
 * DFA????
 *
 * @author Looly
 */
public class DfaTest {
    // ????????
    String text = "????????????";

    @Test
    public void matchAllTest() {
        // ?????
        WordTree tree = buildWordTree();
        // -----------------------------------------------------------------------------------------------------------------------------------
        // ?????????????????????????????
        // ??????????????????????????
        // ??????????????????????????????????????????????????????????????
        List<String> matchAll = tree.matchAll(text, (-1), false, false);
        Assert.assertEquals(matchAll, CollectionUtil.newArrayList("?", "??", "???"));
    }

    /**
     * ??????????????
     */
    @Test
    public void densityMatchTest() {
        // ?????
        WordTree tree = buildWordTree();
        // -----------------------------------------------------------------------------------------------------------------------------------
        // ????????????????????????
        // ???????????????????????????????
        // ????????????????????????????
        List<String> matchAll = tree.matchAll(text, (-1), true, false);
        Assert.assertEquals(matchAll, CollectionUtil.newArrayList("?", "??", "???", "??"));
    }

    /**
     * ????????
     */
    @Test
    public void greedMatchTest() {
        // ?????
        WordTree tree = buildWordTree();
        // -----------------------------------------------------------------------------------------------------------------------------------
        // ???????????????????????
        // ???????????????????????????
        // ?????????????????????????????????????
        List<String> matchAll = tree.matchAll(text, (-1), false, true);
        Assert.assertEquals(matchAll, CollectionUtil.newArrayList("?", "???", "???"));
    }

    /**
     * ?????????????????????
     */
    @Test
    public void densityAndGreedMatchTest() {
        // ?????
        WordTree tree = buildWordTree();
        // -----------------------------------------------------------------------------------------------------------------------------------
        // ???????????????????????????????
        // ?????????????????????????????????????????????????
        // ????????????????????????????
        List<String> matchAll = tree.matchAll(text, (-1), true, true);
        Assert.assertEquals(matchAll, CollectionUtil.newArrayList("?", "???", "??", "???", "??"));
    }

    /**
     * ?????
     */
    @Test
    public void stopWordTest() {
        WordTree tree = new WordTree();
        tree.addWord("tio");
        List<String> all = tree.matchAll("AAAAAAAt-ioBBBBBBB");
        Assert.assertEquals(all, CollectionUtil.newArrayList("t-io"));
    }
}
