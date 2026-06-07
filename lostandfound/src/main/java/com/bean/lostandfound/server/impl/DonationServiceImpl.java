package com.bean.lostandfound.server.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.bean.lostandfound.config.AlipayConfig;
import com.bean.lostandfound.server.DonationService;
import com.bean.lostandfound.utils.BaseContext;
import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.mapper.DonationOrderMapper;
import com.bean.lostandfound.pojo.dto.DonationOrderDTO;
import com.bean.lostandfound.pojo.entity.DonationOrder;
import com.bean.lostandfound.pojo.vo.DonationOrderVO;
import com.bean.lostandfound.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DonationServiceImpl implements DonationService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private DonationOrderMapper donationOrderMapper;

    @Override
    public String createDonationOrder(DonationOrderDTO dto) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        if (userId == null) {
            throw new BaseException("用户未登录");
        }

        // 1. 创建本地订单
        String outTradeNo = UUID.randomUUID().toString().replace("-", "");
        DonationOrder order = new DonationOrder();
        order.setOutTradeNo(outTradeNo);
        order.setUserId(userId);
        order.setLostFoundId(dto.getLostFoundId());
        order.setTotalAmount(dto.getAmount());
        order.setSubject("失物招领平台打赏-" + outTradeNo);
        order.setTradeStatus("WAIT_BUYER_PAY");
        donationOrderMapper.insert(order);

        // 2. 调用支付宝接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        // 组装业务参数
        String bizContent = "{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + dto.getAmount().toString() + "\","
                + "\"subject\":\"" + order.getSubject() + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        request.setBizContent(bizContent);

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                return response.getBody(); // 返回包含支付页面的HTML表单
            } else {
                throw new BaseException("支付宝创建订单失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException("调用支付宝接口异常: " + e.getMessage());
        }
    }

    @Override
    public String handleAlipayNotify(Map<String, String> params) {
        try {
            // 1. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!signVerified) {
                return "fail";
            }

            // 2. 获取业务数据
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");

            // 3. 校验订单
            DonationOrder order = donationOrderMapper.findByOutTradeNo(outTradeNo);
            if (order == null) {
                return "fail";
            }

            // 4. 更新订单状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                donationOrderMapper.updateStatus(outTradeNo, "TRADE_SUCCESS", tradeNo);
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                donationOrderMapper.updateStatus(outTradeNo, "TRADE_CLOSED", tradeNo);
            }

            return "success";

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @Override
    public void handleAlipayReturn(Map<String, String> params, HttpServletResponse response) {
        try {
            // 1. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!signVerified) {
                // 签名验证失败，重定向到错误页面
                response.sendRedirect("/error?msg=签名验证失败");
                return;
            }

            // 2. 获取订单号
            String outTradeNo = params.get("out_trade_no");

            // 3. 构建前端重定向 URL
            String frontendUrl = alipayConfig.getFrontendReturnUrl();
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = "http://localhost:8081/alipay-return";
            }

            // 4. 拼接参数 (使用 URLEncoder 编码以防特殊字符问题)
            String targetUrl = frontendUrl +
                    "?out_trade_no=" + URLEncoder.encode(outTradeNo, "UTF-8") +
                    "&trade_no=" + URLEncoder.encode(params.get("trade_no"), "UTF-8") +
                    "&total_amount=" + URLEncoder.encode(params.get("total_amount"), "UTF-8");

            // 5. 执行 HTTP 302 重定向
            response.sendRedirect(targetUrl);

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendRedirect("/error?msg=处理异常");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendRedirect("/error?msg=处理异常");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public PageResult getMyDonations(int pageNum, int pageSize) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        if (userId == null) throw new BaseException("未登录");
        
        PageHelper.startPage(pageNum, pageSize);
        Page<DonationOrderVO> page = donationOrderMapper.findByUserId(userId);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult getAllDonations(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<DonationOrderVO> page = donationOrderMapper.findAll();
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map<String, Object>> getDailyStatistics() {
        return donationOrderMapper.getDailyStatistics();
    }

    @Override
    public void cancelDonation(String outTradeNo) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        if (userId == null) throw new BaseException("未登录");

        DonationOrder order = donationOrderMapper.findByOutTradeNo(outTradeNo);
        if (order == null) throw new BaseException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BaseException("无权操作");
        if (!"WAIT_BUYER_PAY".equals(order.getTradeStatus())) throw new BaseException("只能取消待支付的订单");

        donationOrderMapper.updateStatus(outTradeNo, "TRADE_CLOSED", null);
    }

    @Override
    public String repayDonation(String outTradeNo) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        if (userId == null) throw new BaseException("未登录");

        DonationOrder order = donationOrderMapper.findByOutTradeNo(outTradeNo);
        if (order == null) throw new BaseException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BaseException("无权操作");
        if (!"WAIT_BUYER_PAY".equals(order.getTradeStatus())) throw new BaseException("该订单状态不支持重新支付");

        // 重新发起支付宝请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        String bizContent = "{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + order.getTotalAmount().toString() + "\","
                + "\"subject\":\"" + order.getSubject() + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        request.setBizContent(bizContent);

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException("发起支付异常");
        }
    }
}