package com.bean.lostandfound.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BaseContextTest {

    @Test
    public void testSetAndGetCurrentId() {
        // 1. 准备测试数据
        Long testUserId = 10086L;

        // 2. 设置用户ID到 ThreadLocal
        BaseContext.setCurrentId(testUserId);

        // 3. 获取用户ID并断言
        Long currentId = BaseContext.getCurrentId();

        // 验证获取到的 ID 是否与设置的 ID 一致
        assertNotNull(currentId, "当前用户ID不应为null");
        assertEquals(testUserId, currentId, "获取到的用户ID应与设置的一致");
    }

    @Test
    public void testRemoveCurrentId() {
        // 1. 设置用户ID
        BaseContext.setCurrentId(10086L);

        // 2. 确认已设置
        assertNotNull(BaseContext.getCurrentId());

        // 3. 移除用户ID
        BaseContext.removeCurrentId();

        // 4. 验证已清除
        Long currentId = BaseContext.getCurrentId();
        assertNull(currentId, "移除后当前用户ID应为null");
    }

    @Test
    public void testThreadIsolation() throws InterruptedException {
        // 1. 主线程设置 ID
        BaseContext.setCurrentId(1L);

        // 2. 创建子线程
        Thread thread = new Thread(() -> {
            // 子线程获取 ID，应该为 null，因为 ThreadLocal 是线程隔离的
            Long idInSubThread = BaseContext.getCurrentId();
            assertNull(idInSubThread, "子线程不应获取到主线程的 ThreadLocal 变量");

            // 子线程设置自己的 ID
            BaseContext.setCurrentId(2L);
            assertEquals(2L, BaseContext.getCurrentId());
        });

        thread.start();
        thread.join(); // 等待子线程执行完毕

        // 3. 主线程再次获取 ID，应仍为 1L，不受子线程影响
        assertEquals(1L, BaseContext.getCurrentId(), "主线程的 ThreadLocal 变量不应受子线程影响");

        // 清理主线程数据
        BaseContext.removeCurrentId();
    }
}
