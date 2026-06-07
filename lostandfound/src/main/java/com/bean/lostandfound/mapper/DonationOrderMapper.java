package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.entity.DonationOrder;
import com.bean.lostandfound.pojo.vo.DonationOrderVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DonationOrderMapper {
    void insert(DonationOrder order);

    @Select("SELECT * FROM donation_order WHERE out_trade_no = #{outTradeNo}")
    DonationOrder findByOutTradeNo(String outTradeNo);

    @Update("UPDATE donation_order SET trade_status = #{tradeStatus}, trade_no = #{tradeNo}, updated_at = NOW() WHERE out_trade_no = #{outTradeNo}")
    void updateStatus(@Param("outTradeNo") String outTradeNo, @Param("tradeStatus") String tradeStatus, @Param("tradeNo") String tradeNo);

    Page<DonationOrderVO> findByUserId(@Param("userId") Integer userId);

    Page<DonationOrderVO> findAll();

    List<Map<String, Object>> getDailyStatistics();
}