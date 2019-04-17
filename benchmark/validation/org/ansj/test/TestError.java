package org.ansj.test;


import DicLibrary.DEFAULT;
import org.ansj.domain.Term;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.SynonymsLibrary;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.junit.Test;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;


public class TestError {
    @Test
    public void test() throws Exception {
        // 
        // Forest forest = new Forest();
        // 
        // Library.insertWord(forest, "????\t10\t10");
        // Library.insertWord(forest, "??\t10	10");
        // Library.insertWord(forest, "???	10	10");
        // Library.insertWord(forest, "??	10	10");
        // 
        // DicLibrary.put(DicLibrary.DEFAULT, DicLibrary.DEFAULT, forest);
        // 
        // Result re = DicAnalysis.parse("????????????");
        // System.out.println(re);
        // 
        // DicLibrary.insert(DicLibrary.DEFAULT,"????????neut%", "clear", 2000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"????????neut", "clear", 2000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"neut%", "clear", 2000);
        // 
        // String str = "2????????NEUT%70.2040.00--75.00%";
        // System.out.println(ToAnalysis.parse(str));
        // 
        // LearnTool tool = new LearnTool();
        // System.out.println(NlpAnalysis.parse("????????????"));
        // System.out.println(NlpAnalysis.parse("?????????????????????_????"));
        // System.out.println(NlpAnalysis.parse("???????????"));
        // System.out.println(ToAnalysis.parse("????"));
        // 
        // DicLibrary.insert(DicLibrary.DEFAULT,"??", "n", 1000);
        // 
        // //		System.out.println(ToAnalysis.parse("????"));
        // System.out.println("aa");
        // System.out.println(ToAnalysis.parse("999? ????? 10g*9?  ?????????????????"));
        // 
        // List<Term> parse = ToAnalysis.parse("?????").getTerms();
        // System.out.println(parse);
        // 
        // System.out.println(IndexAnalysis.parse("???? ????"));
        // 
        // System.out.println(ToAnalysis.parse("????????????????????24???????????????"));
        // System.out.println(ToAnalysis.parse("365?????-???"));
        // System.out.println(NlpAnalysis.parse("????????????"));
        // System.out.println(NlpAnalysis.parse("?????????????????????_????"));
        // System.out.println(NlpAnalysis.parse("???????????"));
        // System.out.println(NlpAnalysis.parse("?????"));
        // System.out.println(NlpAnalysis.parse("????1.???????????????????1.?????????? "));
        // System.out
        // .println(NlpAnalysis
        // .parse("?????????????????????PersonService???name????????Spring?????????????PersonService????????????????????Spring???????????????????????????????????????????????IoC????????????????Spring???????????????????......"));
        // System.out
        // .println(NlpAnalysis
        // .parse("??????25????1989??????????????????????????-?????????1990?????????????????????????????????????????????1995??42%???????????????14%?????2014???87%http://t.cn/8F1g3Mv"));
        // System.out.println(NlpAnalysis.parse("?????????????????????"));
        // System.out.println(NlpAnalysis.parse("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"));
        // 
        // System.out.println(ToAnalysis.parse("????????????????"));
        // 
        // System.out.println(CrfLibrary.get().cohesion("??"));
        // System.out.println(CrfLibrary.get().cohesion("??"));
        // 
        // // ????
        // 
        // AmbiguityLibrary.insert(AmbiguityLibrary.DEFAULT,"???", "n");
        // 
        // AmbiguityLibrary.insert(AmbiguityLibrary.DEFAULT,"???", "nt", "??", "j");
        // 
        // System.out.println(NlpAnalysis.parse("????????"));
        // ;
        // System.out.println(NlpAnalysis.parse("????????????"));
        // ;
        // System.out.println(NlpAnalysis.parse("???????"));
        // MyStaticValue.isNumRecognition = false;
        // System.out.println(ToAnalysis.parse("0.46??"));
        // ;
        // 
        // 
        // System.out.println(ToAnalysis.parse("??????"));
        // System.out.println(ToAnalysis.parse("???+??????"));
        // System.out.println(ToAnalysis.parse("??????????"));
        // System.out.println(ToAnalysis.parse("????+"));
        // 
        // 
        // System.out.println(ToAnalysis.parse("??? ????? 30"));
        // System.out.println(ToAnalysis.parse("?????????????????"));
        // 
        // System.out.println(ToAnalysis.parse("???;"));
        // 
        // System.out.println(ToAnalysis.parse(";"));
        // 
        // List<String> all = new ArrayList<String>();
        // 
        // all.add("????????????????");
        // all.add("????");
        // all.add("10,??????????,??,330102196204011513 ,2,13456808992,?????44-122-102,?????44-122-102,Z2015120110302017,Z,2015    -12-25");
        // all.add("???????????????????");
        // all.add("??16???????");
        // all.add("????????????????");
        // all.add("????????");
        // all.add("?????????????????????????????????????????????????????????????????????????");
        // all.add("?????????????????????_????");
        // all.add("????????????????");
        // all.add("??????????");
        // all.add("????????");
        // all.add("8??????,??????1?2??801?,?AA09362220,????,2006/11/28");
        // all.add("?????1997??971662?,1997-10-06,,,????????,,1997-10-06,,,,???,,330102600702121,19600702,,,??,,???????4?104?,,,,???,,330106601004002,19601004,,,??,,???4?217?,,,");
        // all.add("10,??????????,??,330102196204011513 ,2,13456808992,?????44-122-102,?????44-122-102,Z2015120110302017,Z,2015-12-25");
        // 
        // all.add("?????");
        // all.add("2015?6?3?");
        // //	    System.out.println(ToAnalysis.parse("?????????"));
        // //	    System.out.println(NlpAnalysis.parse("?????????"));
        // 
        // 
        // for (String string : all) {
        // System.out.println(ToAnalysis.parse(string));
        // System.out.println(NlpAnalysis.parse(string));
        // System.out.println(IndexAnalysis.parse(string));
        // }
        // 
        // System.out.println(DATDictionary.getItem(" "));
        // System.out.println(DATDictionary.getItem("	"));
        // 
        // 
        // System.out.println(NlpAnalysis.parse("2015??????????"));
        // 
        // //dic?????bug??????????????? #398
        // MyStaticValue.isRealName = true;
        // DicLibrary.insert(DicLibrary.DEFAULT, "????");
        // DicLibrary.insert(DicLibrary.DEFAULT, "??");
        // System.out.println(DicAnalysis.parse("????"));
        // 
        // 
        // System.out.println(ToAnalysis.parse(""));
        // System.out.println(DicAnalysis.parse(""));
        // System.out.println(ToAnalysis.parse("?"));
        // System.out.println(DicAnalysis.parse("?"));
        // 
        // Forest dict1 = new Forest();
        // Library.insertWord(dict1, new Value("??", "userDefine", "1000"));
        // Library.insertWord(dict1, new Value("??", "userDefine", "1000"));
        // System.out.println(DicAnalysis.parse("??????", dict1));
        // 
        // //#385 ansj_seg5.0.3?????????default.dic???????????????????
        // System.out.println(DicAnalysis.parse("???"));
        // 
        // // #386
        // parse = NlpAnalysis.parse("??170 ???1990? ?").getTerms();
        // 
        // 
        // System.out.println(DicAnalysis.parse("?"));
        // System.out.println(DicAnalysis.parse("?Microsoft?????MICROcomputer??????????SOFTware??????????"));
        // System.out.println(DicAnalysis.parse("???????????12312312???????"));
        // System.out.println(DicAnalysis.parse("??????????????????"));
        // System.out.println(DicAnalysis.parse("??????????????????", null));
        // System.out.println(DicAnalysis.parse("??????????????", null));
        // 
        // System.out.println(ToAnalysis.parse("??????????", null));
        // 
        // //5.1.0??dic???????? #409
        // 
        // DicLibrary.insert(DicLibrary.DEFAULT, "???", "user", 1000);
        // DicLibrary.insert(DicLibrary.DEFAULT, "???");
        // System.out.println(ToAnalysis.parse("?????????"));
        // System.out.println(DicAnalysis.parse("???"));
        // 
        // DicLibrary.insert(DicLibrary.DEFAULT, "????", "n", 10000);
        // System.out.println(IndexAnalysis.parse("??????????"));
        // System.out.println(ToAnalysis.parse("??????????"));
        // DicLibrary.insert(DicLibrary.DEFAULT, "??", "nr", 10000);
        // System.out.println(DicAnalysis.parse("??????????"));
        // 
        // System.out.println(DicAnalysis.parse("???"));
        // 
        // String key = "dic_entity";
        // DicLibrary.put(key, key, new Forest());
        // DicLibrary.insert(key, "??", "entity", 1000);
        // DicLibrary.insert(key, "??", "entity", 1000);
        // System.out.println(DicAnalysis.parse("??", DicLibrary.gets(key)));
        // System.out.println(ToAnalysis.parse("??", DicLibrary.gets(key)));
        // System.out.println(DicAnalysis.parse("??", DicLibrary.gets(key)));
        // System.out.println(ToAnalysis.parse("??", DicLibrary.gets(key)));
        // 
        // 
        // System.out.println(ToAnalysis.parse("?????"));
        // System.out.println(IndexAnalysis.parse("?????"));
        // DicLibrary.insert(DicLibrary.DEFAULT,"mmol/L","danwei",10000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"/HP","danwei",10000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"mmHg","danwei",10000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"?","danwei",10000);
        // MyStaticValue.isQuantifierRecognition = false ;
        // DicLibrary.insert(DicLibrary.DEFAULT,"mmol/L".toLowerCase(),"danwei",10000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"/HP".toLowerCase(),"danwei",10000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"mmHg".toLowerCase(),"danwei",10000);
        // DicLibrary.insert(DicLibrary.DEFAULT,"?".toLowerCase(),"danwei",10000);
        // 
        // 
        // Result recognition = ToAnalysis.parse("??0.2mmol/L, Hg?88mmHg????31.5?").recognition(new UserDicNatureRecognition());
        // System.out.println(recognition);
        // recognition = DicAnalysis.parse("??0.2mmol/L, Hg?88mmHg????31.5?");
        // System.out.println(recognition);
        // DicLibrary.insertOrCreate(DicLibrary.DEFAULT,"??","pos",100000);
        // DicLibrary.insertOrCreate(DicLibrary.DEFAULT,"??","describe",100000);
        // 
        // System.out.println(DicAnalysis.parse("????"));
        // System.out.println(ToAnalysis.parse("????????"));
        // System.out.println(NlpAnalysis.parse("????????"));
        // System.out.println(DicAnalysis.parse("????????"));
        // System.out.println(BaseAnalysis.parse("????????"));
        DicLibrary.insert(DEFAULT, "????", "drug", 2000);
        System.out.println(DicLibrary.get().getWord("????").getParam(0));
        System.out.println(ToAnalysis.parse("???????").recognition(new UserDicNatureRecognition()));
        // DicLibrary.insertOrCreate(DicLibrary.DEFAULT,"???","n",1000);
        // 
        // System.out.println(DicAnalysis.parse("?????????O(?_?)O~_??_??_bilibili_????"));
        // System.out.println(NlpAnalysis.parse("?????????O(?_?)O~_??_??_bilibili_????"));
        // System.out.println(ToAnalysis.parse("?????????O(?_?)O~_??_??_bilibili_????"));
        AmbiguityLibrary.insert(AmbiguityLibrary.DEFAULT, "?", "a", "??", "b");
        System.out.println(NlpAnalysis.parse("???????"));
        System.out.println(ToAnalysis.parse("???????"));
        System.out.println(DicAnalysis.parse("???????"));
        System.out.println(DicAnalysis.parse("??????????"));
        System.out.println(NlpAnalysis.parse("??????????"));
        System.out.println(ToAnalysis.parse("??????5?????????"));
        String test = "[\u79d1\u6280]\u901f\u5ea6\u5bf9\u6bd4\uff01\u5c0f\u7c736 \u5927\u6218 \u4e09\u661fS8\uff01\u8d35\u4e0d\u4e00\u5b9a\u597d \u8fd9\u79cd\u5bf9\u6bd4\u624b\u6bb5\u7279\u522b\u50bb\u903c\uff5e\u771f\u7684\u3002\n" + ("\u4e00\u70b9\u610f\u4e49\u4e5f\u6ca1\u6709\uff5e\n" + "?????????????");
        for (Term term : NlpAnalysis.parse(test)) {
            System.out.println((((term.getName()) + " : ") + (term.getNatureStr())));
        }
        SynonymsLibrary.put(SynonymsLibrary.DEFAULT, SynonymsLibrary.DEFAULT, new org.nlpcn.commons.lang.tire.domain.SmartForest<java.util.List<String>>());
        SynonymsLibrary.insert(SynonymsLibrary.DEFAULT, new String[]{ "??", "??" });
        DicLibrary.put(DEFAULT, DEFAULT, new Forest());
        System.out.println(ToAnalysis.parse("????????").getTerms().toString());
        Value value = new Value("???", "xname", "10000");
        Library.insertWord(DicLibrary.get(), value);
        java.util.List<Term> terms = DicAnalysis.parse("????").getTerms();
        for (Term term : terms) {
            System.out.println((((term.getName()) + "\t") + (term.getNatureStr())));
        }
    }
}
