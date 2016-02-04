package org.kylin.zhang.redis;

import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.bean.EmailBean;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win-7 on 2016/2/2.
 */
public class RedisUtils {
    private static String spam = "spam-class";  // store spam email list
    private static String normal = "normal-class" ;  // store normal email list
    private static Jedis jedisHandler = null ;
    private static int hostport = 6379 ;
    public static String hostaddr ="127.0.0.1" ;


    public  static Jedis getJedisHandler(){

        if ( jedisHandler == null ){
            jedisHandler = new Jedis(hostaddr , hostport) ;
        }
        return jedisHandler ;
    }

    public static List<String> insertEmailBeans(boolean isSpam, List<EmailBean> emailBeanList ){
        if ( emailBeanList == null || emailBeanList.size() <= 0 ){
            System.out.println("[error] input list is empty") ;
            return null ;
        }
        List<String> keySet = new ArrayList<String>() ;

        Jedis jedis = getJedisHandler() ;

        for ( EmailBean bean : emailBeanList){
            String key  ="";

            if ( isSpam)
                key += spam  ;
            else
                key += normal ;

            key += "-"+bean.hashCode() ;
            keySet.add(key);
<<<<<<< HEAD

            if ( !jedis.exists(key.getBytes()))
                jedis.set( key.getBytes(), RedisPacker.packer(bean, EmailBean.class)) ;
            else
                System.out.println("ReidsUtils line 52") ;
=======
            jedis.set( key.getBytes(), RedisPacker.packer(bean, EmailBean.class)) ;
>>>>>>> 827eb6fbd9625673c3d7bb780387aab199b64d6b
        }
        return keySet ;
    }

    public static List<EmailBean> getEmailBeans( List<String> keySet ){

        if( keySet == null || keySet.size() == 0){
            System.out.println("[error] input keySet is null or empty ") ;
            return null ;
        }
        List<EmailBean> emailBeanLists = new ArrayList<EmailBean>() ;

        Jedis jedisHandler = getJedisHandler() ;

        for ( String key : keySet ){
            EmailBean emBean = null ;
            byte [] bytes = null ;

            if ( jedisHandler.exists(key.getBytes())){
                bytes = jedisHandler.get(key.getBytes()) ;
<<<<<<< HEAD

                if ( key.startsWith(spam) || key.startsWith(normal))
                    jedisHandler.del(key.getBytes()) ;
            }
            else {
                System.out.println("could not find value with key " + key);
                continue ;
            }
            Object obj = RedisPacker.unpacker(bytes, EmailBean.class) ;
=======
            }
            else {
                System.out.println("could not find value with key " + key);
                continue ;
            }
            Object obj = RedisPacker.unpacker(bytes, EmailBean.class) ;

           /* if ( obj instanceof  EmailBean)*/
                emBean =(EmailBean)obj ;
         /*   else
                System.out.println("what the fuck ?") ;*/
>>>>>>> 827eb6fbd9625673c3d7bb780387aab199b64d6b

           /* if ( obj instanceof  EmailBean)*/
                emBean =(EmailBean)obj ;
         /*   else
                System.out.println("what the fuck ?") ;*/

            if ( emBean != null ) {
                emailBeanLists.add(emBean);
            }
        }

        return emailBeanLists ;
    }

    public static void closeConn(){
        if ( jedisHandler != null){
            jedisHandler.del("*") ;
            jedisHandler.close();
        }
    }

    public static void main (String [] args ) throws Exception {
        List<EmailBean> emailBeanList = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trainFolderName) ;

        List<String> keySets = RedisUtils.insertEmailBeans(true , emailBeanList) ;

       /* for ( String key : keySets){
            System.out.println(key) ;
        }*/


        emailBeanList = RedisUtils.getEmailBeans(keySets) ;

        System.out.println(emailBeanList.size()) ;

        RedisUtils.closeConn();

       /* Jedis jedis ;
        JedisPool jedisPool ;
        JedisPoolConfig config = new JedisPoolConfig() ;

        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(10001);
        config.setTestOnBorrow(false);

        jedisPool = new JedisPool(config, "localhost", 6379)  ;

        jedis = jedisPool.getResource() ;*/
    }
}

