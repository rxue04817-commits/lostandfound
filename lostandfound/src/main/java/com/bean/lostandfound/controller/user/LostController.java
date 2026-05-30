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
import com.bean.lostandfound.utils.AuthHelper;
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
    private AuthHelper authHelper;

    @Autowired
    private OssService ossService;

    @GetMapping
    public Result<PageResult> getLostFoundList(LostSearchDTO lostSearchDTO) {
        if (lostSearchDTO.getStatus() == null) {
            lostSearchDTO.setStatus(1);
        }
        return Result.success(lostService.getLostFoundList(lostSearchDTO));
    }

    @GetMapping("/{id}")
    public Result<LostFoundDetailVO> getLostFoundDetail(@PathVariable Integer id) {
        try {
            LostFoundDetailVO detail = lostService.getLostFoundDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取失物详情失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<String> publishLostFound(@RequestBody LostFoundDTO lostFoundDTO,
                                           HttpServletRequest request) {
        try {
            Integer userId = authHelper.getUserId(request);
            lostService.publishLostFound(lostFoundDTO, userId);
            return Result.success("发布成功");
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<String> updateLostFound(@PathVariable Integer id,
                                          @RequestBody UpdateLostFoundDTO updateLostFoundDTO,
                                          HttpServletRequest request) {
        try {
            Integer currentUserId = authHelper.getUserId(request);
            lostService.updateLostFound(id, updateLostFoundDTO, currentUserId);
            return Result.success("更新成功");
        } catch (UnauthorizedException e) {
            return Result.error("无权限操作: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public Result<PageResult> getLostFoundByUserId(HttpServletRequest request, LostSearchDTO lostSearchDTO) {
        try {
            Integer userId = authHelper.getUserId(request);
            lostSearchDTO.setUserId(userId);
            PageResult result = lostService.getLostFoundList(lostSearchDTO);
            return Result.success(result);
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateLostFoundStatus(@PathVariable Integer id,
                                                @RequestParam Integer status,
                                                HttpServletRequest request) {
        try {
            Integer userId = authHelper.getUserId(request);
            Integer userRole = authHelper.getRole(request);
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
            authHelper.requireLogin(request);
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = ossService.uploadFile(file, "lostfound/");
                    imageUrls.add(fileUrl);
                }
            }
            return Result.success(imageUrls);
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteById(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = authHelper.getUserId(request);
            Integer userRole = authHelper.getRole(request);
            lostService.deleteById(id, userId, userRole);
            return Result.success("删除成功");
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
