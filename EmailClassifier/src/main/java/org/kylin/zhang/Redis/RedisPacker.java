package org.kylin.zhang.redis;

import org.kylin.zhang.email.bean.EmailBean;
import org.msgpack.MessagePack;

/**
 * Created by win-7 on 2016/2/3.
 */
public class RedisPacker {


    public static byte [] packer(EmailBean obj , Class className){
        byte [] value = null ;

        try{
            MessagePack packer = new MessagePack() ;
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

            MessagePack unpacker = new MessagePack() ;
            unpacker.register(className);
            obj = unpacker.read(bytes , className) ;

        } catch(Exception e){
            e.printStackTrace();
            return null ;
        }

        return obj ;
    }


}

