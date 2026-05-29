<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
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
    email: '',
    role: 0
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
    ],
    role: [
        { required: true, message: '请选择角色', trigger: 'change' }
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
            ElMessage.error(result.message || '登录失败')
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
        email: '',
        role: 0
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
    <span class="title">失物招领平台</span><br>
    <div class="login-container">
        <el-card class="login-card">
            <template #header>
                <div class="login-header">

                    <span>用户登录</span>
                </div>
            </template>
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form"
                @submit.prevent="handleLogin">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable>
                        <template #prefix>
                            <el-icon>
                                <User />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password>
                        <template #prefix>
                            <el-icon>
                                <Lock />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="handleLogin" :loading="loading" class="login-button">
                        登录
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="register-link">
                还没有账号？
                <el-button type="text" @click="showRegisterDialog">
                    立即注册
                </el-button>
            </div>
        </el-card>

        <!-- 注册对话框 -->
        <el-dialog v-model="registerDialogVisible" title="用户注册" width="400px" :close-on-click-modal="false">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef"
                @submit.prevent="handleRegister">
                <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
                </el-form-item>

                <el-form-item label="角色" prop="role">
                    <el-select v-model="registerForm.role" placeholder="请选择角色">
                        <el-option label="普通用户" :value="0" />
                        <el-option label="管理员" :value="1" />
                    </el-select>
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="registerDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="handleRegister" :loading="registerLoading">
                        注册
                    </el-button>
                </span>
            </template>
        </el-dialog>
        <!-- 用户名确认对话框 -->
        <el-dialog v-model="centerDialogVisible" title="欢迎" width="500" align-center>
            <span>您的用户名是:{{ username }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="centerDialogVisible = false">
                        确认
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(0px);
  }
}

.title {
  font-size: 42px;
  font-weight: bold;
  color: #fff;
  text-align: center;
  display: block;
  margin-bottom: 30px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  animation: fadeIn 1s ease-out;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
  animation: rotate 20s linear infinite;
  z-index: 0;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-card {
  width: 420px;
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;
  animation: fadeIn 0.8s ease-out;
  z-index: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
}

.login-header {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  padding: 25px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: relative;
}

.login-header::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background: #409eff;
  border-radius: 3px;
}

.login-form {
  margin-top: 20px;
  padding: 0 30px 20px;
}

.login-form .el-form-item {
  margin-bottom: 25px;
}

.login-form .el-form-item__label {
  color: #555;
  font-weight: 500;
}

.login-button {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 500;
  letter-spacing: 1px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.register-link {
  text-align: center;
  margin-top: 25px;
  color: #666;
  padding: 20px;
  border-top: 1px solid #eee;
  font-size: 14px;
}

.register-link .el-button--text {
  color: #667eea;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
}

.register-link .el-button--text:hover {
  color: #764ba2;
  transform: scale(1.05);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 20px 20px;
}

.dialog-footer .el-button {
  border-radius: 6px;
  padding: 8px 20px;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}
</style>