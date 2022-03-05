package com.cat.fsai.fx;

import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.type.TR;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;


@Component
@Slf4j
public class HelloworldController implements Initializable {

    @FXML
    private Label welcomeText;

    @FXML
    private LineChart lineA;

    @FXML
    private TextField nameField;

    // Be aware: This is a Spring bean. So we can do the following:
    @Autowired
    private BinanceMarket binanceMarket;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeText.setText("初始化");
    }

    @FXML
    private void setHelloText(final Event event) {
        CountDownLatch downLatch = new CountDownLatch(1);
        binanceMarket.depth((dg, e)->{
            Platform.runLater(()->{
                log.info("币安最新交易深度:{}",dg);
                welcomeText.setText("dg:"+dg);
            });
            downLatch.countDown();
        }, TR.BTC_USDT);

        welcomeText.setText("测试：");
    }
}