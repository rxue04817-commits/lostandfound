<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const tradeNo = ref('')
const outTradeNo = ref('')

onMounted(() => {
  // 支付宝同步回调的参数在 URL query 中
  tradeNo.value = route.query.trade_no || ''
  outTradeNo.value = route.query.out_trade_no || ''
})

const goHome = () => {
  router.push('/layout')
}

const goMyPosts = () => {
  router.push('/my-posts')
}
</script>

<template>
  <div class="return-container">
    <el-result
      icon="success"
      title="支付成功"
      sub-title="感谢您的慷慨打赏！祝您生活愉快！"
    >
      <template #extra>
        <p v-if="tradeNo">支付宝交易号: {{ tradeNo }}</p>
        <p v-if="outTradeNo">商户订单号: {{ outTradeNo }}</p>
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="goMyPosts">返回我的发布</el-button>
          <el-button @click="goHome">返回首页</el-button>
        </div>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.return-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
</style>