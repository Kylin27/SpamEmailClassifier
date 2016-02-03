package org.kylin.zhang.Redis;

import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.bean.EmailBean;
import org.msgpack.MessagePack;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
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

            key += "-"+bean.getHashCode() ;
        //    System.out.println("insert key " + key ) ;
            keySet.add(key);
            jedis.set( key.getBytes(), Packer.packer(bean, EmailBean.class)) ;
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
            if ( jedisHandler.exists(key.getBytes()))
                bytes = jedisHandler.get(key.getBytes()) ;
            else
                System.out.println("could not find value with key "+ key ) ;

            emBean = (EmailBean)Packer.unpacker(bytes, EmailBean.class) ;

            if ( emBean != null )
                emailBeanLists.add(emBean) ;
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

class Packer {
    private static  MessagePack packer = null ;

    public static MessagePack getPacker (){
        if (packer == null ){
            packer = new MessagePack() ;
        }
        return packer ;
    }

    public static byte [] packer(Object obj , Class className){
        byte [] value = null ;

        try{
            MessagePack packer = getPacker() ;
            packer.register(className);
            value = packer.write(obj) ;
        } catch(Exception e){
            e.printStackTrace();
        }
        return value ;
    }
    public static Object unpacker(byte [] bytes , Class className ){
        Object obj = null ;

        try{
            // verify the bytes is legal
            if (bytes == null || bytes.length <= 0){
                System.out.println("[error] input bytes is null or empty") ;
                return obj ;
            }

            MessagePack unpacker = getPacker() ;
            unpacker.register(className);
            obj = unpacker.read(bytes) ;

        } catch(Exception e){
            e.printStackTrace();
        }

        return obj ;
    }


}
