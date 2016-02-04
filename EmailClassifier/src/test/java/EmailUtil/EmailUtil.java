/*
package EmailUtil;

import EmailUtil.bean.EmailBean;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

*/
/**
 * Created by win-7 on 2016/1/31.
 *
 * EmailUtil class is an utility class which
 * 1. extract email-contents and sender , reciver info. from the training data files
 * 2. filter the no-use data (includes special characters and stop-words)
 * 3. extract topN words which appear most often in spam email
 *
 *//*

@SuppressWarnings("serial")
public class EmailUtil implements Serializable{

    transient private JavaSparkContext sc ;
    transient private SparkConf sparkConf ;

    private int topN_counter = 1000 ; // top N limit, 1000 in default
    private Map<String,Long> topNwordList = null ;
    // HahsMap<String: spam-word, Long: how many times it appear in all training sparm email dataset

    private List<String> stopWordList = null  ;
    private static Logger logger = Logger.getLogger(EmailUtil.class) ;


    public EmailUtil () {
         sparkConf = new SparkConf().setAppName("EmailUtil").setMaster("local") ;
         sc = new JavaSparkContext(sparkConf) ;
        topNwordList = new HashMap<String,Long>() ;
    }

    public EmailUtil( int topN_c ){
        this() ; // initialize the org.kylin.zhang.spark working environment
        this.topN_counter = topN_c ;
    }

    public List<String> getStopWordList(String stopWordFilePath ){
        // lazy mode create stopWordList

        if ( this.stopWordList != null && this.stopWordList.size() > 0 )
            return this.stopWordList ;

        else {

            this.stopWordList = new ArrayList<String>();

            try {
                URL url = EmailUtil.class.getResource(stopWordFilePath) ;
             //   System.out.println(stopWordFilePath) ;
                File f ;
                BufferedReader fin;
                String line;

                if ( url == null ){
                    logger.error("[error] cannot find file " + stopWordFilePath +" under resource folder");
                    return null ;
                }

               f = new File(url.getPath()) ;
               fin = new BufferedReader( new FileReader( f) ) ;

                while ((line = fin.readLine()) != null) {
                    this.stopWordList.add(line);
                }

            } catch ( FileNotFoundException e ){
                logger.error("[error] can not find file " + stopWordFilePath + " under resource folder");
                e.printStackTrace();

            }   catch(IOException e) {
                logger.error("[error] something wrong when reading from file " + stopWordFilePath);
                e.printStackTrace();
            }

            return this.stopWordList ;
        }
    }

    */
/* List<EmailBean> to JavaRDD<String> *//*

    public JavaRDD<String> getJavaRDDFromEmailList1( List<EmailBean> emailBeanList){
        if ( emailBeanList == null || emailBeanList.size() <= 0)
        {
            logger.error("[error] no elements contained in emailBeanList") ;
            return null ;
        }

        List<String> resultList = new ArrayList<String> () ;
        List<String> stopWordsSet = getStopWordList("/stop_word_set.txt") ;

        for ( EmailBean emailBean : emailBeanList){
            String beanContent = emailBean.getEmailText() ;
            beanContent = filterSpecialCharacters(beanContent) ;
            beanContent = filterStopWords(beanContent,stopWordsSet) ;

            resultList.add(beanContent) ;
        }

        return  this.sc.parallelize(resultList,2) ;
    }

    */
/* JavaRDD<String> to JavaRDD<Long>
    *  this method is used as a word-counter
    *  < String : the words appeared in the email-content (subject,& sender, & email-content )
    *    Long : how many times the word appears in this email content>
    * *//*

    JavaPairRDD<String,Integer> wordCounter2( final JavaRDD<String> wordList ){
        // wordList each line --> hello world hello aimer
        ///                   --> hello a hello b a c d
        JavaRDD<String> words = wordList.flatMap(new FlatMapFunction<String, String>() {
            public Iterable<String> call(String s) throws Exception {
                return Arrays.asList(s.split(" ")) ;
            }
        }) ;

      JavaPairRDD<String,Integer>  wordCounterRDD =  words.mapToPair(
              new PairFunction<String, String, Integer>() {
                  public Tuple2<String, Integer> call(String s) throws Exception {
                       return new Tuple2<String, Integer>(s,1) ;
                  }
              }) ;
        wordCounterRDD.cache() ;

      JavaPairRDD<String,Integer> counterPairRDD = wordCounterRDD.reduceByKey(
              new Function2<Integer, Integer, Integer>() {
                  public Integer call(Integer v1, Integer v2) throws Exception {
                      return v1+v2 ;
                  }
              }) ;

        return counterPairRDD ;
    }

    */
/*
    * JavaPairRDD<String, Integer> to JavaRDD<Double>
    *     this method is used to calculate each words in the current email appear frequency
    *   1. calculate how many words total --> total - (long)
    *   2. get each words appears time -->     appear_counter -- (long)
    *   3. Doulbe = (double)(appear_counter)/(double)(total)
    * *//*

    JavaRDD<LabeledPoint> wordsFrequency3(JavaPairRDD<String,Integer> wordsCounterRDD){
        final long total =  wordsCounterRDD.count() ;

        System.out.println("======= counters =============") ;

        Map<String,Integer> mapp = new HashMap<String, Integer>() ;
        mapp =  wordsCounterRDD.collectAsMap() ;

        Iterator iter = mapp.entrySet().iterator() ;

       */
/* while ( iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next() ;

            Object key = entry.getKey() ;
            Object value = entry.getValue() ;

            System.out.println("[word :] " + (String)key) ;
            System.out.println("[counter value : ]  " +(Integer)value) ;
        }*//*



        JavaRDD<Double> frequencyList = wordsCounterRDD.map(new Function<Tuple2<String, Integer>, Double>() {
            public Double call(Tuple2<String, Integer> v1) throws Exception {
                double freq = v1._2().intValue()*1.0/total*1.0 ;

                return new Double(freq) ;
            }
        }) ;

        // JavaRDD <Double> --> List<Double>
        List<Double> doubleFrequencyList = frequencyList.collect() ;

        */
/* for ( int i = 0 ; i < doubleFrequencyList.size() ; i++ ){
            System.out.print(doubleFrequencyList.get(i) + "    ") ;
             if ( i % 20 == 0)
                 System.out.println() ;
         }*//*


        // List<Double> --> Vector
         double [] values = new double[doubleFrequencyList.size()] ;
        int counter = 0 ;

        for ( Double d : doubleFrequencyList){
            values[counter++]  = d.doubleValue() ;
        }

        Vector vector = Vectors.dense(values) ;

         LabeledPoint point =  new LabeledPoint(0 , vector) ;

        List<LabeledPoint> pointList = new ArrayList<LabeledPoint>() ;

        pointList.add(point);
        return this.sc.parallelize( pointList ) ;
    }




    */
/**
     * @param  contentText String
     * method to filter special characters : {space , \ , \t , \r , \n , - , _ , < , > , {, } , ? , . , \, , ( , )}
     * from the input String type parameter
     * *//*

     private static String filterSpecialCharacters(String contentText){
        String empty =" " ;

        if ( contentText == null || contentText.length() <= 0)
            return null ;

        // filter {space , \ , \t , \r , \n , - , _ , < , > , {, } , ? , . , \, , ( , )}
        Pattern p1 = Pattern.compile("\\s|\\'|#|\t|\r|\n|-|_|/|>|<|[0-9]|;|:|\"|\\.|\\,|\\?|\\||\\{|\\}|\\[|\\]|@|\\(|\\)|=|\\*|\\&|\\^|\\%|\\$|\\!|\\`|\\~|\\+") ;

        Matcher m  = p1.matcher(contentText) ;
        String result = m.replaceAll(empty).trim() ;

        // remove multiple spaces and lower case
        Pattern p2 = Pattern.compile(" +") ;
        Matcher m2 = p2.matcher(result) ;
        result = m2.replaceAll(empty).trim().toLowerCase() ;

        return result  ;
    }

    */
/**
     * @param  string:Stirng  ,  stopWordList:List<String>
     *
     * method to filter stop-words from the input string
     * we read in stop-words from './resource/stop_word_set.txt' into List<String> variable
     * *//*

    private static String filterStopWords(String string, List<String> stopWordList ){
        String [] stringArray = string.split(" ") ;
        StringBuilder sb = new StringBuilder("") ;
        String whiteSpace = " " ;

        if ( stringArray == null )
        {
         //   System.out.println ("split error") ;
            return null ;
        }
        if (stringArray.length <= 0)
        {
        //    System.out.println("string array length < 0") ;
            return null ;
        }

        if ( stopWordList == null ){
     //       System.out.println("stop word list is null ") ;
            return null ;
        }

        if (stopWordList.size() <= 0){
       //     System.out.println("size stop word = 0") ;
            return null ;
        }

        // filter the stop-words from the input param string

        for ( String str : stringArray){
            if ( stopWordList.contains( str )){
         //       System.out.println("here is the stop words " + str ) ;
            } else{
                sb.append(str).append(whiteSpace) ;
            }
        }
        return sb.toString() ;
    }


    public static void main (String [] args ) throws Exception {

        String emailPathName ="C:\\Users\\win-7\\Downloads\\spam\\CSDMC2010_SPAM\\CSDMC2010_SPAM\\TRAINING";
        EmailUtil emailUtil = new EmailUtil() ;
        List<EmailBean> emailBeanList = EmailUtil.EmailReader.getAllEmailBeanFromFiles(emailPathName) ;

        JavaRDD<String> emailBeanRDD    =  emailUtil.getJavaRDDFromEmailList1(emailBeanList) ; // get a list of EmailBean

        JavaPairRDD<String,Integer> wordCounterPairRDD = emailUtil.wordCounter2(emailBeanRDD) ;

        Map<String,Integer> resultMap = wordCounterPairRDD.collectAsMap() ;


        List<Map.Entry<String,Integer>> valueList = new ArrayList<Map.Entry<String, Integer>>( resultMap.entrySet() ) ;
        Collections.sort(valueList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue() - o1.getValue() ;
            }
        });

        for ( Map.Entry<String,Integer> mapping: valueList){
            System.out.println(mapping.getKey() +"  :  "+ mapping.getValue()) ;
        }

*/
/*
        现在我要的是，已经排序好的单词序列


        NaiveBayesModel model = NaiveBayes.train(labeledPoint.rdd()) ;

        List<LabeledPoint> testList = labeledPoint.collect() ;

        double r = model.predict(testList.get(0).features()) ;

        System.out.println("predict result " + r) ;*//*

    }

    public static class SpamClassifier {

        public static void main (String [] args) {
            SparkConf sparkConf = new SparkConf().setAppName("Spark hands-on").setMaster("local") ;
            JavaSparkContext sc = new JavaSparkContext(sparkConf) ;

            // Load 2 types of emails from text files , spam and ham (non-spam)
            // Each line has text from one email
            JavaRDD<String> spam = sc.textFile("text.txt") ;


            // here we , create a HashTF instance to map email text to vectors of 100 features
            final HashingTF tf = new HashingTF(100) ;

            JavaRDD<Vector> javaRDD = spam.map(new Function<String, Vector>() {
                public Vector call(String v1) throws Exception {
                    System.out.println(Arrays.asList(v1.split(" "))) ;
                   return  tf.transform(Arrays.asList(v1.split(" "))) ;
                }
            }) ;

            List<Vector> vectorList = javaRDD.take(100) ;

            for (Vector vv : vectorList){
                System.out.println( vv);
            }

             // Each email is split into words , and each word is mapped to one feature
            // Create LabeledPoint datasets for positive (spam) and negative(ham) examples


           */
/* JavaRDD<LabeledPoint> positiveExamples = spam.map(new Function<String, LabeledPoint>() {
                public LabeledPoint call(String v1) throws Exception {
                    return new LabeledPoint(1, tf.transform(Arrays.asList(v1.split(" ")))) ;
                }
            })  ;

           // print JavaRDD<LabeledPoint> contents
            List<LabeledPoint> pointList = positiveExamples.takeOrdered(20) ;

            for ( LabeledPoint p : pointList){
                System.out.println(p.label());

                System.out.println(p.features().toArray()) ;

                System.out.println("------------------------") ;
            }*//*

        }
    }
}
*/
