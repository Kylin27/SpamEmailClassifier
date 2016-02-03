package org.kylin.zhang.spark;

import org.apache.log4j.Logger;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.kylin.zhang.redis.RedisUtils;
import org.kylin.zhang.email.EmailWordCounter.WordsCounter;
import org.kylin.zhang.email.bean.EmailBean;
import redis.clients.jedis.Jedis;


import java.util.*;

/**
 * Created by win-7 on 2016/2/3.
 *
 * This class is used to read EmailBeans from Redis
 * and extracts features from EmailBean
 *
 * final return value is List<LabeledPointed>
 *
 *
 */
public class FeatureExtractor {


    private Logger logger = Logger.getLogger(FeatureExtractor.class) ;
    private WordsCounter wordsCounter ;

    public FeatureExtractor(){
            wordsCounter = new WordsCounter() ;
    }

   public  List<LabeledPoint> getEmailBeansFeatureLabeledPointList (List<String> emailBeanKeySet){
        List<EmailBean> emailBeanList = RedisUtils.getEmailBeans( emailBeanKeySet ) ;
        boolean isSpam = false ;

        // if the email is spam , the key starts with 'spam'
        String key = emailBeanKeySet.get(0) ;

        if (key.startsWith("spam"))
            isSpam = true ;
        // if the email is normal , key starts with 'normal'
        else if (key.startsWith("normal"))
            isSpam = false ;
        else {
            logger.error("unknown key type error") ;
            return null ;
        }

       return getLabeledPointListFromEmailBeanList(emailBeanList, isSpam) ;
    }

   private   List<LabeledPoint> getLabeledPointListFromEmailBeanList(List<EmailBean> beanList , boolean isSpamEmail){
        double classifierType  ;
        if ( isSpamEmail )
            classifierType = 0.0 ;  // spam email
        else
            classifierType = 1.0 ; // normal email type

        List<LabeledPoint> labeledPointList = new ArrayList<LabeledPoint>() ;

        for ( EmailBean bean : beanList){
            Vector v = getFeaturesFromEmailBean(bean, true ) ;
            LabeledPoint labeledPoint = new LabeledPoint(classifierType , v ) ;
            labeledPointList.add(labeledPoint);
        }

        return labeledPointList ;
    }


    /**
     * This method is used both in testing and training method
     *
     * In getFeaturesFromEmailBean function ,
     * we extract 5 features
     * 1. we set n :how many times current email words match with spam-email topN words which are extracted from training spam-email dataset
     *       set w (< 1 ) :the weight = how many times a spam-word appear in whole spam training data set / total spam training data words
     *       freature 1 = n*w
     *
     * 2. we classify time 24 hours into three region
     *     am.  --> 0
     *     pm.  --> 1
     *     null --> 2
     *
     * 3. whether the sentDate is later than receivedDate  --> ordinary spam feature , sentDate > receivedDate
     *    if sentDate later than receivedDate        --> type 0 ;
     *    else if sentDate earlier than receiveDate  --> type 1 ;
     *    else if one of them is null | all null              --> type 2 ;
     *
     * 4. whether the from-address attribute is empty (which you could not see where the email comes from )
     *    if sender's attribute is empty  --> type 0 ;
     *    else                            --> type 1 ;
     *
     * 5. whether the receiver's address are more than 3    (ordinary spam email , the spam email sender usually sends the spam to multi-receiver at one time)
     *    if ( len(receiver-array )  == 0 )         --> type 0 ;
     *    else if ( len(receiver-array) <= 3)       --> type 1 ;
     *    else                                      --> type 2  ;
     *
     * PS: Before we inserted each Email-Bean into the Redis , the Email-Bean's text content has already been filtered(cleaned)
     * so when we get the email-bean from Redis , we do not need to filter it again ;
     *
     * However , the getFeaturesFromEmailBean method can both be called by training data (already cleaned ) processor and testing data processor.
     * So , with the aim to clean the cleaned data again , we use the boolean type variable isCleaned to describe
     * whether the data is cleaned (filtered special characters or stop words) --> isCleaned : true
     * or the data is uncleaned(need filter special characters and stop words) --> isCleaned : false
     * */
    public Vector getFeaturesFromEmailBean(EmailBean emailBean, boolean isCleaned ){

        double [] featuresArray = new double [5] ;

        if ( !isCleaned) {
            emailBean = wordsCounter.filterEmailBean( emailBean ) ;
        }


        // current email text word counter hash-map
        Map<String,Integer> wordCounterPair = wordsCounter.wordCounter(emailBean) ;

        double f1 = 0.0 ;

        for ( Map.Entry<String,Integer> entry : wordCounterPair.entrySet() ){
            String key = entry.getKey() ;
            int   value = entry.getValue() ;    // word key appear times

           Jedis jedisHandler = RedisUtils.getJedisHandler() ;
            if ( jedisHandler.exists(key)){
                double weight = Double.parseDouble(jedisHandler.get(key)) ;  // weight
                f1 += weight * value ;  // how many time appear * spam-topN frequency weight
            }

        }

        // add feature 1
       featuresArray[0] = f1 ;

        double f2 ;
        Calendar cal =null ;

        if ( emailBean.getSentDate() != null ){
            cal = Calendar.getInstance() ;
            cal.setTime(emailBean.getSentDate()) ;
        }

        else if ( emailBean.getRecvDate() != null){
            cal = Calendar.getInstance() ;
            cal.setTime( emailBean.getRecvDate());
        }
        else {
            cal = null ;
        }

        if ( cal != null && cal.get(Calendar.AM_PM) == Calendar.AM)
            f2 = 0.0 ; // am sent the email
        else if ( cal != null &&  cal.get(Calendar.AM_PM)== Calendar.PM)
            f2 = 1.0 ; // pm sent the email
        else
            f2 = 2.0 ;

        featuresArray[1] = f2 ;

        double f3 = 2.0;
        Date sentDate  = emailBean.getSentDate() ;
        Date recvDate  = emailBean.getRecvDate() ;


        if ( (sentDate != null && recvDate != null )&&sentDate.after( recvDate))
            f3 = 0.0 ;
        else if (  (sentDate != null && recvDate != null ) && sentDate.before( recvDate))
            f3 = 1.0 ;
        featuresArray[2] = f3 ;

        double f4 ;
        if ( emailBean.getSenderList() == null )
            f4 = 0.0 ; // sender addr is null
        else
            f4 = 1.0 ; // we can get the sender name

        featuresArray[3] = f4 ;

        double f5 ;
        if ( emailBean.getToTypeList() == null )
            f5 = 0.0 ; // receiver is null
        else if ( emailBean.getToTypeList().size() <= 3)
            f5  = 1.0 ;
        else
            f5 = 2.0 ;

        featuresArray[4] = f5 ;

        return Vectors.dense( featuresArray) ;
    }

    }