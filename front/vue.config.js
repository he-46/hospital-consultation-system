module.exports = {
  css: {
    loaderOptions: {
      sass: {
        api: 'modern'
      }
    }
  },
  devServer: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
}
