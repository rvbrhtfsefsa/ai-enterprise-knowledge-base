<template>
  <div>
    <div class="card" style="margin-bottom: 5px">您好！{{ data.user?.name }}，欢迎使用本系统！</div>
  </div>
</template>

<script setup>

import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('sys-user') || '{}'),
  noticeData: []
})

</script>