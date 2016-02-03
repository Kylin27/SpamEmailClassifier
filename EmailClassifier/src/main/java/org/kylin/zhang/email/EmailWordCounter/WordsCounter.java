package org.kylin.zhang.email.EmailWordCounter;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.kylin.zhang.redis.RedisUtils;
import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.bean.EmailBean;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by win-7 on 2016/2/3.
 */
@SuppressWarnings("serial")
public class WordsCounter  implements Serializable {
    transient public static SparkConf sparkConf ;
    transient public  static JavaSparkContext sparkContext ;
    transient private List<Map.Entry<String,Integer>> sortedMapList ;
    protected int topN = 1000 ;
    protected long totalWords  ;
    transient protected Logger logger  ;
    transient private List<String> stopWordList = new ArrayList<String>();

    transient private List<String> keySet = null ;

    private static void setup(){
        sparkConf = new SparkConf().setAppName("Spark hands-on").setMaster("local");
        sparkContext = new JavaSparkContext(sparkConf);
    }

    public WordsCounter(){
        logger = Logger.getLogger(this.getClass()) ;
        if (sparkConf == null || sparkContext == null)
            setup();
        loadStopWordFromFile();
    }

    public WordsCounter(int n){
        this() ;
        this.topN = n ;
    }

    private void loadStopWordFromFile(){
        URL url ;
        File f ;
        BufferedReader bufferedReader ;
        String line ;

        try{
            url = WordsCounter.class.getResource("/stop_word_set.txt") ;
            if ( url == null){
                logger.error("[error] failed to create url, input path wrong")  ;
                return ;
            }

            f = new File(url.getPath()) ;
            bufferedReader = new BufferedReader( new FileReader( f )) ;

            while ((line = bufferedReader.readLine()) != null){
                stopWordList.add(line);
            }

            bufferedReader.close();
        }catch (Exception e){
            logger.error("[error] something wrong when loading stop words from file ") ;
            e.printStackTrace();
        }
    }

    private List<EmailBean> filterAllEmailBeans(List<EmailBean> beanList){

        List<EmailBean> filteredEmailBeanList = new ArrayList<EmailBean>() ;

        if ( beanList == null || beanList.size() <= 0){
            logger.error("[error] no elements contained in beanList") ;
            return null ;
        }

        for ( EmailBean emBean : beanList){
            EmailBean filteredEmailBean = filterEmailBean(emBean) ;

            if ( filteredEmailBean != null )
                filteredEmailBeanList.add(filteredEmailBean) ;
        }

        return filteredEmailBeanList ;
    }

    /**
     * Method wordCounter used to count each words contained in EmailBean
     * appear times
     * pair<String : word , Integer : how many times word appear in EmailBean's text content
     *
     * */
    public  Map<String,Integer> wordCounter(EmailBean bean ){
        String emailText = bean.getEmailText() ;

        List<String> emailTextToken = Arrays.asList(emailText.split(" ")) ;

        JavaRDD<String> emailTokenRDD = sparkContext.parallelize(emailTextToken).cache() ;

        JavaPairRDD<String,Integer> wordCounterPairRDD = emailTokenRDD.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s,1) ;
            }
        }) ;

        wordCounterPairRDD.cache() ;

        // fourth : JavaPairRDD<String,Integer> to JavaPairRDD<String,Integer> this step is reduce
        JavaPairRDD<String,Integer> counterPairRDD = wordCounterPairRDD.reduceByKey(
                new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1+v2 ;
                    }
                } ) ;

        return counterPairRDD.collectAsMap() ;
    }

    private List<Map.Entry<String,Integer>> getTopNWordsPair( List<EmailBean> beanList){

        // first : we set the List<EmailBean> into JavaRDD<String>
        List<String> emailContentList = new ArrayList<String>() ;

        for ( EmailBean emailBean : beanList){
            String content = emailBean.getEmailText() ;

            if ( content != null ){
                emailContentList.add(content);
            }
            else {
                emailContentList.add(emailBean.getSubject());
            }
        }

        if ( emailContentList == null || emailContentList.size() <= 0){
            logger.error("[error] failed to get email content list") ;
            return null ;
        }

        JavaRDD<String> stringJavaRDD = sparkContext.parallelize(emailContentList).cache() ;

        // sencond : JavaRDD<String> to JavaRDD<String> --> which each word in one line


        JavaRDD<String> words = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
            public Iterable<String> call(String s) throws Exception {
                return Arrays.asList(s.split(" ")) ;
            }
        }) ;
        this.totalWords = words.count() ;

                // third : JavaRDD<String> ---> JavaPariRDD<String,Integer>
        // initialize all word's initial counter value <- 1
        JavaPairRDD<String,Integer> wordCounterPairRDD = words.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s,1) ;
            }
        }) ;

        wordCounterPairRDD.cache() ;

        // fourth : JavaPairRDD<String,Integer> to JavaPairRDD<String,Integer> this step is reduce
        JavaPairRDD<String,Integer> counterPairRDD = wordCounterPairRDD.reduceByKey(
                new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1+v2 ;
                    }
                } ) ;


        // final : transfer the JavaPairRDD<String,Integer> into HashMap<String,Integer>
        // and sort it by HashMap's value
       Map<String,Integer> topWordsPair  = counterPairRDD.collectAsMap() ;

       this.sortedMapList = new ArrayList<Map.Entry<String, Integer>>(topWordsPair.entrySet()) ;
        Collections.sort(sortedMapList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue() ;
            }
        });

        return this.sortedMapList.subList(0,this.topN) ;
    }

    private  String filterSpecialCharacters(String contentText ){
        String empty =" " ;

        if ( contentText == null || contentText.length() <= 0)
            return null ;

        // filter {space , \ , \t , \r , \n , - , _ , < , > , {, } , ? , . , \, , ( , )}
        Pattern p1 = Pattern.compile("\\s|\\'|#|\t|\r|\n|\\-|\\_|/|>|<|[0-9]|;|:|\"|\\.|\\,|\\?|\\||\\{|\\}|\\[|\\]|@|\\(|\\)|=|\\*|\\&|\\^|\\%|\\$|\\!|\\`|\\~|\\+") ;

        Matcher m  = p1.matcher(contentText) ;
        String result = m.replaceAll(empty).trim() ;

        // remove multiple spaces and lower case
        Pattern p2 = Pattern.compile(" +") ;
        Matcher m2 = p2.matcher(result) ;
        result = m2.replaceAll(empty).trim().toLowerCase() ;

        return result  ;
    }
    private  String filterStopWordsFromString( String textContent ){

          if ( textContent == null  ){
           logger.error("[error]  input text content is null ") ;
           return null ;
         }

        String [] stringArray = textContent.split(" ") ;
        StringBuilder sb = new StringBuilder("") ;
        String whiteSpace = " " ;

        if ( stringArray == null || stringArray.length <= 0){
            System.out.println("here the text " + textContent) ;
            logger.error("[error] input content is null ");
            return null;
        }

        if ( stopWordList == null || stopWordList.size() <= 0){
            logger.error("[error] stop word list is empyt") ;
            return textContent;
        }

        for ( String subStr : stringArray){
            subStr = subStr.toLowerCase() ;
            if ( stopWordList.contains( subStr)) // if the input parameter's sub-str (separated by white space )
            {                                     // the word should be removed from the original input parameter
                // do nothing
            } else{
                sb.append(subStr).append(whiteSpace) ;
            }
        }

        return sb.toString() ;
    }

    public  EmailBean  filterEmailBean(EmailBean emailBean) {
        if (emailBean == null ) {
            logger.error("[error] email bean is null ");
            return null ;
        }

            String content = emailBean.getContent();

            // 1.  filter special charachters
            content = filterSpecialCharacters(content) ;

            // 2. filter stop words
            content = filterStopWordsFromString(content) ;
            emailBean.setContent(content);



            String subject = emailBean.getSubject();
            subject = filterSpecialCharacters(subject) ;
            subject = filterStopWordsFromString(subject) ;
            emailBean.setSubject(subject);

            // sender address list
            List<String> listContent = emailBean.getSenderList();
            if (listContent != null && listContent.size() > 0) {
                List<String> filteredList = new ArrayList<String>();

                for (String str : listContent) {
                    String filteredStr = filterSpecialCharacters(str) ;
                    filteredStr = filterStopWordsFromString(filteredStr);
                    filteredList.add(filteredStr);
                }
                emailBean.setSenderList(filteredList);
            }

            // to list content filter --> To
            listContent = emailBean.getToTypeList();
            if (listContent != null && listContent.size() > 0) {
                List<String> filteredList = new ArrayList<String>();

                for (String str : listContent) {
                    String filtedStr = filterSpecialCharacters(str) ;
                    filtedStr = filterStopWordsFromString(filtedStr);
                    filteredList.add(filtedStr);
                }
                emailBean.setToTypeList(listContent);
            }

            // bcc list content filter ---> Bcc
            listContent = emailBean.getBccTypeList();
            if (listContent != null && listContent.size() > 0) {
                List<String> filteredList = new ArrayList<String>();

                for (String str : listContent) {
                    String filteredStr = filterSpecialCharacters(str) ;
                    filteredStr = filterStopWordsFromString(filteredStr);
                    filteredList.add(filteredStr);
                }

                emailBean.setBccTypeList(filteredList);
            }

            // cc list content filter ---> Bcc
            listContent = emailBean.getCcTypeList();
            if (listContent != null && listContent.size() > 0) {
                List<String> filteredList = new ArrayList<String>();

                for (String str : listContent) {
                    String filteredStr = filterSpecialCharacters(str) ;
                    filteredStr = filterStopWordsFromString(filteredStr);
                    filteredList.add(filteredStr);
                }
            }

          return emailBean ;
    }


    /**
     * run method is the final execute method
     * step follows :
     * 1. load in stop words from stop-words-set file into List<String>
     * 2. receive List<EmailBean> list
     * 3. filter each EmailBean object in List<EmailBean>   ---> write List<EmailBean> into Redis by calling RedisUtil static method
     * 4. call word-counter to get the top-N word-list
     * 5. return the top-N word-list
     *
     *
     * {
     *     In sub class NormalWordsTopCounter
     *                  after getting the top-N word-list , call updateStopWordFile() method to update normal email top-N into stop-word-set
     *
     *
     *    In sub class SpamWordsTopCounter
     *                  after getting the top-N word-list , call getTopWordCounterPair() method to return the HashMap<String,Double>
     *                  the key in returned HashMap is the word and the value is the {word appear times / total top N words }
     *                  which called the weight in this program
     * }
     * */
    public List<Map.Entry<String,Integer>> run( List<EmailBean> emailBeanList, boolean isSpamEmailList ){
        loadStopWordFromFile();
        List<EmailBean> filteredEmailBeanList = this.filterAllEmailBeans(emailBeanList) ;
        this.keySet = RedisUtils.insertEmailBeans( isSpamEmailList , filteredEmailBeanList) ;    // --> here we insert the readin EmailBean s into Redis

        if ( keySet != null && keySet.size() >= 0){
          //  System.out.println("writes in redis total records " + keySet.size()) ;
            logger.info("[info] write in records "+ keySet.size() );
        }

        return this.getTopNWordsPair( filteredEmailBeanList ).subList(0 , filteredEmailBeanList.size()<=this.topN?filteredEmailBeanList.size():topN ) ;
    }

    public List<String> getKeySet() {
        return keySet;
    }

    public static void main (String [] args ) throws Exception {

        WordsCounter wordsCounter = new WordsCounter(500) ;
        List<EmailBean> emailBeanList = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trainFolderName);


       /* for ( EmailBean bean : emailBeanList){
            if ( bean.getContent() != null )
                System.out.println(bean.getContent()) ;
        }*/

       List<Map.Entry<String,Integer>> sortedValueList = wordsCounter.run(emailBeanList, true) ;

        for (Map.Entry<String,Integer> entry : sortedValueList){
        //    System.out.println("key " + entry.getKey() + "---> "+ "value "+ entry.getValue()) ;
        }

       RedisUtils.closeConn();
    }

}
