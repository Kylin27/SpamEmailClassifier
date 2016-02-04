package org.kylin.zhang;

import org.apache.log4j.Logger;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.EmailWordCounter.NormalWordsTopCounter;
import org.kylin.zhang.email.EmailWordCounter.SpamWordsTopCounter;
import org.kylin.zhang.email.bean.EmailBean;
import org.kylin.zhang.spark.FeatureExtractor;
import org.kylin.zhang.spark.NaiveBayesUtil;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aimer on 2016/2/3.
 */
public class Main {
    public static void main (String [] args ){
        Logger logger = Logger.getLogger(Main.class.getName()) ;
        List<String> spamKeySet ;
        List<String> normalKeySet ;
        List<String> topNSpamWordPairKeySet ;

        List<EmailBean> normalEmBeans = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trianNormalFolderName) ;
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

        normalEmailPointList.addAll( spamEmailPointList ) ;

       /* for ( LabeledPoint point : normalEmailPointList){
            System.out.print(point.label() + " : ") ;
            System.out.println( point.features()) ;
        }

        System.out.println("----------spam email point list-------------------") ;
        for ( LabeledPoint point : spamEmailPointList){
            System.out.print( point.label() +" : ") ;
            System.out.println( point.features()) ;
        }*/

        System.out.println("---------- begin training data -------------------") ;
        NaiveBayesUtil naiveBayesUtil = new NaiveBayesUtil( normalWordCounter.sparkConf , normalWordCounter.sparkContext ) ;
        naiveBayesUtil.TrainDataSet( normalEmailPointList );
        System.out.println("----------- training bayes model finish ---------------------------") ;


        System.out.println("--------------- reading spam email testing data sets ------------------------------") ;
        List<EmailBean> spamTestEmailBeans = EmailFileReader.getAllBeansFromFiles( EmailFileReader.testFolderName) ;
        // transfer List<EmailBean> into List<Vector>
        List<Vector> spamEmailFeatureList = new ArrayList<Vector>() ;

        for ( EmailBean emailBean : spamTestEmailBeans){
            // cause , the emailBean is extracted directly from testing dataset ,
            // so the dataset is unclean , we set second parameter false to let passed in emailBean will be cleaned
            Vector featureVector =  featureExtractor.getFeaturesFromEmailBean (emailBean, false ) ;
            if (featureVector != null ){
                spamEmailFeatureList.add(featureVector);
            }
        }

        int totalEmail = spamEmailFeatureList.size() ;
        int correctCounter = 0 ;

        for ( Vector fVector : spamEmailFeatureList ){
           Double  typeResult =  naiveBayesUtil.getClassifierResult(fVector) ;

            System.out.println("result " + typeResult) ;

            if ( typeResult.equals(0.0))
                correctCounter += 1 ;
        }

        System.out.println("final result , correct rate " + correctCounter*1.0/totalEmail*1.0) ;
    }
}
