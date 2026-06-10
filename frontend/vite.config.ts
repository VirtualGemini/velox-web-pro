import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath } from 'url'
import viteCompression from 'vite-plugin-compression'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import ElementPlus from 'unplugin-element-plus/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import tailwindcss from '@tailwindcss/vite'
// import { visualizer } from 'rollup-plugin-visualizer'

export default async ({ mode }: { mode: string }) => {
  const root = process.cwd()
  const env = loadEnv(mode, root)
  const { VITE_VERSION, VITE_PORT, VITE_BASE_URL, VITE_API_URL, VITE_API_PROXY_URL } = env
  const isDevelopment = mode === 'development'
  const devToolsPlugins = []

  if (isDevelopment) {
    ensureConfigLocalStorage()
    const { default: vueDevTools } = await import('vite-plugin-vue-devtools')
    devToolsPlugins.push(vueDevTools())
  }

  console.log(`🚀 API_URL = ${VITE_API_URL}`)
  console.log(`🚀 VERSION = ${VITE_VERSION}`)

  return defineConfig({
    define: {
      __APP_VERSION__: JSON.stringify(VITE_VERSION)
    },
    base: VITE_BASE_URL,
    server: {
      port: Number(VITE_PORT),
      // CSP 以 Report-Only 观察违规（不阻断 SPA），违规输出到浏览器控制台。
      // Report-Only 只能由 HTTP 响应头下发（<meta> 不支持），故由 dev server 注入；
      // dev 含 'unsafe-eval'（Vite/devtools/依赖预构建在开发期会 eval）与 ws/wss（HMR），避免误伤。
      // 生产环境改由静态服务器/反代下发同名头，且通常无需 'unsafe-eval'。
      headers: {
        'Content-Security-Policy-Report-Only':
          "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' http://localhost:* https: ws://localhost:* wss://localhost:*; frame-src 'self'; object-src 'none'; base-uri 'self'"
      },
      proxy: {
        '/api': {
          target: VITE_API_PROXY_URL,
          changeOrigin: true,
          xfwd: true
        }
      },
      host: true,
      allowedHosts: true
    },
    // 路径别名
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '@views': resolvePath('src/views'),
        '@imgs': resolvePath('src/assets/images'),
        '@icons': resolvePath('src/assets/icons'),
        '@utils': resolvePath('src/utils'),
        '@stores': resolvePath('src/store'),
        '@styles': resolvePath('src/assets/styles')
      }
    },
    build: {
      target: 'es2015',
      outDir: 'dist',
      chunkSizeWarningLimit: 2000,
      minify: 'terser',
      terserOptions: {
        compress: {
          // 生产环境去除 console
          drop_console: true,
          // 生产环境去除 debugger
          drop_debugger: true
        }
      },
      dynamicImportVarsOptions: {
        warnOnError: true,
        exclude: [],
        include: ['src/views/**/*.vue']
      }
    },
    plugins: [
      vue(),
      tailwindcss(),
      // 自动按需导入 API
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia', '@vueuse/core'],
        dts: 'src/types/import/auto-imports.d.ts',
        resolvers: [ElementPlusResolver()],
        eslintrc: {
          enabled: true,
          filepath: './.auto-import.json',
          globalsPropValue: true
        }
      }),
      // 自动按需导入组件
      Components({
        dts: 'src/types/import/components.d.ts',
        resolvers: [ElementPlusResolver()]
      }),
      // 按需定制主题配置
      ElementPlus({
        useSource: true
      }),
      // 压缩
      viteCompression({
        verbose: false, // 是否在控制台输出压缩结果
        disable: false, // 是否禁用
        algorithm: 'gzip', // 压缩算法
        ext: '.gz', // 压缩后的文件名后缀
        threshold: 10240, // 只有大小大于该值的资源会被处理 10240B = 10KB
        deleteOriginFile: false // 压缩后是否删除原文件
      }),
      ...devToolsPlugins
      // 打包分析
      // visualizer({
      //   open: true,
      //   gzipSize: true,
      //   brotliSize: true,
      //   filename: 'dist/stats.html' // 分析图生成的文件名及路径
      // }),
    ],
    // 依赖预构建：避免运行时重复请求与转换，提升首次加载速度
    optimizeDeps: {
      include: [
        'echarts/core',
        'echarts/charts',
        'echarts/components',
        'echarts/renderers',
        'xlsx',
        'xgplayer',
        'crypto-js',
        'file-saver',
        'vue-img-cutter',
        'element-plus/es',
        'element-plus/es/components/*/style/css',
        'element-plus/es/components/*/style/index'
      ]
    },
    css: {
      preprocessorOptions: {
        // sass variable and mixin
        scss: {
          additionalData: `
            @use "@styles/core/el-light.scss" as *; 
            @use "@styles/core/mixin.scss" as *;
          `
        }
      },
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  })
}

function resolvePath(paths: string) {
  return path.resolve(__dirname, paths)
}

type ConfigLocalStorage = {
  getItem?: (key: string) => string | null
  setItem?: (key: string, value: string) => void
  removeItem?: (key: string) => void
  clear?: () => void
  key?: (index: number) => string | null
  length?: number
}

function ensureConfigLocalStorage() {
  const descriptor = Object.getOwnPropertyDescriptor(globalThis, 'localStorage')
  const current =
    descriptor && 'value' in descriptor ? (descriptor.value as ConfigLocalStorage) : undefined
  if (current && typeof current.getItem === 'function') {
    return
  }

  const store = new Map<string, string>()
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      get length() {
        return store.size
      },
      getItem(key: string) {
        return store.has(key) ? store.get(key)! : null
      },
      setItem(key: string, value: string) {
        store.set(key, String(value))
      },
      removeItem(key: string) {
        store.delete(key)
      },
      clear() {
        store.clear()
      },
      key(index: number) {
        return Array.from(store.keys())[index] ?? null
      }
    }
  })
}
