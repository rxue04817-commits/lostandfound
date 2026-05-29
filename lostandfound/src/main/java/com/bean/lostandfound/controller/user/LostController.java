// LostController.java (用户端控制器)
package com.bean.lostandfound.controller.user;

import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.pojo.dto.LostFoundDTO;
import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.pojo.dto.UpdateLostFoundDTO;
import com.bean.lostandfound.pojo.vo.LostFoundDetailVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.LostService;
import com.bean.lostandfound.server.OssService;
import com.bean.lostandfound.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/lostfound")
public class LostController {

    @Autowired
    private LostService lostService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OssService ossService;

    /**
     * 用户浏览失物列表（只能看到已审核的失物）
     * @param lostSearchDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult> getLostFoundList(LostSearchDTO lostSearchDTO) {
        // 普通用户默认只能看到已审核的失物
        if (lostSearchDTO.getStatus() == null) {
            lostSearchDTO.setStatus(1); // 只显示已审核的失物
        }
        return Result.success(lostService.getLostFoundList(lostSearchDTO));
    }

    /**
     * 获取失物详情
     * @param id 失物ID
     * @return 失物详情
     */
    @GetMapping("/{id}")
    public Result<LostFoundDetailVO> getLostFoundDetail(@PathVariable Integer id) {
        try {
            LostFoundDetailVO detail = lostService.getLostFoundDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取失物详情失败: " + e.getMessage());
        }
    }

    /**
     * 发布失物信息
     * @param lostFoundDTO 失物信息DTO
     * @param request HTTP请求
     * @return Result
     */
    @PostMapping
    public Result<String> publishLostFound(@RequestBody LostFoundDTO lostFoundDTO,
                                           HttpServletRequest request) {
        try {
            String token = request.getHeader(jwtUtil.getHeader());
            Integer userId = jwtUtil.getUserIdFromToken(token);
            lostService.publishLostFound(lostFoundDTO, userId);
            return Result.success("发布成功");
        } catch (Exception e) {
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 修改失物信息（发布者）
     * @param id 失物信息ID
     * @param updateLostFoundDTO 更新的失物信息DTO
     * @param request HTTP请求
     * @return Result
     */
    @PutMapping("/{id}")
    public Result<String> updateLostFound(@PathVariable Integer id,
                                          @RequestBody UpdateLostFoundDTO updateLostFoundDTO,
                                          HttpServletRequest request) {
        try {
            String token = request.getHeader(jwtUtil.getHeader());
            // 从token中获取用户ID
            Integer currentUserId = jwtUtil.getUserIdFromToken(token);

            lostService.updateLostFound(id, updateLostFoundDTO, currentUserId);

            return Result.success("更新成功");
        } catch (UnauthorizedException e) {
            return Result.error("无权限操作: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 查询自己发布的失物信息
     * @param request HTTP请求

     * @return 分页结果
     */
    @GetMapping("/user")
    public Result<PageResult> getLostFoundByUserId(HttpServletRequest request,LostSearchDTO lostSearchDTO) {
        try {
            // 构造查询条件

            String token = request.getHeader(jwtUtil.getHeader());
            Integer userId = jwtUtil.getUserIdFromToken(token);
            lostSearchDTO.setUserId(userId); // 查询自己发布的失物，可以看到所有状态

            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新失物信息状态
     * @param id 失物信息ID
     * @param status 新的状态值
     * @param request HTTP请求
     * @return Result
     */
    @PutMapping("/{id}/status")
    public Result<String> updateLostFoundStatus(@PathVariable Integer id,
                                                @RequestParam Integer status,
                                                HttpServletRequest request) {
        try {
            String token = request.getHeader(jwtUtil.getHeader());
            Integer userId = jwtUtil.getUserIdFromToken(token);
            Integer userRole = jwtUtil.getRoleFromToken(token);

            lostService.updateLostFoundStatus(id, status, userId, userRole);
            return Result.success("状态更新成功");
        } catch (UnauthorizedException e) {
            return Result.error("无权限操作: " + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error("状态更新失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
    @PostMapping("/upload-images")
    public Result<List<String>> uploadLostFoundImages(@RequestParam("images") MultipartFile[] files,
                                                      HttpServletRequest request) {
        try {
            // 验证token
            String token = request.getHeader(jwtUtil.getHeader());
            if (token == null) {
                return Result.error("未授权");
            }

            if (!jwtUtil.validateToken(token)) {
                return Result.error("token无效或已过期");
            }

            List<String> imageUrls = new ArrayList<>();

            // 上传所有图片
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = ossService.uploadFile(file, "lostfound/");
                    imageUrls.add(fileUrl);
                }
            }

            return Result.success(imageUrls);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id){
        lostService.deleteById(id);
        return Result.success();
    }

}
