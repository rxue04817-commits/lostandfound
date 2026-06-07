package com.bean.lostandfound.server;

import com.bean.lostandfound.pojo.dto.DonationOrderDTO;
import com.bean.lostandfound.pojo.vo.DonationOrderVO;
import com.bean.lostandfound.result.PageResult;

import java.util.List;
import java.util.Map;

public interface DonationService {
    /**
     * 创建打赏支付订单
     * @param dto 包含金额和关联信息ID
     * @return 支付宝返回的支付表单（HTML）
     */
    String createDonationOrder(DonationOrderDTO dto);

    /**
     * 处理支付宝异步通知
     * @param params 支付宝回调参数
     * @return 成功返回"success"，失败返回"fail"
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 处理支付宝同步回调
     * @param params 支付宝回调参数
     * @param response HTTP响应对象，用于执行重定向
     */
    void handleAlipayReturn(Map<String, String> params, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 获取当前用户的打赏订单
     */
    PageResult getMyDonations(int page, int pageSize);

    /**
     * 获取所有打赏订单 (管理员)
     */
    PageResult getAllDonations(int page, int pageSize);

    /**
     * 获取打赏每日统计数据 (管理员)
     */
    List<Map<String, Object>> getDailyStatistics();

    /**
     * 取消打赏订单
     */
    void cancelDonation(String outTradeNo);

    /**
     * 重新支付订单
     */
    String repayDonation(String outTradeNo);
}