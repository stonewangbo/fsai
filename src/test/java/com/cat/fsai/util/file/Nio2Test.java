package com.cat.fsai.util.file;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class Nio2Test {

    @Test
    public void testSave()throws Exception{
        var file = new File("data/sample.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        try (var channel = AsynchronousFileChannel.open(file.toPath(),StandardOpenOption.WRITE)) {
            var buffer = ByteBuffer.allocate(1000);
            buffer.put("12356abv\r\n".getBytes());
            buffer.put("312443\r\n".getBytes());
            int position = buffer.position();
            buffer.flip();
            var handler =  new CompletionHandler<Integer,ByteBuffer>() {
                public void completed(Integer result,
                                      ByteBuffer buffer) {

                }

                public void failed(Throwable exception,
                                   ByteBuffer attachment) {
                    log.error("fail:",exception);
                }
            };

            channel.write(buffer, file.length());
            //channel.write(buffer, file.length(),buffer,handler);
        }
        Thread.sleep(1000);
    }

    @Test
    public void testLoad()throws Exception{
        var file = Path.of("data/sample.txt");

        try (var channel = AsynchronousFileChannel.open(file, StandardOpenOption.READ)) {
            var buffer = ByteBuffer.allocate(1000);
            var handler = new CompletionHandler<Integer,
                                ByteBuffer>() {
                public void completed(Integer result,
                                      ByteBuffer buffer) {
                    try {
                        char[] en = "\r\n".toCharArray();
                        int readedbyte = result;
                        byte[] by = buffer.array();
                        ByteBuffer lineBuffer = ByteBuffer.allocate(500);
                        for (int i = 0; i < readedbyte; i++) {
                            if (by[i] == en[0] && by[i+1] == en[1]) {
                                i=i+1;
                                int position = lineBuffer.position();
                                byte[] lineArray = lineBuffer.array();
                                String lineStr = new String(lineArray, 0, position, "utf-8");
                                log.info("=== i:{} lineStr:{}",i,lineStr);
                                lineBuffer.clear();
                            }else {
                                lineBuffer.put(by[i]);
                            }
                        }
//                        String str = new String(attachment.array());
//                        String[] list = str.split("\r\n");
//                        log.info("Bytes result：{} attachment:{}", result, list[0]);
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
        } catch (IOException e) {
            log.error("fail:",e);
        }
        Thread.sleep(1000);
    }
}
