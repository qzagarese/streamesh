<template v-on:show="updateList" >
  <div>
    <el-checkbox v-model="autoRefresh">Auto refresh</el-checkbox>
    <el-table
      :data="tableData.filter(data => !search || data.serviceName.toLowerCase().includes(search.toLowerCase()))"
      stripe
      style="width: 100%"
    >
      <el-table-column sortable prop="serviceName" label="Service" width="180"></el-table-column>
      <el-table-column prop="id" label="Id" width="300"></el-table-column>
      <el-table-column sortable prop="started" label="Started"></el-table-column>
      <el-table-column sortable prop="exited" label="Completed"></el-table-column>
      <el-table-column sortable prop="status" label="Status"></el-table-column>
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
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>


<script>
export default {
  name: "TasksTable",
  data: () => {
    return {
      tableData: [],
      search: "",
      autoRefresh: true
    }
  },
  props: {
    flowInstanceId: String
  },
  mounted() {
    this.updateList();
  },
  methods: {
    updateList: function() {
      var endpoint;
      if (this.flowInstanceId != undefined) {
        endpoint = "http://localhost:8081/api/v1/flow-instances/" + this.flowInstanceId + "/tasks"
      } else {
        endpoint = "http://localhost:8081/api/v1/tasks"
      }
      

      fetch(endpoint)
        .then(response => {
          return response.json();
        })
        .then(json => {
          this.tableData = json;
        });
        if (this.autoRefresh) {
          setTimeout(this.updateList, 5000)
        }
    },
    goToDetails: function(id) {
      this.$router.push({ path: `/tasks/${id}` });
    },
  }
}
</script>
