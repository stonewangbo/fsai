package com.cat.fsai.inter.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class KLine {
    /** 开盘时间 */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /** 开盘价 */
    private BigDecimal beginPr;
    /** 最高价 */
    private BigDecimal highPr;
    /** 最低价 */
    private BigDecimal lowPr;
    /** 收盘价 */
    private BigDecimal finishPr;
    /** 交易量 */
    private BigDecimal tradeAmt;
    /** 收盘时间 */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /** 成交额 */
    private BigDecimal turnover;
    /** 成交笔数 */
    private Integer tradeCount;
    /** 主动买入成交量 */
    private BigDecimal buyAmt;
    /** 主动买入成交额 */
    private BigDecimal buyTurnover;
}
