package com.cat.fsai.util.file;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

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


     /**
      * 追加写入模式，不支持并发调用
      * @param file
      * @param list
      */
     public CountDownLatch save(File file, List<?> list)throws Exception{
          CountDownLatch downLatch = new CountDownLatch(1);
          // check file exist
          if(!file.exists()){
               file.createNewFile();
          }
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
          CompletionHandler<Integer,ByteBuffer> lasthandler =  new CompletionHandler<>() {
               public void completed(Integer result,
                                     ByteBuffer buffer) {
                    buffer.clear();
                    downLatch.countDown();
               }

               public void failed(Throwable exception,
                                  ByteBuffer attachment) {
                    log.error("fail:",exception);
               }
          };

          try ( var channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
               var position = file.length();
               log.info("打开文件:{} 当前大小:{}",file.toPath(),printSize(position));

               var bufferPos = 0;
               var buffer = ByteBuffer.allocate(buffSize);
               var bufferTmp= ByteBuffer.allocate(buffSize);
               for(var obj:list){
                    try {
                         var data = JSONObject.toJSONString(obj).getBytes();
                         if(buffSize-buffer.position()-data.length-CRLF.length<0){
                              log.info("缓存满，发起写入 当前大小：{}",printSize(position));
                              buffer.flip();
                              channel.write(buffer, position,bufferTmp,handler);
                              position+=bufferPos;
                              buffer = ByteBuffer.allocate(buffSize);
                              buffer.put(data);
                              buffer.put(CRLF);
                              bufferPos = buffer.position();
                         }
                         buffer.put(data);
                         buffer.put(CRLF);
                         bufferPos = buffer.position();
                    }catch (BufferOverflowException e){
                        log.error("异常",e);
                    }

               }
               //最后写入
               log.info("写入处理完毕，发起写入 当前大小：{}",printSize(position));
               buffer.flip();
               channel.write(buffer, position,bufferTmp,lasthandler);

          }

          return downLatch;
     }

     @AllArgsConstructor
     class FilePos{
          long pos;
     }

     public <T> CountDownLatch load(File file,FileContentDealer<T> dealer,Class<T> clazz)throws Exception{
          CountDownLatch downLatch = new CountDownLatch(1);
          if(!file.exists()){
              throw new IOException("file not exist:"+file.toPath());
          }
          var fileLength = file.length();
          try  {
               var channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
               var buffer = ByteBuffer.allocate(buffSize);
               FilePos filePos = new FilePos(0l);
               var handler = new CompletionHandler<Integer,
                       ByteBuffer>() {
                    public void completed(Integer result,
                                          ByteBuffer buffer) {
                         try {
                              int readedbyte = result;
                              byte[] by = buffer.array();
                              ByteBuffer lineBuffer = ByteBuffer.allocate(buffSize);
                              int nextBegin = 0;
                              for (int i = 0; i < readedbyte; i++) {
                                   if (by[i] == CRLF[0] && by[i+1] == CRLF[1]) {
                                        i=i+1;
                                        int position = lineBuffer.position();
                                        byte[] lineArray = lineBuffer.array();
                                        String lineStr = new String(lineArray, 0, position, "utf-8");
                                        //log.info("=== i:{} lineStr:{}",i,lineStr);
                                        dealer.deal(JSONObject.parseObject(lineStr,clazz));
                                        lineBuffer.clear();
                                        nextBegin =  i+1;
                                   }else {
                                        lineBuffer.put(by[i]);
                                   }
                              }
                              buffer.clear();
                              filePos.pos = filePos.pos+nextBegin;


                              if(filePos.pos<fileLength) {
                                   //嵌套读取
                                   log.info("读取进行中 result:{}，开始下一次读取filePos:{}",result,filePos.pos);
                                   channel.read(buffer, filePos.pos, buffer, this);
                              }else {
                                   log.info("全部读取完毕 result:{}，filePos:{}",result,filePos.pos);
                                   //数据读取完毕
                                   downLatch.countDown();

                              }
                         }catch (Exception e){
                              log.error("fail:",e);
                         }
                    }

                    public void failed(Throwable exception,
                                       ByteBuffer attachment) {
                         log.error("fail:",exception);
                    }
               };
               channel.read(buffer, 0, buffer, handler);
          }catch (Exception e){
               log.error("laod fail:",e);
          }
          return downLatch;
     }


     final long kb = 1l<<10;
     final long mb = kb<<10;
     final long gb = mb<<10;

     /**
      * 格式化打印大小数据
      * @param size
      * @return
      */
     public String printSize(long size){
          if(size>gb){
               long gbm = size>>30;
               return String.format("%s.%03sgb",gbm,size-(gbm<<30));
          }
          if(size>mb){
               long mbm = size>>20;
               return String.format("%s.%03smb",mbm,size-(mbm<<20));
          }
          if(size>kb){
               long kbm = size>>10;
               return String.format("%s.%03dkb",kbm,size-(kbm<<10));
          }
          return String.format("%sb",size);
     }
}
