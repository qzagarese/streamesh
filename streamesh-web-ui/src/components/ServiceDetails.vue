<template>
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ details.name }}</span>
        
      </div>
      <div class="text item">{{'Description: ' + ((details.description === undefined) ? 'Not provided' : details.description )}}</div>
      <div class="text item">{{'Image: ' + details.image }}</div>

    </el-card>

    <!-- <el-table :data="details" stripe style="width: 100%">
      <el-table-column prop="id" label="Id"></el-table-column>
      <el-table-column prop="name" label="Name"></el-table-column>
      <el-table-column prop="image" label="Image"></el-table-column>
      <el-table-column prop="imageId" label="Image id"></el-table-column>
    </el-table> -->
  </div>
</template>


<script>
export default {
  name: "ServiceDetails",
  data: () => {
    return {
      details: {}
    };
  },
  props: {
      id: String
  },
  mounted() {
    fetch("http://localhost:8081/api/v1/definitions/" + this.id)
      .then(response => {
        return response.json();
      })
      .then(json => {
        this.details = json;
      });
  }
};
</script>