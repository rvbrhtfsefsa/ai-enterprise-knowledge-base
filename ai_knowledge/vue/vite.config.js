import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 按需定制主题配置
    ElementPlus({
      useSource: true,
    }),
    AutoImport({
      resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
    }),
  ],
  // 预加载项目必需的组件
  optimizeDeps: {
    include: [
      "vue",
      "vue-router",
      "axios",
      "element-plus/es/components/base/html",
      "element-plus/es/components/message/html",
      "element-plus/es/components/message-box/html",
      "element-plus/es/components/form/html",
      "element-plus/es/components/form-item/html",
      "element-plus/es/components/button/html",
      "element-plus/es/components/input/html",
      "element-plus/es/components/input-number/html",
      "element-plus/es/components/switch/html",
      "element-plus/es/components/upload/html",
      "element-plus/es/components/menu/html",
      "element-plus/es/components/col/html",
      "element-plus/es/components/icon/html",
      "element-plus/es/components/row/html",
      "element-plus/es/components/tag/html",
      "element-plus/es/components/dialog/html",
      "element-plus/es/components/loading/html",
      "element-plus/es/components/radio/html",
      "element-plus/es/components/radio-group/html",
      "element-plus/es/components/popover/html",
      "element-plus/es/components/scrollbar/html",
      "element-plus/es/components/tooltip/html",
      "element-plus/es/components/dropdown/html",
      "element-plus/es/components/dropdown-menu/html",
      "element-plus/es/components/dropdown-item/html",
      "element-plus/es/components/sub-menu/html",
      "element-plus/es/components/menu-item/html",
      "element-plus/es/components/divider/html",
      "element-plus/es/components/card/html",
      "element-plus/es/components/link/html",
      "element-plus/es/components/breadcrumb/html",
      "element-plus/es/components/breadcrumb-item/html",
      "element-plus/es/components/table/html",
      "element-plus/es/components/tree-select/html",
      "element-plus/es/components/table-column/html",
      "element-plus/es/components/select/html",
      "element-plus/es/components/option/html",
      "element-plus/es/components/pagination/html",
      "element-plus/es/components/tree/html",
      "element-plus/es/components/alert/html",
      "element-plus/es/components/radio-button/html",
      "element-plus/es/components/checkbox-group/html",
      "element-plus/es/components/checkbox/html",
      "element-plus/es/components/tabs/html",
      "element-plus/es/components/tab-pane/html",
      "element-plus/es/components/rate/html",
      "element-plus/es/components/date-picker/html",
      "element-plus/es/components/notification/html",
      "element-plus/es/components/image/html",
      "element-plus/es/components/statistic/html",
      "element-plus/es/components/watermark/html",
      "element-plus/es/components/config-provider/html",
      "element-plus/es/components/text/html",
      "element-plus/es/components/drawer/html",
      "element-plus/es/components/color-picker/html",
    ],
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 自动导入定制化样式文件进行样式覆盖
        additionalData: `
          @use "@/assets/css/index.scss" as *;
        `,
      }
    }
  },
})
