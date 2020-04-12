<template v-on:show="updateList" >
  <div>
    <el-upload drag action :http-request="postService" accept="application/x-yaml">

    
      <i class="el-icon-upload"></i>
      <div class="el-upload__text">
        Drop yaml here or
        <em>click to upload</em>
      </div>
      <div class="el-upload__tip" slot="tip">yml/yaml files.</div>
    </el-upload>
    <el-table
      :data="tableData.filter(data => !search || data.name.toLowerCase().includes(search.toLowerCase()))"
      stripe
      style="width: 100%"
    >
      <el-table-column sortable prop="type" label="Type" ></el-table-column>
      <el-table-column sortable prop="id" label="Id"></el-table-column>
      <el-table-column sortable prop="name" label="Name" ></el-table-column>
      <el-table-column align="right">
        <template slot="header" slot-scope="scope">
          <el-input
            v-model="search"
            size="mini"
            @input="scope.search"
            placeholder="Type to search"
          />
        </template>
        <template slot-scope="scope">
          <el-button
            @click.native.prevent="goToDetails(scope.row.id)"
            size="small"
          >Details</el-button>
          <el-button
            @click.native.prevent="goToRunTask(scope.row.id)"
            size="small"
          >Run</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>


<script>
export default {
  name: "ServicesTable",
  data: () => {
    return {
      tableData: [],
      search: ""
    }
  },
  mounted() {
    this.updateList();
  },
  methods: {
    updateList: function() {
      fetch("http://localhost:8081/api/v1/definitions")
        .then(response => {
          return response.json();
        })
        .then(json => {
          this.tableData = json;
        });
    },
    goToDetails: function(id) {
      this.$router.push({ path: `/services/${id}` });
    },
    goToRunTask: function(id) {
      this.$router.push({ path: `/services/${id}/instances` });
    },
    postService: function(file) {
      const reader = new FileReader()
      reader.onload = e => {
        fetch("http://localhost:8081/api/v1/definitions", {
          headers: {
            "Content-Type": "application/x-yaml"
          },
          method: "POST",
          body: e.target.result
        }).then(() => {
          this.updateList()
        })
      }

      reader.readAsText(file.file);
    }
  }
}
</script>
