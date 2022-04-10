package com.cat.fsai.fx;

import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.type.TR;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;


@Component
@Slf4j
public class HelloworldController implements Initializable {

    @FXML
    private Label welcomeText;

//    @FXML
//    private CategoryAxis xAxis;
//    @FXML
//    private NumberAxis yAxis;
    @FXML
    private LineChart<String, BigDecimal> lineChart;

    @FXML
    private TextField nameField;

    @FXML
    private DatePicker startTime;

    @FXML
    private DatePicker endTime;

    @FXML
    private ProgressBar loadProgress;

    // Be aware: This is a Spring bean. So we can do the following:
    @Autowired
    private BinanceMarket binanceMarket;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeText.setText("初始化1");
        lineChart.setTitle("数据初始化");

        loadProgress.setProgress(0d);
        loadProgress.requestLayout();
//        XYChart.Series dataSeries1 = new XYChart.Series();
//        dataSeries1.setName("2022");
//
//        dataSeries1.getData().add(new XYChart.Data( "2022-01-01", 567));
//        dataSeries1.getData().add(new XYChart.Data( "2022-01-02", 612));
//
//
//        lineChart.getData().add(dataSeries1);
        log.info("页面初始化完毕");
    }

    @FXML
    private void setStartTime(final Event event) throws Exception{

    }

    @FXML
    private void setEndTime(final Event event) throws Exception{

    }

    @FXML
    private void clickLoad(final Event event) throws Exception{
        CountDownLatch downLatch = new CountDownLatch(1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date startTimeD =Date.from(startTime.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endTimeD = Date.from(endTime.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        binanceMarket.klines((dg, e)->{
            Platform.runLater(()->{
                log.info("币安最新交易深度:{} startTimeD:{} endTimeD：{}",dg,startTimeD,endTimeD);
                welcomeText.setText("dg:"+dg);

                XYChart.Series dataSeries1 = new XYChart.Series();
                dataSeries1.setName(yearFormat.format(startTimeD));
                for(var item:dg) {
                    dataSeries1.getData().add(new XYChart.Data(
                            timeFormat.format(item.getStartTime()), item.getBeginPr()));
                }
                lineChart.getData().add(dataSeries1);

            });
        },startTimeD,endTimeD, 1000,TR.BTC_USDT);

        welcomeText.setText("测试：");
    }
}