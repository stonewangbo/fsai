package com.cat.fsai;

import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.type.TR;
import de.felixroske.jfxsupport.FXMLView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@FXMLView("/fxml/hello-view.fxml")
public class HelloController {
    @FXML
    private Label welcomeText;

    @Autowired
    private BinanceMarket binanceMarket;


    @FXML
    protected void onHelloButtonClick() {
        CountDownLatch downLatch = new CountDownLatch(1);
        binanceMarket.depth((dg, e)->{
            log.info("dg:{}",dg);
            welcomeText.setText("dg:"+dg);
            downLatch.countDown();
        }, TR.BTC_USDT);


        welcomeText.setText("Welcome to JavaFX Application!");

    }
}