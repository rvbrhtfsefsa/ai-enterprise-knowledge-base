<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.title" @clear="reset" clearable @keyup.enter="load" prefix-icon="Search" style="width: 220px; margin-right: 10px" placeholder="请输入文档标题"></el-input>
      <el-select v-model="data.knowledgeBaseId" @change="load" clearable @clear="reset" style="width: 220px; margin-right: 10px" placeholder="请选择知识库">
        <el-option v-for="item in data.knowledgeBaseList" :key="item.id" :label="item.name" :value="item.id"></el-option>
      </el-select>
      <el-select v-model="data.parseStatus" @change="load" clearable @clear="reset" style="width: 160px; margin-right: 10px" placeholder="解析状态">
        <el-option label="待解析" value="待解析"></el-option>
        <el-option label="已解析" value="已解析"></el-option>
        <el-option label="解析失败" value="解析失败"></el-option>
      </el-select>
      <el-button type="primary" plain @click="handleAdd">上传文档</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="文档标题" min-width="180" />
        <el-table-column prop="knowledgeBaseName" label="所属知识库" min-width="160" />
        <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="90" />
        <el-table-column prop="fileSize" label="大小" width="100" />
        <el-table-column prop="parseStatus" label="解析状态" width="110">
          <template v-slot="scope">
            <el-tag type="warning" v-if="scope.row.parseStatus === '待解析'">待解析</el-tag>
            <el-tag type="success" v-else-if="scope.row.parseStatus === '已解析'">已解析</el-tag>
            <el-tag type="danger" v-else>解析失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vectorStatus" label="向量化状态" width="120">
          <template v-slot="scope">
            <el-tag type="info" v-if="scope.row.vectorStatus === '未向量化'">未向量化</el-tag>
            <el-tag type="success" v-else-if="scope.row.vectorStatus === '已向量化'">已向量化</el-tag>
            <el-tag type="danger" v-else>向量化失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template v-slot="scope">
            <el-tooltip content="解析文档" placement="top">
              <el-button type="warning" circle :icon="Reading" @click="parse(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="切分片段" placement="top">
              <el-button type="primary" circle :icon="Scissor" @click="generateSegment(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看解析文本" placement="top">
              <el-button type="info" circle :icon="View" @click="viewParsedText(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="下载文件" placement="top">
              <el-button type="success" circle :icon="Download" @click="download(scope.row.fileUrl)"></el-button>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="知识文档信息" v-model="data.formVisible" width="44%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding: 20px">
        <el-form-item prop="knowledgeBaseId" label="所属知识库">
          <el-select v-model="data.form.knowledgeBaseId" style="width: 100%" placeholder="请选择知识库">
            <el-option v-for="item in data.knowledgeBaseList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="title" label="文档标题">
          <el-input v-model="data.form.title" placeholder="请输入文档标题"></el-input>
        </el-form-item>
        <el-form-item prop="fileUrl" label="文档文件">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleFileUpload"
              :show-file-list="false"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
          <div class="file-info" v-if="data.form.fileName">
            {{ data.form.fileName }} · {{ data.form.fileSize }}
          </div>
        </el-form-item>
        <el-form-item label="解析状态">
          <el-tag type="warning" v-if="data.form.parseStatus === '待解析'">待解析</el-tag>
          <el-tag type="success" v-else-if="data.form.parseStatus === '已解析'">已解析</el-tag>
          <el-tag type="danger" v-else>解析失败</el-tag>
        </el-form-item>
        <el-form-item label="向量化状态">
          <el-tag type="info" v-if="data.form.vectorStatus === '未向量化'">未向量化</el-tag>
          <el-tag type="success" v-else-if="data.form.vectorStatus === '已向量化'">已向量化</el-tag>
          <el-tag type="danger" v-else>向量化失败</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="解析文本" v-model="data.textVisible" width="55%" destroy-on-close>
      <el-input type="textarea" :rows="18" readonly v-model="data.parsedText" placeholder="当前文档还没有解析文本"></el-input>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Download, Edit, Reading, Scissor, View } from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()

const data = reactive({
  formVisible: false,
  textVisible: false,
  form: {},
  parsedText: '',
  tableData: [],
  knowledgeBaseList: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  title: null,
  knowledgeBaseId: null,
  parseStatus: null,
  ids: [],
  rules: {
    knowledgeBaseId: [
      { required: true, message: '请选择知识库', trigger: 'change' }
    ],
    title: [
      { required: true, message: '请输入文档标题', trigger: 'blur' }
    ],
    fileUrl: [
      { required: true, message: '请上传文档文件', trigger: 'change' }
    ]
  }
})

const loadKnowledgeBase = () => {
  request.get('/knowledgeBase/selectAll', {
    params: {
      status: '启用'
    }
  }).then(res => {
    if (res.code === '200') {
      data.knowledgeBaseList = res.data || []
    }
  })
}

const load = () => {
  request.get('/knowledgeDocument/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      knowledgeBaseId: data.knowledgeBaseId,
      parseStatus: data.parseStatus
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
    parseStatus: '待解析',
    vectorStatus: '未向量化'
  }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/knowledgeDocument/add', data.form).then(res => {
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
  request.put('/knowledgeDocument/update', data.form).then(res => {
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
    request.delete('/knowledgeDocument/delete/' + id).then(res => {
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
    request.delete("/knowledgeDocument/delete/batch", { data: data.ids }).then(res => {
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

const parse = (row) => {
  request.post('/knowledgeDocument/parse/' + row.id).then(res => {
    if (res.code === '200') {
      // 只提示解析成功，不再自动弹出文本框；查看文本由“查看解析文本”按钮负责
      ElMessage.success('解析成功')
      load()
    } else {
      ElMessage.error(res.msg)
      load()
    }
  })
}

const generateSegment = (row) => {
  request.post('/knowledgeSegment/generate/' + row.id).then(res => {
    if (res.code === '200') {
      ElMessage.success('生成成功，共 ' + (res.data?.length || 0) + ' 个片段')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const viewParsedText = (row) => {
  data.parsedText = row.parsedText || ''
  data.textVisible = true
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const handleFileUpload = (res, file) => {
  data.form.fileUrl = res.data
  data.form.fileName = file.name
  data.form.fileType = getFileType(file.name)
  data.form.fileSize = formatFileSize(file.size)
  data.form.parsedText = null
  data.form.parseTime = null
  data.form.parseStatus = '待解析'
  data.form.vectorStatus = '未向量化'
  if (!data.form.title) {
    data.form.title = file.name.replace(/\.[^/.]+$/, '')
  }
}

const getFileType = (fileName) => {
  const index = fileName.lastIndexOf('.')
  return index > -1 ? fileName.substring(index + 1).toLowerCase() : 'unknown'
}

const formatFileSize = (size) => {
  if (!size) {
    return '0KB'
  }
  if (size < 1024 * 1024) {
    return Math.ceil(size / 1024) + 'KB'
  }
  return (size / 1024 / 1024).toFixed(2) + 'MB'
}

const download = (url) => {
  if (url) {
    window.open(url)
  }
}

const reset = () => {
  data.title = null
  data.knowledgeBaseId = null
  data.parseStatus = null
  load()
}

loadKnowledgeBase()
load()
</script>

<style scoped>
.file-info {
  margin-left: 12px;
  color: #666;
  font-size: 13px;
}
</style>
