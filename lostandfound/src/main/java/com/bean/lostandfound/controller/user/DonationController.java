package com.bean.lostandfound.controller.user;

import com.bean.lostandfound.pojo.dto.DonationOrderDTO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    /**
     * 发起打赏支付
     */
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody DonationOrderDTO dto) {
        String payForm = donationService.createDonationOrder(dto);
        return Result.success(payForm);
    }

    /**
     * 支付宝异步通知接口 (必须是 POST，且不能加拦截器鉴权)
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        return donationService.handleAlipayNotify(params);
    }

    /**
     * 支付宝同步回调接口 (GET请求，用于支付成功后跳转回前端页面)
     */
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        donationService.handleAlipayReturn(params, response);
    }

    /**
     * 获取我的打赏订单
     */
    @GetMapping("/my")
    public Result<PageResult> getMyDonations(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(donationService.getMyDonations(page, pageSize));
    }

    /**
     * 取消打赏订单
     */
    @PutMapping("/cancel/{outTradeNo}")
    public Result<String> cancelDonation(@PathVariable String outTradeNo) {
        donationService.cancelDonation(outTradeNo);
        return Result.success("取消成功");
    }

    /**
     * 重新支付订单
     */
    @PostMapping("/repay/{outTradeNo}")
    public Result<String> repayDonation(@PathVariable String outTradeNo) {
        String payForm = donationService.repayDonation(outTradeNo);
        return Result.success(payForm);
    }
}