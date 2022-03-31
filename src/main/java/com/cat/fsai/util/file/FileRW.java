package com.cat.fsai.util.file;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * @Description 文件读写工具
 * @Author wangbo
 * @Date 3/27/2022 2:22 PM
 * @Version 1.0
 */
@Slf4j
@Component
public class FileRW {

     static final int buffSize = 1024;

     static final byte[] CRLF= "\r\n".getBytes();


     CompletionHandler<Integer,ByteBuffer> handler =  new CompletionHandler<>() {
          public void completed(Integer result,
                                ByteBuffer buffer) {
               buffer.clear();
          }

          public void failed(Throwable exception,
                             ByteBuffer attachment) {
               log.error("fail:",exception);
          }
     };
     /**
      * @param file
      * @param list
      */
     public void save(File file, List<?> list)throws Exception{
          // check file exist
          if(!file.exists()){
               file.createNewFile();
          }
          try ( var channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
               var position = file.length();
               var bufferPos = 0;
               var buffer = ByteBuffer.allocate(buffSize);
               var bufferTmp= ByteBuffer.allocate(buffSize);
               for(var obj:list){
                    try {
                         buffer.put(JSONObject.toJSONString(obj).getBytes());
                         bufferPos = buffer.position();
                    }catch (BufferOverflowException e){
                         buffer.flip();
                         channel.write(buffer, position,bufferTmp,handler);
                         position+=bufferPos;
                         buffer = ByteBuffer.allocate(buffSize);
                         buffer.put(JSONObject.toJSONString(obj).getBytes());
                         bufferPos = buffer.position();
                    }
                    try {
                         buffer.put(CRLF);
                         bufferPos = buffer.position();
                    }catch (BufferOverflowException e){

                    }
               }
          }
     }
}
