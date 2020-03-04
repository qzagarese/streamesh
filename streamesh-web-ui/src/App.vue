<template>
  <div id="app">
    <el-header>
        <el-image
      style="width: 150px; height: 50px"
      src="./assets/streamesh.png"></el-image>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu>
          <router-link to="/services">
            <el-menu-item index="1">
              <template slot="title">
                <i class="el-icon-eleme"></i>Services
              </template>
            </el-menu-item>
          </router-link>
          <router-link to="/tasks">
            <el-menu-item index="2">
              <template slot="title">
                <i class="el-icon-menu"></i>Tasks
              </template>
            </el-menu-item>
          </router-link>
          <el-menu-item index="3">
            <template slot="title">
              <i class="el-icon-film"></i>Flows
            </template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "App" 
};
</script>

<!-- <style>
.el-header,
.el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  line-height: 200px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  text-align: center;
  line-height: 160px;
}

body > .el-container {
  margin-bottom: 40px;
}

.el-container:nth-child(5) .el-aside,
.el-container:nth-child(6) .el-aside {
  line-height: 260px;
}

.el-container:nth-child(7) .el-aside {
  line-height: 320px;
}
</style> -->
