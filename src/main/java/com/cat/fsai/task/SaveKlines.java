package com.cat.fsai.task;

import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.type.TR;
import com.cat.fsai.util.file.FileRW;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Description 保存k线图数据
 * @Author wangbo
 * @Date 4/7/2022 9:40 PM
 * @Version 1.0
 */
@Service
@Slf4j
public class SaveKlines {

    @Value("${data.path.klines}")
    private String path;

    @Autowired
    private FileRW fileRW;

    @Autowired
    private BinanceMarket binanceMarket;


    public void saveKlines(TR tr,final LocalDateTime startTime,final LocalDateTime endTime)throws Exception{
        // 按时间进行拆分
        final int size = 1000;
        LocalDateTime nowStart = LocalDateTime.from(startTime);
        while (nowStart.compareTo(endTime)<0){
            LocalDateTime nowEnd = nowStart.plusMinutes(size);
            if(nowEnd.compareTo(endTime)>0) nowEnd = endTime;
            //生成文件
            File flle = getFile(nowStart,tr);
            CountDownLatch downLatch = new CountDownLatch(1);
            //开始调取数据
            binanceMarket.klines((klines, e)->{
                //log.info("klines:{} \r\n error:{}",klines,e);

                try {
                    var sign =  fileRW.save(flle, klines);
                    sign.await(2, TimeUnit.SECONDS);
                }catch (Exception e1){
                    log.error("save fail",e1);
                }
                downLatch.countDown();
            },java.sql.Timestamp.valueOf(nowStart),java.sql.Timestamp.valueOf(nowEnd),size, TR.BTC_USDT);
            downLatch.await(5, TimeUnit.SECONDS);

            nowStart = nowEnd;
        }

    }

    final DateTimeFormatter pathFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    final DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("HH-mm");

    private File getFile(LocalDateTime time, TR tr){
        return new File(String.format("%s/%s/%s/%s_%s.txt",path,tr.name(),pathFormatter.format(time),tr.name(),fileNameFormatter.format(time)));
    }
}
