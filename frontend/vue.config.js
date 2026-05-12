module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      // API 请求代理
      '/api': {
        target: 'http://localhost:8087',
        changeOrigin: true,
        pathRewrite: { '^/api': '/api' }
      },

      '/images': {
        target: 'http://localhost:8087',
        changeOrigin: true,
        pathRewrite: { '^/images': '/images' }
      }
    }
  }
}