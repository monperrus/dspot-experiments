package org.ansj.recognition.arrimpl;


import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.junit.Test;


/**
 * Created by Ansj on 26/09/2017.
 */
public class NumRecognitionTest {
    @Test
    public void test() {
        MyStaticValue.isRealName = false;
        System.out.println(ToAnalysis.parse("???????????"));
        System.out.println(ToAnalysis.parse("??2323????"));
        System.out.println(ToAnalysis.parse("??2323????"));
        System.out.println(ToAnalysis.parse("??2016?"));
        System.out.println(ToAnalysis.parse("??????????AN TPY-2?????"));
        System.out.println(ToAnalysis.parse("2\u4e2d\u6027\u7c92\u7ec6\u80de\u767e\u5206\u6570NEUT%70.2040.00--75.00%\n").toString("\t"));
        System.out.println(ToAnalysis.parse("??????5??????????2?11?"));// #164

        System.out.println(ToAnalysis.parse("??????????????????123456 "));
        System.out.println(ToAnalysis.parse("??????????????????123456???? "));
        System.out.println(ToAnalysis.parse("?????3.???"));
        System.out.println(ToAnalysis.parse("???????????????"));
        System.out.println(ToAnalysis.parse("???????????????"));
        System.out.println(ToAnalysis.parse("6 666"));
        System.out.println(ToAnalysis.parse("905200.00 ????? ?????????? ?????????????"));
        System.out.println(ToAnalysis.parse("2017?2?12??????????????1985?8?28?"));
        System.out.println(ToAnalysis.parse("http://www.ansj-sun123.23.423.com?????"));
        System.out.println(ToAnalysis.parse("????????"));
        System.out.println(ToAnalysis.parse("????????"));
        System.out.println(ToAnalysis.parse("??????????????"));
        System.out.println(ToAnalysis.parse("??????????????"));
        System.out.println(ToAnalysis.parse("???????!"));
        System.out.println(ToAnalysis.parse("??????"));
        System.out.println(ToAnalysis.parse("12,345.60?"));
        System.out.println(ToAnalysis.parse("3.2??"));
        System.out.println(ToAnalysis.parse("pm2.5??????20mg"));
    }
}
