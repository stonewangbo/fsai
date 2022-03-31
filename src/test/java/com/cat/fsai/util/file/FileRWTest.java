package com.cat.fsai.util.file;

import com.cat.fsai.cc.binance.BinanceMarket;
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
        Date endTime = format.parse("2022-01-01 00:20:00");
        binanceMarket.klines((klines, e)->{
            log.info("klines:{} \r\n error:{}",klines,e);
            downLatch.countDown();
            try {
                fileRW.save(new File("data/sample.txt"), klines);
            }catch (Exception e1){
                log.error("save fail",e1);
            }
        },startTime,endTime, TR.BTC_USDT);
        downLatch.await(2, TimeUnit.SECONDS);
        //Thread.sleep(2000);

    }
}