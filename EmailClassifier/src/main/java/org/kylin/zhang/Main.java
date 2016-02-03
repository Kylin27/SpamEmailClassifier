package org.kylin.zhang;

import org.apache.log4j.Logger;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.EmailWordCounter.NormalWordsTopCounter;
import org.kylin.zhang.email.EmailWordCounter.SpamWordsTopCounter;
import org.kylin.zhang.email.bean.EmailBean;
import org.kylin.zhang.redis.RedisPacker;
import org.kylin.zhang.redis.RedisUtils;
import org.kylin.zhang.spark.FeatureExtractor;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Created by Aimer on 2016/2/3.
 */
 
  /**   process : 
         * *** setp 1 training naive bayes model ****
         * 1. read in normal email training data file
         *    by calling EmailFileReader
         *    ./TRAINING{ *.eml } --> List<EmailBean>
         *
         * 2. execute word counter and update stop-words-set
         *    by calling NormalWordsTopCounter -->
         *    2.1 List<EmailBean>    -->  Map<String, Integer> --> (sorted) List<Map.Entry<String, Integer>
         *       top N List<String> --> ./data/stop_word_list.txt
         *
         *
         *    2.2 List<EmailBean> --> (cleaned)List<EmailBean> --> Redis cache ('normal-class-***' as key)
         *
         * 3. read in spam email training data file
         *   by calling EmailFileReader
         *   ./TRAINING{*.eml} --> List<Email>
         *
         * 4. execute word counter
         *    by calling SpamWordsTopCounter -->
         *    4.1 List<EmailBean> --> Map<String,Integer> --> (sorted) List<Map.Entry<String,Integer>>
         *        --> top N Map<String,Double> top N words and corresponding frequency( < 1.0 )
         *        --> Redis cache < String as key , Double + "" as value >
         *
         *    4.2 List<EmailBean> --> Redis cache ('sparm-class-***' as beging of redis key-value-pair's key)
         *
         *
         *  5. get every EmailBean's features into
         *     by calling FeatureExtractor
         *     Redis key-value pair --> 1. spam   (type 0 ) --> List<EmailBeans> --> List<LabeledPoint>
         *                          --> 2. normal (type 1 ) --> List<EmailBeans> --> List<LabeledPoint>
         *
         *  6. pass both spam & normal List<LabeledPoint> into spark mllib's NaiveBayes model as training data
         *     by call NaiveBayesUtil
         *     List<LabledPoint> spam & normal --> NaiveBayesUtil
         *
         * --------------------------------------------------
         *  *** step 2 put in test data and calculate correct rate
         * --------------------------------------------------
         * 1. read in one or more (spam or normal unknown) email file
         *    by calling EmailFileReader
         *    ./TESTING/*.eml --> EmailBean
         *
         * 2. extract email features from EmailBean instance
         *    by calling FeatureExecute
         *    EmailBean --> Vector()
         *
         * 3. calculate classify value {0.0 , 1.0}
         *    by calling NaiveBayesUtil 's getClassifierResult method
         *    Vector () --> double --> classify result
         *
         *    if        result = 0.0 --> this EmailBean --> xxx.eml is a spam email
         *    else if   result = 1.0 --> this EmailBean --> xxx.eml is a normal email 
         * */
public class Main {
    public static void main (String [] args ){
        Logger logger = Logger.getLogger(Main.class.getName()) ;
        List<String> spamKeySet ;
        List<String> normalKeySet ;
        List<String> topNSpamWordPairKeySet ;

        List<EmailBean> normalEmBeans = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trainFolderName) ;
        System.out.println( "normal em beans size : " + normalEmBeans.size() ) ;


        if ( normalEmBeans == null || normalEmBeans.size() <= 0){
            logger.error("[error] something wrong , we got List<EmailBean> normalEmBeans is null ");
            return ;
        }

        System.out.println("---------------- normal email begin--------------------------------") ;
        NormalWordsTopCounter normalWordCounter = new NormalWordsTopCounter( 500 ) ;
        normalWordCounter.updateStopWordFile(normalEmBeans);
        normalKeySet = normalWordCounter.getKeySet() ;

     /*   EmailBean testNode = normalEmBeans.get(0) ;
        String key = "normal-class-"+testNode.hashCode() ;
        Jedis redisHandler =   RedisUtils.getJedisHandler() ;
        byte [] bytes = RedisPacker.packer(testNode , EmailBean.class) ;
        redisHandler.set(key.getBytes() , bytes) ;

        if (redisHandler.exists( key.getBytes())){
            System.out.println("contain key "+key ) ;
            bytes = redisHandler.get(key.getBytes()) ;
            EmailBean anotherNode = (EmailBean)RedisPacker.unpacker(bytes, EmailBean.class) ;

            System.out.println( anotherNode ) ;
        }

*/


        System.out.println(normalKeySet.size()) ;
        System.out.println("------------------ normal email beans update and stored in redis ------------------------------") ;
        List<EmailBean> spamEmBeans = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trainFolderName) ;
        System.out.println( "spam em beans size : " + spamEmBeans.size() ) ;

        if ( spamEmBeans == null || spamEmBeans.size() <= 0){
            logger.error("[error] something goes wrong , we got List<EmailBean> spamEmBeans is null ");
            return ;
        }

        System.out.println("----------------- spam email begin -------------------------------") ;
        SpamWordsTopCounter spamWordsTopCounter = new SpamWordsTopCounter(1000) ;

        spamWordsTopCounter.storeTopnWordCounterWeightPair(spamEmBeans) ;
        spamKeySet = spamWordsTopCounter.getKeySet() ;
        System.out.println(spamKeySet.size()) ;
        System.out.println("------------------ spam email beans and hash-map value key pair stored in redis------------------------------") ;


        System.out.println("----------------- feature extractor begin -------------------------------") ;
        FeatureExtractor featureExtractor = new FeatureExtractor() ;
        List<LabeledPoint> normalEmailPointList = featureExtractor.getEmailBeansFeatureLabeledPointList(normalKeySet) ;

        List<LabeledPoint> spamEmailPointList = featureExtractor.getEmailBeansFeatureLabeledPointList(spamKeySet) ;

        System.out.println("------------------- feature extractor end -----------------------------") ;


        for ( LabeledPoint point : normalEmailPointList){
            System.out.print(point.label() + " : ") ;
            System.out.println( point.features()) ;
        }

        System.out.println("----------spam email point list-------------------") ;
        for ( LabeledPoint point : spamEmailPointList){
            System.out.print( point.label() +" : ") ;
            System.out.println( point.features()) ;
        }
    }
}
