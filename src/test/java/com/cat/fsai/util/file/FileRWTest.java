package com.cat.fsai.util.file;

import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.inter.pojo.KLine;
import com.cat.fsai.type.TR;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
@Slf4j
public class FileRWTest {

    @Autowired
    private FileRW fileRW;

    @Autowired
    private BinanceMarket binanceMarket;

    @Test
    public void save()throws Exception{
        CountDownLatch downLatch = new CountDownLatch(1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = format.parse("2022-01-01 00:00:00");
        Date endTime = format.parse("2022-01-01 01:20:00");
        binanceMarket.klines((klines, e)->{
            log.info("klines:{} \r\n error:{}",klines,e);

            try {
               var sign =  fileRW.save(new File("data/sample.txt"), klines);
               sign.await(2, TimeUnit.SECONDS);
            }catch (Exception e1){
                log.error("save fail",e1);
            }
            downLatch.countDown();
        },startTime,endTime, TR.BTC_USDT);
        downLatch.await(2, TimeUnit.SECONDS);
        //Thread.sleep(2000);

    }

    @Test
    public void load()throws Exception{
        CountDownLatch downLatch =fileRW.load(new File("data/sample.txt"),d->{
            log.info("load:{}",d);
        }, KLine.class);

        downLatch.await(2, TimeUnit.SECONDS);
        log.info("测试读取完毕");
    }
}