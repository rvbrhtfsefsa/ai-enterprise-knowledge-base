<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" @clear="reset" clearable @keyup.enter="load" prefix-icon="Search" style="width: 220px; margin-right: 10px" placeholder="请输入配置名称"></el-input>
      <el-select v-model="data.modelType" clearable placeholder="请选择模型类型" style="width: 160px; margin-right: 10px" @change="load" @clear="reset">
        <el-option label="聊天模型" value="聊天模型"></el-option>
        <el-option label="向量模型" value="向量模型"></el-option>
      </el-select>
      <el-input v-model="data.provider" @clear="reset" clearable @keyup.enter="load" prefix-icon="Search" style="width: 180px; margin-right: 10px" placeholder="请输入服务商"></el-input>
      <el-select v-model="data.enabled" clearable placeholder="请选择状态" style="width: 140px; margin-right: 10px" @change="load" @clear="reset">
        <el-option label="启用" value="是"></el-option>
        <el-option label="停用" value="否"></el-option>
      </el-select>
      <el-button type="primary" plain @click="load">查询</el-button>
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="配置名称" min-width="150" />
        <el-table-column prop="modelType" label="模型类型" width="110">
          <template v-slot="scope">
            <el-tag type="primary" v-if="scope.row.modelType === '聊天模型'">聊天模型</el-tag>
            <el-tag type="warning" v-else>向量模型</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="服务商" width="120" />
        <el-table-column prop="modelName" label="模型名称" min-width="180" />
        <el-table-column prop="baseUrl" label="接口地址" min-width="240" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="90">
          <template v-slot="scope">
            <el-tag type="success" v-if="scope.row.enabled === '是'">启用</el-tag>
            <el-tag type="info" v-else>停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="AI模型配置" v-model="data.formVisible" width="45%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="95px" style="padding: 20px">
        <el-form-item prop="name" label="配置名称">
          <el-input v-model="data.form.name" placeholder="例如：百炼向量模型配置"></el-input>
        </el-form-item>
        <el-form-item prop="modelType" label="模型类型">
          <el-radio-group v-model="data.form.modelType">
            <el-radio-button label="聊天模型" value="聊天模型"></el-radio-button>
            <el-radio-button label="向量模型" value="向量模型"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="provider" label="服务商">
          <el-select v-model="data.form.provider" placeholder="请选择服务商" style="width: 100%">
            <el-option label="DeepSeek" value="DeepSeek"></el-option>
            <el-option label="阿里云百炼" value="阿里云百炼"></el-option>
            <el-option label="OpenAI" value="OpenAI"></el-option>
            <el-option label="智谱AI" value="智谱AI"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="baseUrl" label="接口地址">
          <el-input v-model="data.form.baseUrl" placeholder="例如：https://dashscope.aliyuncs.com/compatible-mode/v1"></el-input>
        </el-form-item>
        <el-form-item prop="apiKey" label="API Key">
          <el-input v-model="data.form.apiKey" type="password" show-password placeholder="请输入 API Key"></el-input>
        </el-form-item>
        <el-form-item prop="modelName" label="模型名称">
          <el-input v-model="data.form.modelName" placeholder="例如：text-embedding-v4"></el-input>
        </el-form-item>
        <el-form-item prop="enabled" label="状态">
          <el-radio-group v-model="data.form.enabled">
            <el-radio label="是">启用</el-radio>
            <el-radio label="否">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="remark" label="备注">
          <el-input v-model="data.form.remark" type="textarea" :rows="3" placeholder="记录这个配置的使用场景"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit } from "@element-plus/icons-vue";

const formRef = ref()

const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  name: null,
  modelType: null,
  provider: null,
  enabled: null,
  ids: [],
  rules: {
    name: [
      { required: true, message: '请输入配置名称', trigger: 'blur' }
    ],
    modelType: [
      { required: true, message: '请选择模型类型', trigger: 'change' }
    ],
    provider: [
      { required: true, message: '请选择服务商', trigger: 'change' }
    ],
    baseUrl: [
      { required: true, message: '请输入接口地址', trigger: 'blur' }
    ],
    apiKey: [
      { required: true, message: '请输入 API Key', trigger: 'blur' }
    ],
    modelName: [
      { required: true, message: '请输入模型名称', trigger: 'blur' }
    ],
    enabled: [
      { required: true, message: '请选择状态', trigger: 'change' }
    ]
  }
})

const load = () => {
  request.get('/aiModelConfig/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      modelType: data.modelType,
      provider: data.provider,
      enabled: data.enabled
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}

const handleAdd = () => {
  data.form = {
    modelType: '聊天模型',
    enabled: '是'
  }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/aiModelConfig/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/aiModelConfig/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/aiModelConfig/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/aiModelConfig/delete/batch", { data: data.ids }).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.name = null
  data.modelType = null
  data.provider = null
  data.enabled = null
  load()
}

load()
</script>
