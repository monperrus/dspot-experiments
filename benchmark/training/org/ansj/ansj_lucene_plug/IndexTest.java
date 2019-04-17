package org.ansj.ansj_lucene_plug;


import DicLibrary.DEFAULT;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import org.ansj.library.DicLibrary;
import org.ansj.lucene7.AnsjAnalyzer.TYPE;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.tartarus.snowball.ext.PorterStemmer;


public class IndexTest {
    @Test
    public void testQuery() throws IOException {
        Analyzer ca = new org.ansj.lucene7.AnsjAnalyzer(TYPE.query_ansj);
        String content = "\n\n\n\n\n\n\n\u6211\u4ece\u5c0f\u5c31\u4e0d\u7531\u81ea\u4e3b\u5730\u8ba4\u4e3a\u81ea\u5df1\u957f\u5927\u4ee5\u540e\u4e00\u5b9a\u5f97\u6210\u4e3a\u4e00\u4e2a\u8c61\u6211\u7236\u4eb2\u4e00\u6837\u7684\u753b\u5bb6, \u53ef\u80fd\u662f\u7236\u6bcd\u6f5c\u79fb\u9ed8\u5316\u7684\u5f71\u54cd\u3002\u5176\u5b9e\u6211\u6839\u672c\u4e0d\u77e5\u9053\u4f5c\u4e3a\u753b\u5bb6\u610f\u5473\u7740\u4ec0\u4e48\uff0c\u6211\u662f\u5426\u559c\u6b22\uff0c\u6700\u91cd\u8981\u7684\u662f\u5426\u9002\u5408\u6211\uff0c\u6211\u662f\u5426\u6709\u8fd9\u4e2a\u624d\u534e\u3002\u5176\u5b9e\u4eba\u5230\u4e2d\u5e74\u7684\u6211\u8fd8\u662f\u4e0d\u786e\u5b9a\u6211\u6700\u559c\u6b22\u4ec0\u4e48\uff0c\u6700\u60f3\u505a\u7684\u662f\u4ec0\u4e48\uff1f\u6211\u76f8\u4fe1\u5f88\u591a\u4eba\u548c\u6211\u4e00\u6837\u6709\u540c\u6837\u7684\u70e6\u607c\u3002\u6bd5\u7adf\u4e0d\u662f\u6bcf\u4e2a\u4eba\u90fd\u80fd\u6210\u4e3a\u4f5c\u6587\u91cc\u7684\u5b87\u822a\u5458\uff0c\u79d1\u5b66\u5bb6\u548c\u5927\u6559\u6388\u3002\u77e5\u9053\u81ea\u5df1\u9002\u5408\u505a\u4ec0\u4e48\uff0c\u559c\u6b22\u505a\u4ec0\u4e48\uff0c\u80fd\u505a\u597d\u4ec0\u4e48\u5176\u5b9e\u662f\u4e2a\u975e\u5e38\u56f0\u96be\u7684\u95ee\u9898\u3002" + ((("????????????????????????????????????????????????????????????????????????????????????????????????????????????????" + "???????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????????????????2??????????????????") + "?????????????") + "??????????? ?????????????????????????????????????????????????????????????????????????????????????????????  ????????????????????????? ???????????????????????????????????????????????????????????");
        TokenStream ts = ca.tokenStream(content, new StringReader(content));
        System.out.println(("start: " + (new Date())));
        long before = System.currentTimeMillis();
        while (ts.incrementToken()) {
            System.out.println(ts.getAttribute(CharTermAttribute.class));
        } 
        ts.close();
        long now = System.currentTimeMillis();
        System.out.println((("time: " + ((now - before) / 1000.0)) + " s"));
    }

    @Test
    public void testDic() throws IOException {
        DicLibrary.put(DEFAULT, "../../library/default.dic");
        Analyzer ca = new org.ansj.lucene7.AnsjAnalyzer(TYPE.dic_ansj);
        String content = "\n\n\n\n\n\n\n\u6211\u4ece\u5c0f\u5c31\u4e0d\u7531\u81ea\u4e3b\u5730\u8ba4\u4e3a\u81ea\u5df1\u957f\u5927\u4ee5\u540e\u4e00\u5b9a\u5f97\u6210\u4e3a\u4e00\u4e2a\u8c61\u6211\u7236\u4eb2\u4e00\u6837\u7684\u753b\u5bb6, \u53ef\u80fd\u662f\u7236\u6bcd\u6f5c\u79fb\u9ed8\u5316\u7684\u5f71\u54cd\u3002\u5176\u5b9e\u6211\u6839\u672c\u4e0d\u77e5\u9053\u4f5c\u4e3a\u753b\u5bb6\u610f\u5473\u7740\u4ec0\u4e48\uff0c\u6211\u662f\u5426\u559c\u6b22\uff0c\u6700\u91cd\u8981\u7684\u662f\u5426\u9002\u5408\u6211\uff0c\u6211\u662f\u5426\u6709\u8fd9\u4e2a\u624d\u534e\u3002\u5176\u5b9e\u4eba\u5230\u4e2d\u5e74\u7684\u6211\u8fd8\u662f\u4e0d\u786e\u5b9a\u6211\u6700\u559c\u6b22\u4ec0\u4e48\uff0c\u6700\u60f3\u505a\u7684\u662f\u4ec0\u4e48\uff1f\u6211\u76f8\u4fe1\u5f88\u591a\u4eba\u548c\u6211\u4e00\u6837\u6709\u540c\u6837\u7684\u70e6\u607c\u3002\u6bd5\u7adf\u4e0d\u662f\u6bcf\u4e2a\u4eba\u90fd\u80fd\u6210\u4e3a\u4f5c\u6587\u91cc\u7684\u5b87\u822a\u5458\uff0c\u79d1\u5b66\u5bb6\u548c\u5927\u6559\u6388\u3002\u77e5\u9053\u81ea\u5df1\u9002\u5408\u505a\u4ec0\u4e48\uff0c\u559c\u6b22\u505a\u4ec0\u4e48\uff0c\u80fd\u505a\u597d\u4ec0\u4e48\u5176\u5b9e\u662f\u4e2a\u975e\u5e38\u56f0\u96be\u7684\u95ee\u9898\u3002" + ((("????????????????????????????????????????????????????????????????????????????????????????????????????????????????" + "???????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????????????????2??????????????????") + "?????????????") + "??????????? ?????????????????????????????????????????????????????????????????????????????????????????????  ????????????????????????? ???????????????????????????????????????????????????????????");
        TokenStream ts = ca.tokenStream(content, new StringReader(content));
        System.out.println(("start: " + (new Date())));
        long before = System.currentTimeMillis();
        while (ts.incrementToken()) {
            System.out.println(ts.getAttribute(CharTermAttribute.class));
        } 
        ts.close();
        long now = System.currentTimeMillis();
        System.out.println((("time: " + ((now - before) / 1000.0)) + " s"));
    }

    @Test
    public void testIndex() throws IOException {
        Analyzer ca = new org.ansj.lucene7.AnsjAnalyzer(TYPE.index_ansj);
        String content = "\n\n\n\n\n\n\n\u6211\u4ece\u5c0f\u5c31\u4e0d\u7531\u81ea\u4e3b\u5730\u8ba4\u4e3a\u81ea\u5df1\u957f\u5927\u4ee5\u540e\u4e00\u5b9a\u5f97\u6210\u4e3a\u4e00\u4e2a\u8c61\u6211\u7236\u4eb2\u4e00\u6837\u7684\u753b\u5bb6, \u53ef\u80fd\u662f\u7236\u6bcd\u6f5c\u79fb\u9ed8\u5316\u7684\u5f71\u54cd\u3002\u5176\u5b9e\u6211\u6839\u672c\u4e0d\u77e5\u9053\u4f5c\u4e3a\u753b\u5bb6\u610f\u5473\u7740\u4ec0\u4e48\uff0c\u6211\u662f\u5426\u559c\u6b22\uff0c\u6700\u91cd\u8981\u7684\u662f\u5426\u9002\u5408\u6211\uff0c\u6211\u662f\u5426\u6709\u8fd9\u4e2a\u624d\u534e\u3002\u5176\u5b9e\u4eba\u5230\u4e2d\u5e74\u7684\u6211\u8fd8\u662f\u4e0d\u786e\u5b9a\u6211\u6700\u559c\u6b22\u4ec0\u4e48\uff0c\u6700\u60f3\u505a\u7684\u662f\u4ec0\u4e48\uff1f\u6211\u76f8\u4fe1\u5f88\u591a\u4eba\u548c\u6211\u4e00\u6837\u6709\u540c\u6837\u7684\u70e6\u607c\u3002\u6bd5\u7adf\u4e0d\u662f\u6bcf\u4e2a\u4eba\u90fd\u80fd\u6210\u4e3a\u4f5c\u6587\u91cc\u7684\u5b87\u822a\u5458\uff0c\u79d1\u5b66\u5bb6\u548c\u5927\u6559\u6388\u3002\u77e5\u9053\u81ea\u5df1\u9002\u5408\u505a\u4ec0\u4e48\uff0c\u559c\u6b22\u505a\u4ec0\u4e48\uff0c\u80fd\u505a\u597d\u4ec0\u4e48\u5176\u5b9e\u662f\u4e2a\u975e\u5e38\u56f0\u96be\u7684\u95ee\u9898\u3002" + ((("????????????????????????????????????????????????????????????????????????????????????????????????????????????????" + "???????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????????????????2??????????????????") + "?????????????") + "??????????? ?????????????????????????????????????????????????????????????????????????????????????????????  ????????????????????????? ???????????????????????????????????????????????????????????");
        TokenStream ts = ca.tokenStream(content, new StringReader(content));
        System.out.println(("start: " + (new Date())));
        long before = System.currentTimeMillis();
        while (ts.incrementToken()) {
            System.out.println(ts.getAttribute(CharTermAttribute.class));
        } 
        ts.close();
        long now = System.currentTimeMillis();
        System.out.println((("time: " + ((now - before) / 1000.0)) + " s"));
    }

    @Test
    public void indexTest() throws IOException, CorruptIndexException, ParseException, LockObtainFailedException {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new org.ansj.lucene7.AnsjAnalyzer(TYPE.index_ansj));
        Directory directory = null;
        IndexWriter iwriter = null;
        IndexWriterConfig ic = new IndexWriterConfig(analyzer);
        // ????????
        directory = new RAMDirectory();
        iwriter = new IndexWriter(directory, ic);
        addContent(iwriter, "??????????????????????????????????????????10????????????");
        addContent(iwriter, "??????????????????");
        addContent(iwriter, "?????");
        iwriter.commit();
        iwriter.close();
        System.out.println("??????");
        Analyzer queryAnalyzer = new org.ansj.lucene7.AnsjAnalyzer(TYPE.dic_ansj);
        System.out.println("index ok to search!");
        search(queryAnalyzer, directory, "\"\u548c\u670d\"");
    }

    @Test
    public void poreterTest() {
        PorterStemmer ps = new PorterStemmer();
        System.out.println(ps.stem());
    }
}
