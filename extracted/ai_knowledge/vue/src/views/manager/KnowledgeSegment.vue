<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.content" @clear="reset" clearable @keyup.enter="load" prefix-icon="Search" style="width: 220px; margin-right: 10px" placeholder="请输入片段内容"></el-input>
      <el-select v-model="data.knowledgeBaseId" @change="loadDocument(); load()" clearable @clear="reset" style="width: 220px; margin-right: 10px" placeholder="请选择知识库">
        <el-option v-for="item in data.knowledgeBaseList" :key="item.id" :label="item.name" :value="item.id"></el-option>
      </el-select>
      <el-select v-model="data.documentId" @change="load" clearable @clear="reset" style="width: 240px; margin-right: 10px" placeholder="请选择文档">
        <el-option v-for="item in data.documentList" :key="item.id" :label="item.title" :value="item.id"></el-option>
      </el-select>
      <el-select v-model="data.vectorStatus" @change="load" clearable @clear="reset" style="width: 160px; margin-right: 10px" placeholder="向量化状态">
        <el-option label="未向量化" value="未向量化"></el-option>
        <el-option label="已向量化" value="已向量化"></el-option>
        <el-option label="向量化失败" value="向量化失败"></el-option>
      </el-select>
      <el-select v-model="data.modelConfigId" clearable style="width: 200px; margin-right: 10px" placeholder="请选择向量模型">
        <el-option v-for="item in data.vectorModelList" :key="item.id" :label="item.name + '（' + item.modelName + '）'" :value="item.id"></el-option>
      </el-select>
      <el-button type="warning" plain :loading="data.vectorLoading" @click="vectorizeBatch">批量向量化</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange" tooltip-effect="dark myEffect">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="knowledgeBaseName" label="所属知识库" min-width="150" />
        <el-table-column prop="documentTitle" label="文档标题" min-width="170" />
        <el-table-column prop="segmentIndex" label="序号" width="80" />
        <el-table-column prop="content" label="片段内容" min-width="360" show-overflow-tooltip />
        <el-table-column prop="charCount" label="字符数" width="90" />
        <el-table-column prop="vectorStatus" label="向量化状态" width="120">
          <template v-slot="scope">
            <el-tag type="info" v-if="scope.row.vectorStatus === '未向量化'">未向量化</el-tag>
            <el-tag type="success" v-else-if="scope.row.vectorStatus === '已向量化'">已向量化</el-tag>
            <el-tag type="danger" v-else>向量化失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template v-slot="scope">
            <el-button type="warning" circle :icon="MagicStick" :loading="data.currentVectorId === scope.row.id" @click="vectorizeOne(scope.row)"></el-button>
            <el-button type="info" circle :icon="View" @click="viewContent(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="知识片段内容" v-model="data.contentVisible" width="55%" destroy-on-close>
      <el-input type="textarea" :rows="18" readonly v-model="data.currentContent" placeholder="暂无片段内容"></el-input>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, MagicStick, View } from "@element-plus/icons-vue";

const data = reactive({
  contentVisible: false,
  currentContent: '',
  tableData: [],
  knowledgeBaseList: [],
  documentList: [],
  vectorModelList: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  content: null,
  knowledgeBaseId: null,
  documentId: null,
  vectorStatus: null,
  modelConfigId: null,
  vectorLoading: false,
  currentVectorId: null,
  ids: []
})

const loadKnowledgeBase = () => {
  request.get('/knowledgeBase/selectAll').then(res => {
    if (res.code === '200') {
      data.knowledgeBaseList = res.data || []
    }
  })
}

const loadDocument = () => {
  data.documentId = null
  request.get('/knowledgeDocument/selectAll', {
    params: {
      knowledgeBaseId: data.knowledgeBaseId
    }
  }).then(res => {
    if (res.code === '200') {
      data.documentList = res.data || []
    }
  })
}

const loadVectorModel = () => {
  request.get('/aiModelConfig/selectAll', {
    params: {
      modelType: '向量模型',
      enabled: '是'
    }
  }).then(res => {
    if (res.code === '200') {
      data.vectorModelList = res.data || []
      if (!data.modelConfigId && data.vectorModelList.length) {
        data.modelConfigId = data.vectorModelList[0].id
      }
    }
  })
}

const load = () => {
  request.get('/knowledgeSegment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      content: data.content,
      knowledgeBaseId: data.knowledgeBaseId,
      documentId: data.documentId,
      vectorStatus: data.vectorStatus
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}

const checkModel = () => {
  if (!data.modelConfigId) {
    ElMessage.warning('请选择向量模型配置')
    return false
  }
  return true
}

const vectorizeOne = (row) => {
  if (!checkModel()) {
    return
  }
  data.currentVectorId = row.id
  request.post('/knowledgeSegment/vectorize/' + row.id, null, {
    params: {
      modelConfigId: data.modelConfigId
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('向量化成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  }).finally(() => {
    data.currentVectorId = null
  })
}

const vectorizeBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning('请选择知识片段')
    return
  }
  if (!checkModel()) {
    return
  }
  data.vectorLoading = true
  request.post('/knowledgeSegment/vectorize/batch', {
    ids: data.ids,
    modelConfigId: data.modelConfigId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('向量化成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  }).finally(() => {
    data.vectorLoading = false
  })
}

const viewContent = (row) => {
  data.currentContent = row.content || ''
  data.contentVisible = true
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/knowledgeSegment/delete/' + id).then(res => {
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
    request.delete("/knowledgeSegment/delete/batch", { data: data.ids }).then(res => {
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
  data.content = null
  data.knowledgeBaseId = null
  data.documentId = null
  data.vectorStatus = null
  loadDocument()
  load()
}

loadKnowledgeBase()
loadDocument()
loadVectorModel()
load()
</script>
<style>
.myEffect {
  max-width: 30%;
}
</style>
