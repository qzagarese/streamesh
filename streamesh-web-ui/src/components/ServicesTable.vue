<template>
  <el-table :data="tableData" stripe style="width: 100%">
    <el-table-column prop="id" label="Id" width="300"></el-table-column>
    <el-table-column prop="name" label="Name" width="180"></el-table-column>
    <el-table-column prop="image" label="Image"></el-table-column>
    <el-table-column
      fixed="right"
      label="Operations"
      width="120">
      <template slot-scope="scope">
        <el-button
          @click.native.prevent="goToDetails(scope.row.id)"
          type="text"
          size="small">
          Details
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>


<script>
export default {
  name: "ServiceTable",
  data: () => {
    return {
      tableData: []
    };
  },
  mounted() {
    fetch("http://localhost:8081/api/v1/definitions")
      .then(response => {
        return response.json();
      })
      .then(json => {
        this.tableData = json;
      });
  },
  methods: {
    goToDetails: function(id) {
      console.log()
      this.$router.push({ path: `/services/${id}` });
    }
  }
};
</script>
