// AdminController.java
package com.bean.lostandfound.controller.admin;

import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.LostService;
import com.bean.lostandfound.utils.AuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/lostfound")
public class AdminController {

    @Autowired
    private LostService lostService;

    @Autowired
    private AuthHelper authHelper;

    /**
     * 管理员查看所有失物信息（可按状态筛选）
     * @param lostSearchDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult> getAllLostFound(LostSearchDTO lostSearchDTO,
                                              HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 管理员按状态查看失物信息
     * @param status 状态值 (0:待审核, 1:已审核, 2:已领取, 3:已过期)
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/status/{status}")
    public Result<PageResult> getLostFoundByStatus(@PathVariable Integer status,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);

            // 构造查询条件
            LostSearchDTO lostSearchDTO = new LostSearchDTO();
            lostSearchDTO.setPage(page);
            lostSearchDTO.setSize(size);
            lostSearchDTO.setStatus(status);

            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取全平台统计数据（管理员专用）
     * @return 所有失物信息列表（不分页）
     */
    @GetMapping("/statistics/all")
    public Result<PageResult> getAllPlatformStatistics(HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            
            // 构造查询条件，获取所有数据
            LostSearchDTO lostSearchDTO = new LostSearchDTO();
            lostSearchDTO.setPage(1);
            lostSearchDTO.setSize(100000); // 获取大量数据用于统计
            
            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
