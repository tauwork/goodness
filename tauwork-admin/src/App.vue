<template>
  <router-view />
</template>

<style>
:root {
  --el-font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  --el-font-size-base: 13px;
  --el-font-size-small: 12px;
  --el-font-size-extra-small: 12px;
  --el-font-size-medium: 15px;
  --el-font-size-large: 16px;
  --el-font-weight-primary: 600;
  --el-text-color-primary: #22304A;
  --el-text-color-regular: #22304A;
  --el-text-color-secondary: #6B7C93;
  --el-text-color-placeholder: #8A94A8;
}
html {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
body, button, input, textarea, select {
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  color: #22304A;
}
b, strong { font-weight: 600; }
.el-card__header {
  font-size: 16px;
  font-weight: 600;
  color: #22304A;
}
.el-table {
  --el-table-header-text-color: #22304A;
  color: #22304A;
}
.el-table th.el-table__cell { font-weight: 600; }
.el-button { font-weight: 600; }
</style>
