package com.cat.fsai.fx;

import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.type.TR;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;

@FXMLController
@Slf4j
public class HelloworldController {

    @FXML
    private Label welcomeText;

    @FXML
    private TextField nameField;

    // Be aware: This is a Spring bean. So we can do the following:
    @Autowired
    private BinanceMarket binanceMarket;

    @FXML
    private void setHelloText(final Event event) {
        CountDownLatch downLatch = new CountDownLatch(1);
        binanceMarket.depth((dg, e)->{
            log.info("dg:{}",dg);
            downLatch.countDown();
        }, TR.BTC_USDT);

        welcomeText.setText("12343");
    }
}