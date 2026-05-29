<template>
   <RouterView></RouterView>
</template>
<script>
export default {
  name: 'App'
}
</script>
<style >
.nav-links {
  margin-bottom: 15px;
}
body {
  background-image: url('img/image.png');
  background-size: cover; /* 完全覆盖容器 */
  background-position: center; /* 居中显示 */
  background-repeat: no-repeat; /* 不重复 */
  background-attachment: fixed; /* 固定背景 */
  margin: 0;
  padding: 0;
}
body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(142, 102, 104, 0.691); /* 白色半透明遮罩 */
  z-index: -1;
}
</style>


