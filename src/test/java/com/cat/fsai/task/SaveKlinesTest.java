package com.cat.fsai.task;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.type.TR;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
@Slf4j
public class SaveKlinesTest {

    @Autowired
    private SaveKlines saveKlines;


    @Test
    public void saveKlines() throws Exception{
        saveKlines.saveKlines(TR.BTC_USDT, LocalDateTime.now().plusMinutes(-2000),LocalDateTime.now());
    }
}