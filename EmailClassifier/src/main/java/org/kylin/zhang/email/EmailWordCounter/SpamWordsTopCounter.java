package org.kylin.zhang.email.EmailWordCounter;

import org.kylin.zhang.Redis.RedisUtils;
import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.bean.EmailBean;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by win-7 on 2016/2/2.
 */
public class SpamWordsTopCounter extends  WordsCounter {

    private static boolean isSpamEmail = true ;
    private Map<String,Double> topnWordCounterWeightPair ;

    public SpamWordsTopCounter(){
        super() ;
        topnWordCounterWeightPair = new HashMap<String,Double>() ;
    }

    public SpamWordsTopCounter( int topN ){
        this() ;
        this.topN = topN ;
    }


    /**
     * run parent class's run method
     * get the top-N word-counter
     * */
    Map<String, Double> storeTopnWordCounterWeightPair( List<EmailBean> emailBeanList){
        if ( topN <= 0 || emailBeanList==null || emailBeanList.size() <= 0){
            logger.error("[error] top n or emailBeanList is null ");
            return null ;
        }

        List<Map.Entry<String,Integer>> topNWordList =   super.run(emailBeanList, isSpamEmail) ;

        // here we store the String-Double pair into the Redis
        for ( Map.Entry<String,Integer> mapping : topNWordList){
            String key = mapping.getKey() ;
            Double value = (mapping.getValue()*1.0)/(this.totalWords*1.0) ;

            Jedis jRedisHandler = RedisUtils.getJedisHandler() ;
            jRedisHandler.set(key, value+"") ;
            this.topnWordCounterWeightPair.put(key,value) ;
        }

        return topnWordCounterWeightPair ;
    }

    public static void main ( String [] args ) throws Exception {
        List<EmailBean> emailBeanList = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trainFolderName) ;

        SpamWordsTopCounter spamWordsTopCounter = new SpamWordsTopCounter(1000) ;

        spamWordsTopCounter.storeTopnWordCounterWeightPair(emailBeanList);
    }
}
