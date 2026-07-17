<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-section">
        <h4>关于我们</h4>
        <ul>
          <li><a href="#">公司介绍</a></li>
          <li><a href="#">联系我们</a></li>
          <li><a href="#">人才招聘</a></li>
        </ul>
      </div>
      <div class="footer-section">
        <h4>帮助中心</h4>
        <ul>
          <li><a href="#">常见问题</a></li>
          <li><a href="#">服务条款</a></li>
          <li><a href="#">隐私政策</a></li>
        </ul>
      </div>
      <div class="footer-section">
        <h4>服务支持</h4>
        <ul>
          <li><a href="#">用户反馈</a></li>
          <li><a href="#">意见建议</a></li>
          <li><a href="#">在线客服</a></li>
        </ul>
      </div>
      <div class="footer-section">
        <h4>关注我们</h4>
        <ul>
          <li><a href="#">官方微博</a></li>
          <li><a href="#">官方微信</a></li>
          <li><a href="#">关注订阅号</a></li>
        </ul>
      </div>
    </div>
    <div class="copyright">
      <p>©2024 健康之路 All Rights Reserved | 400-888-8888</p>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style lang="scss" scoped>
.footer {
  background-color: #2c3e50;
  color: white;
  padding: 40px 0 20px;
  margin-top: 50px;
  
  .footer-content {
    width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    padding-bottom: 30px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .footer-section {
    flex: 1;
    padding: 0 20px;
    
    h4 {
      font-size: 16px;
      margin-bottom: 20px;
      color: white;
    }
    
    ul {
      list-style: none;
      
      li {
        margin-bottom: 10px;
        font-size: 14px;
        color: rgba(255, 255, 255, 0.7);
        
        a {
          color: rgba(255, 255, 255, 0.7);
          text-decoration: none;
          transition: color 0.3s;
          
          &:hover {
            color: white;
          }
        }
      }
    }
  }
  
  .copyright {
    text-align: center;
    padding-top: 20px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>
