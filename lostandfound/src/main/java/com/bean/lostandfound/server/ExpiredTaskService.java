// ExpiredTaskService.java
package com.bean.lostandfound.service;

import com.bean.lostandfound.mapper.LostFoundMapper;
import com.bean.lostandfound.pojo.entity.LostFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpiredTaskService {

    @Autowired
    private LostFoundMapper lostFoundMapper;

    /**
     * 定时任务：每天凌晨2点检查并更新过期的失物信息
     * cron表达式：秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkAndExpireLostFound() {
        // 查询7天前创建且状态为已审核（1）的失物信息
        List<LostFound> lostFounds = lostFoundMapper.selectExpiredLostFound();

        // 更新状态为已过期（3）
        for (LostFound lostFound : lostFounds) {
            lostFound.setStatus(3); // 已过期
            lostFound.setUpdatedAt(LocalDateTime.now());
            lostFoundMapper.updateStatus(lostFound);
        }
    }
}
