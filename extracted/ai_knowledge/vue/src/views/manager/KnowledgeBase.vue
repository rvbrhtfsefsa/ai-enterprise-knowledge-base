<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" @clear="reset" clearable @keyup.enter="load" prefix-icon="Search" style="width: 220px; margin-right: 10px" placeholder="请输入知识库名称"></el-input>
      <el-input v-model="data.code" @clear="reset" clearable @keyup.enter="load" prefix-icon="Search" style="width: 220px; margin-right: 10px" placeholder="请输入知识库编码"></el-input>
      <el-select v-model="data.status" @change="load" clearable @clear="reset" style="width: 160px; margin-right: 10px" placeholder="请选择状态">
        <el-option label="启用" value="启用"></el-option>
        <el-option label="停用" value="停用"></el-option>
      </el-select>
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="知识库名称" min-width="160" />
        <el-table-column prop="code" label="知识库编码" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
        <el-table-column prop="chunkSize" label="切片长度" width="100" />
        <el-table-column prop="overlapSize" label="重叠长度" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template v-slot="scope">
            <el-tag type="success" v-if="scope.row.status === '启用'">启用</el-tag>
            <el-tag type="info" v-else>停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
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

    <el-dialog title="知识库信息" v-model="data.formVisible" width="42%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding: 20px">
        <el-form-item prop="name" label="知识库名称">
          <el-input v-model="data.form.name" placeholder="请输入知识库名称"></el-input>
        </el-form-item>
        <el-form-item prop="code" label="知识库编码">
          <el-input v-model="data.form.code" placeholder="请输入知识库编码，如 PRODUCT_MANUAL"></el-input>
        </el-form-item>
        <el-form-item prop="status" label="状态">
          <el-radio-group v-model="data.form.status">
            <el-radio-button label="启用" value="启用"></el-radio-button>
            <el-radio-button label="停用" value="停用"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="chunkSize" label="切片长度">
          <el-input-number v-model="data.form.chunkSize" :min="100" :max="2000" :step="50" controls-position="right" style="width: 180px"></el-input-number>
        </el-form-item>
        <el-form-item prop="overlapSize" label="重叠长度">
          <el-input-number v-model="data.form.overlapSize" :min="0" :max="1000" :step="10" controls-position="right" style="width: 180px"></el-input-number>
        </el-form-item>
        <el-form-item prop="description" label="描述">
          <el-input type="textarea" :rows="4" v-model="data.form.description" placeholder="请输入知识库用途说明"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
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
  code: null,
  status: null,
  ids: [],
  rules: {
    name: [
      { required: true, message: '请输入知识库名称', trigger: 'blur' }
    ],
    code: [
      { required: true, message: '请输入知识库编码', trigger: 'blur' }
    ],
    status: [
      { required: true, message: '请选择状态', trigger: 'change' }
    ],
    chunkSize: [
      { required: true, message: '请输入切片长度', trigger: 'blur' }
    ],
    overlapSize: [
      { required: true, message: '请输入重叠长度', trigger: 'blur' }
    ]
  }
})

const load = () => {
  request.get('/knowledgeBase/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      code: data.code,
      status: data.status
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
    status: '启用',
    chunkSize: 500,
    overlapSize: 80
  }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/knowledgeBase/add', data.form).then(res => {
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
  request.put('/knowledgeBase/update', data.form).then(res => {
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
      if (data.form.overlapSize >= data.form.chunkSize) {
        ElMessage.warning('重叠长度必须小于切片长度')
        return
      }
      data.form.id ? update() : add()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/knowledgeBase/delete/' + id).then(res => {
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
    request.delete("/knowledgeBase/delete/batch", { data: data.ids }).then(res => {
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
  data.code = null
  data.status = null
  load()
}

load()
</script>
