package com.bean.lostandfound.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonationExportDTO {
    @ExcelProperty("商户订单号")
    private String outTradeNo;
    
    @ExcelProperty("打赏用户")
    private String username;
    
    @ExcelProperty("打赏金额(元)")
    private BigDecimal totalAmount;
    
    @ExcelProperty("订单标题")
    private String subject;
    
    @ExcelProperty("支付状态")
    private String tradeStatus;
    
    @ExcelProperty("创建时间")
    private String createdAt;
}