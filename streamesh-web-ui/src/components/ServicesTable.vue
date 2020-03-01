<template>
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
      <el-table-column prop="id" label="Id" width="300"></el-table-column>
      <el-table-column sortable prop="name" label="Name" width="180"></el-table-column>
      <el-table-column sortable prop="image" label="Image"></el-table-column>
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
            type="text"
            size="small"
          >Details</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>


<script>
export default {
  name: "ServiceTable",
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
      console.log();
      this.$router.push({ path: `/services/${id}` });
    },
    postService: function(file) {
      console.log(file);
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
