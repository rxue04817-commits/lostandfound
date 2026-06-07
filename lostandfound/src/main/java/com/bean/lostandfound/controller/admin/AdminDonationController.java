package com.bean.lostandfound.controller.admin;

import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.pojo.dto.DonationExportDTO;
import com.bean.lostandfound.pojo.vo.DonationOrderVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.DonationService;
import com.bean.lostandfound.utils.AuthHelper;
import com.bean.lostandfound.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/donation")
public class AdminDonationController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private AuthHelper authHelper;

    /**
     * 分页查询所有打赏订单
     */
    @GetMapping("/page")
    public Result<PageResult> getDonationPage(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            return Result.success(donationService.getAllDonations(page, pageSize));
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取打赏统计数据（图表用）
     */
    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> getStatistics(HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            return Result.success(donationService.getDailyStatistics());
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 导出打赏数据为Excel
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response, HttpServletRequest request) throws IOException {
        try {
            authHelper.requireAdmin(request);
            
            // 获取所有数据，不分页或者查最大数量
            List<DonationOrderVO> list = (List<DonationOrderVO>) donationService.getAllDonations(1, 100000).getRecords();
            
            List<DonationExportDTO> exportList = list.stream().map(vo -> {
                DonationExportDTO dto = new DonationExportDTO();
                dto.setOutTradeNo(vo.getOutTradeNo());
                dto.setUsername(vo.getUsername());
                dto.setTotalAmount(vo.getTotalAmount());
                dto.setSubject(vo.getSubject());
                dto.setTradeStatus("TRADE_SUCCESS".equals(vo.getTradeStatus()) || "TRADE_FINISHED".equals(vo.getTradeStatus()) ? "已支付" : ("TRADE_CLOSED".equals(vo.getTradeStatus()) ? "已取消" : "待支付"));
                dto.setCreatedAt(vo.getCreatedAt() != null ? vo.getCreatedAt().toString() : "");
                return dto;
            }).collect(Collectors.toList());

            ExcelUtil.export(response, "打赏订单数据", DonationExportDTO.class, exportList);
        } catch (UnauthorizedException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败: " + e.getMessage());
        }
    }
}