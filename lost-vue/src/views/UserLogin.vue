<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'
import { login, register } from '@/api/userApi'

// 路由实例
const router = useRouter()

// 登录表单引用
const loginFormRef = ref()
// 注册表单引用
const registerFormRef = ref()
// 登录状态
const loading = ref(false)
// 注册状态
const registerLoading = ref(false)

// 注册对话框可见性
const registerDialogVisible = ref(false)
//确认对话框
const centerDialogVisible = ref(false)
// 登录表单数据
const loginForm = reactive({
    username: '',
    password: ''
})

// 注册表单数据
const registerForm = reactive({
    realName: '',
    password: '',
    phone: '',
    email: ''
})
const username = ref()
// 登录表单验证规则
const loginRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ]
}

// 注册表单验证规则
const registerRules = {
    realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ],
    phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
}

// 处理登录
const handleLogin = async () => {
    if (loading.value) return
    if (!loginFormRef.value) return

    try {
        const result = await login(loginForm)
        if (result.success) {
            ElMessage.success('登录成功')
            // 跳转到主页面
            router.push('/layout')
        } else {
            ElMessage.error(result.message)
        }
    } catch (error) {
        ElMessage.error('登录请求异常')
    } finally {
        loading.value = false
    }
}


// 显示注册对话框
const showRegisterDialog = () => {
    registerDialogVisible.value = true
    // 重置注册表单
    Object.assign(registerForm, {
        realName: '',
        password: '',
        phone: '',
        email: ''
    })
}

// 处理注册
// 处理注册
const handleRegister = async () => {
    // 先进行表单校验
    if (!registerFormRef.value) {
        ElMessage.error('表单校验未通过')
        return
    }
    try {
        await registerFormRef.value.validate()
        const result = await register(registerForm)
        if (result.success) {
            username.value = result.username
            registerDialogVisible.value = false
            centerDialogVisible.value = true
        } else {
            ElMessage.error(result.message || '注册失败')
        }
    } catch (error) {
        if (error instanceof Error) {
            // 表单校验不通过的情况
            console.log('表单校验未通过')
        } else {
            // 网络请求错误
            console.error('注册错误:', error)
            ElMessage.error('注册请求异常: ' + (error.message || '未知错误'))
        }
    }
}
</script>
<template>
    <div class="login-container">
        <!-- 背景装饰 -->
        <div class="bg-decoration">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
            <div class="circle circle-3"></div>
        </div>

        <!-- 主标题 -->
        <h1 class="main-title">失物招领平台</h1>

        <!-- 登录卡片 -->
        <el-card class="login-card" shadow="never">
            <div class="card-header">
                <div class="header-icon">
                    <el-icon :size="32"><User /></el-icon>
                </div>
                <h2 class="header-text">用户登录</h2>
            </div>

            <el-form 
                :model="loginForm" 
                :rules="loginRules" 
                ref="loginFormRef" 
                class="login-form"
                @submit.prevent="handleLogin"
            >
                <el-form-item prop="username">
                    <el-input 
                        v-model="loginForm.username" 
                        placeholder="请输入用户名" 
                        size="large"
                        clearable
                    >
                        <template #prefix>
                            <el-icon><User /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item prop="password">
                    <el-input 
                        v-model="loginForm.password" 
                        type="password" 
                        placeholder="请输入密码" 
                        size="large"
                        show-password
                    >
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item>
                    <el-button 
                        type="primary" 
                        @click="handleLogin" 
                        :loading="loading" 
                        class="login-button"
                        size="large"
                    >
                        登 录
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="register-link">
                <span>还没有账号？</span>
                <el-button type="text" @click="showRegisterDialog" class="register-btn">
                    立即注册
                </el-button>
            </div>
        </el-card>

        <!-- 注册对话框 -->
        <el-dialog 
            v-model="registerDialogVisible" 
            width="500px" 
            :close-on-click-modal="false"
            class="register-dialog"
        >
            <template #header>
                <div class="dialog-header">
                    <div class="header-icon">
                        <el-icon :size="28"><User /></el-icon>
                    </div>
                    <h2 class="dialog-title">用户注册</h2>
                </div>
            </template>

            <el-form 
                :model="registerForm" 
                :rules="registerRules" 
                ref="registerFormRef"
                class="register-form"
                @submit.prevent="handleRegister"
            >
                <el-form-item label="真实姓名" prop="realName">
                    <el-input 
                        v-model="registerForm.realName" 
                        placeholder="请输入真实姓名"
                        size="large"
                    />
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input 
                        v-model="registerForm.password" 
                        type="password" 
                        placeholder="请输入密码（至少6位）" 
                        size="large"
                        show-password
                    />
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                    <el-input 
                        v-model="registerForm.phone" 
                        placeholder="请输入手机号"
                        size="large"
                    />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                    <el-input 
                        v-model="registerForm.email" 
                        placeholder="请输入邮箱地址"
                        size="large"
                    />
                </el-form-item>

                <el-form-item>
                    <el-button 
                        type="primary" 
                        @click="handleRegister" 
                        :loading="registerLoading"
                        class="register-submit-btn"
                        size="large"
                    >
                        注 册
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <!-- 用户名确认对话框 -->
        <el-dialog 
            v-model="centerDialogVisible" 
            title="注册成功" 
            width="400px" 
            align-center
            class="success-dialog"
        >
            <div class="success-content">
                <div class="success-icon">
                    <el-icon :size="60" color="#67c23a"><CircleCheck /></el-icon>
                </div>
                <h3>欢迎加入失物招领平台！</h3>
                <p class="username-text">您的用户名是：<strong>{{ username }}</strong></p>
                <p class="tip-text">请使用该用户名登录系统</p>
            </div>
            <template #footer>
                <el-button 
                    type="primary" 
                    @click="centerDialogVisible = false"
                    size="large"
                    class="confirm-btn"
                >
                    确认
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
/* 全局容器 - 占满整个屏幕 */
.login-container {
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;
    overflow: hidden;
}

/* 背景装饰圆圈 */
.bg-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    pointer-events: none;
}

.circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 20s infinite ease-in-out;
}

.circle-1 {
    width: 300px;
    height: 300px;
    top: -100px;
    right: -50px;
    animation-delay: 0s;
}

.circle-2 {
    width: 200px;
    height: 200px;
    bottom: -50px;
    left: -50px;
    animation-delay: 5s;
}

.circle-3 {
    width: 150px;
    height: 150px;
    top: 50%;
    left: 20%;
    animation-delay: 10s;
}

@keyframes float {
    0%, 100% {
        transform: translateY(0) rotate(0deg);
    }
    50% {
        transform: translateY(-30px) rotate(180deg);
    }
}

/* 主标题 - 无背景色 */
.main-title {
    font-size: 48px;
    font-weight: 700;
    color: #fff;
    text-align: center;
    margin-bottom: 40px;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    letter-spacing: 8px;
    animation: fadeInDown 1s ease-out;
    z-index: 10;
}

@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 登录卡片 */
.login-card {
    width: 480px;
    border-radius: 20px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    animation: fadeInUp 0.8s ease-out;
    z-index: 10;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 卡片头部 */
.card-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 15px;
    margin-bottom: 35px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f0f0f0;
}

.header-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.header-text {
    font-size: 28px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

/* 表单样式 */
.login-form {
    margin-top: 10px;
}

.login-form .el-form-item {
    margin-bottom: 25px;
}

.login-form :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 12px 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.login-form :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

/* 登录按钮 */
.login-button {
    width: 100%;
    height: 50px;
    font-size: 18px;
    font-weight: 600;
    border-radius: 12px;
    margin-top: 15px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    letter-spacing: 2px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.login-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.login-button:active {
    transform: translateY(0);
}

/* 注册链接 */
.register-link {
    text-align: center;
    margin-top: 25px;
    padding-top: 20px;
    border-top: 1px solid #e8e8e8;
    color: #666;
    font-size: 14px;
}

.register-btn {
    color: #667eea;
    font-weight: 600;
    font-size: 15px;
    margin-left: 8px;
    transition: all 0.3s ease;
}

.register-btn:hover {
    color: #764ba2;
    transform: scale(1.05);
}

/* 注册对话框样式 */
.register-dialog :deep(.el-dialog) {
    border-radius: 20px;
    overflow: hidden;
}

.register-dialog :deep(.el-dialog__header) {
    margin: 0;
    padding: 0;
    border-bottom: none;
}

.dialog-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 25px 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.dialog-header .header-icon {
    width: 45px;
    height: 45px;
}

.dialog-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
}

.register-dialog :deep(.el-dialog__body) {
    padding: 35px 30px 25px;
}

.register-form .el-form-item {
    margin-bottom: 22px;
}

.register-form :deep(.el-form-item__label) {
    font-weight: 500;
    color: #555;
}

.register-form :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.register-submit-btn {
    width: 100%;
    height: 48px;
    font-size: 17px;
    font-weight: 600;
    border-radius: 10px;
    margin-top: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    letter-spacing: 2px;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.register-submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

/* 成功对话框 */
.success-dialog :deep(.el-dialog) {
    border-radius: 20px;
}

.success-content {
    text-align: center;
    padding: 20px 0;
}

.success-icon {
    margin-bottom: 20px;
    animation: scaleIn 0.5s ease-out;
}

@keyframes scaleIn {
    from {
        transform: scale(0);
    }
    to {
        transform: scale(1);
    }
}

.success-content h3 {
    font-size: 22px;
    color: #333;
    margin-bottom: 15px;
    font-weight: 600;
}

.username-text {
    font-size: 16px;
    color: #666;
    margin: 15px 0;
}

.username-text strong {
    color: #667eea;
    font-size: 18px;
}

.tip-text {
    font-size: 14px;
    color: #999;
    margin-top: 10px;
}

.confirm-btn {
    width: 100%;
    height: 45px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.confirm-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .main-title {
        font-size: 36px;
        letter-spacing: 4px;
    }

    .login-card {
        width: 90%;
        max-width: 400px;
        padding: 30px 25px;
    }

    .register-dialog :deep(.el-dialog) {
        width: 90% !important;
    }
}
</style>