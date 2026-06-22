<template>
  <div class="merchant-intro">
    <section class="hero">
      <div class="hero-content">
        <h1>商家入驻</h1>
        <p>加入智慧旅游平台，开启您的数字化经营之旅</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/merchant/register')">
            立即入驻
          </el-button>
          <el-button size="large" @click="$router.push('/merchant/login')">
            商家登录
          </el-button>
        </div>
      </div>
    </section>
    
    <section class="process">
      <h2>入驻流程</h2>
      <el-steps :active="3" align-center>
        <el-step title="注册账号" description="填写商家信息" />
        <el-step title="等待审核" description="管理员审核资质" />
        <el-step title="开始经营" description="管理产品和订单" />
      </el-steps>
    </section>
    
    <section class="advantages">
      <h2>入驻优势</h2>
      <el-row :gutter="40">
        <el-col :span="8">
          <el-card shadow="hover" class="advantage-card">
            <div class="icon">
              <el-icon :size="48" color="#409eff"><Setting /></el-icon>
            </div>
            <h3>自主管理</h3>
            <p>独立管理产品信息、价格、库存，灵活调整经营策略</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="advantage-card">
            <div class="icon">
              <el-icon :size="48" color="#67c23a"><Document /></el-icon>
            </div>
            <h3>订单管理</h3>
            <p>实时查看订单，掌握经营动态，提升服务质量</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="advantage-card">
            <div class="icon">
              <el-icon :size="48" color="#e6a23c"><DataAnalysis /></el-icon>
            </div>
            <h3>数据统计</h3>
            <p>可视化数据分析，助力科学决策，提升经营效率</p>
          </el-card>
        </el-col>
      </el-row>
    </section>
    
    <section class="merchant-types">
      <h2>商家类型</h2>
      <el-row :gutter="40">
        <el-col :span="12">
          <el-card shadow="hover" class="type-card">
            <div class="type-icon">
              <el-icon :size="64" color="#67c23a"><Location /></el-icon>
            </div>
            <h3>景点商家</h3>
            <ul>
              <li>管理景点信息和门票</li>
              <li>设置开放时间和价格</li>
              <li>查看游客订单和评价</li>
              <li>统计游客数量和收入</li>
            </ul>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="type-card">
            <div class="type-icon">
              <el-icon :size="64" color="#409eff"><OfficeBuilding /></el-icon>
            </div>
            <h3>酒店商家</h3>
            <ul>
              <li>管理酒店和房型信息</li>
              <li>设置房间价格和库存</li>
              <li>查看预订订单和评价</li>
              <li>统计入住率和收入</li>
            </ul>
          </el-card>
        </el-col>
      </el-row>
    </section>
    
    <section class="fees">
      <h2>费用说明</h2>
      <el-card>
        <el-row :gutter="40">
          <el-col :span="8">
            <div class="fee-item">
              <div class="fee-label">入驻费用</div>
              <div class="fee-value">免费</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="fee-item">
              <div class="fee-label">平台服务费</div>
              <div class="fee-value">5%</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="fee-item">
              <div class="fee-label">结算周期</div>
              <div class="fee-value">月结</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </section>
    
    <section class="cta">
      <h2>立即开始您的数字化经营</h2>
      <el-button type="primary" size="large" @click="$router.push('/merchant/register')">
        立即入驻
      </el-button>
    </section>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.merchant-intro {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f5f7fa 0%, #fff 100%);
  
  section {
    max-width: 1200px;
    margin: 0 auto;
    padding: 60px 20px;
    
    h2 {
      text-align: center;
      font-size: 32px;
      font-weight: 600;
      color: #333;
      margin-bottom: 40px;
    }
  }
  
  .hero {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    text-align: center;
    padding: 100px 20px;
    
    h1 {
      font-size: 48px;
      font-weight: 700;
      margin-bottom: 20px;
    }
    
    p {
      font-size: 20px;
      margin-bottom: 40px;
      opacity: 0.9;
    }
    
    .hero-actions {
      display: flex;
      gap: 20px;
      justify-content: center;
    }
  }
  
  .process {
    :deep(.el-step__title) {
      font-size: 16px;
    }
  }
  
  .advantages {
    .advantage-card {
      text-align: center;
      padding: 20px;
      transition: transform 0.3s;
      
      &:hover {
        transform: translateY(-10px);
      }
      
      .icon {
        margin-bottom: 20px;
      }
      
      h3 {
        font-size: 20px;
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
      }
      
      p {
        color: #666;
        line-height: 1.6;
      }
    }
  }
  
  .merchant-types {
    background: #f5f7fa;
    
    .type-card {
      text-align: center;
      padding: 30px;
      transition: transform 0.3s;
      
      &:hover {
        transform: translateY(-10px);
      }
      
      .type-icon {
        margin-bottom: 20px;
      }
      
      h3 {
        font-size: 24px;
        font-weight: 600;
        color: #333;
        margin-bottom: 20px;
      }
      
      ul {
        list-style: none;
        padding: 0;
        text-align: left;
        
        li {
          padding: 8px 0;
          color: #666;
          position: relative;
          padding-left: 20px;
          
          &:before {
            content: '✓';
            position: absolute;
            left: 0;
            color: #67c23a;
            font-weight: 600;
          }
        }
      }
    }
  }
  
  .fees {
    .fee-item {
      text-align: center;
      
      .fee-label {
        font-size: 16px;
        color: #666;
        margin-bottom: 12px;
      }
      
      .fee-value {
        font-size: 32px;
        font-weight: 600;
        color: #409eff;
      }
    }
  }
  
  .cta {
    text-align: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    
    h2 {
      color: #fff;
    }
  }
}
</style>
