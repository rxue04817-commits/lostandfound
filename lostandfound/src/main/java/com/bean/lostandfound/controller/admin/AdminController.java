// AdminController.java
package com.bean.lostandfound.controller.admin;

import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.LostService;
import com.bean.lostandfound.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/lostfound")
public class AdminController {

    @Autowired
    private LostService lostService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员查看所有失物信息（可按状态筛选）
     * @param lostSearchDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult> getAllLostFound(LostSearchDTO lostSearchDTO,
                                              HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader(jwtUtil.getHeader());
            Integer userRole = jwtUtil.getRoleFromToken(token);

            if (userRole != 1) {
                return Result.error("无权限访问");
            }

            // 管理员可以看到所有状态的失物
            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
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
            // 验证管理员权限
            String token = request.getHeader(jwtUtil.getHeader());
            Integer userRole = jwtUtil.getRoleFromToken(token);

            if (userRole != 1) {
                return Result.error("无权限访问");
            }

            // 构造查询条件
            LostSearchDTO lostSearchDTO = new LostSearchDTO();
            lostSearchDTO.setPage(page);
            lostSearchDTO.setSize(size);
            lostSearchDTO.setStatus(status);

            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
