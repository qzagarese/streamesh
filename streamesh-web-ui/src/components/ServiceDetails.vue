<template v-on:show="updateList">
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ details.name }}</span>
        <el-button style="float: right; padding: 3px 0" type="text">Run task</el-button>
      </div>
      <div class="text item">
        <b>Description:</b>
        {{ (details.description === undefined) ? 'Not provided' : details.description }}
      </div>
      <div class="text item">
        <b>Image name:</b>
        {{ details.image }}
      </div>
      <div class="text item">
        <b>Image id:</b>
        {{ details.imageId }}
      </div>
      <div class="text item">
        <b>Command:</b>
        {{ details.inputMapping.baseCmd}}
      </div>

            <el-divider></el-divider>

      <el-table :data="details.inputMapping.parameters" stripe style="width: 100%">
        <el-table-column label="Input">
          <el-table-column prop="name" label="Name"></el-table-column>
          <el-table-column prop="internalName" label="Command line"></el-table-column>
          <el-table-column prop="optional" label="Optional">
            <template slot-scope="scope">{{scope.row.optional == 'true'? 'Yes' : 'No'}}</template>
          </el-table-column>
          <el-table-column prop="repeatable" label="Multiple values">
            <template slot-scope="scope">{{scope.row.repeatable == 'true'? 'Yes' : 'No'}}</template>
          </el-table-column>
        </el-table-column>
      </el-table>
      <el-divider></el-divider>
      <el-table :data="details.outputMapping" stripe style="width: 100%">
        <el-table-column label="Output">
          <el-table-column prop="name" label="Name"></el-table-column>
          <el-table-column prop="outputDir" label="Container output directory"></el-table-column>
          <el-table-column prop="fileNamePattern" label="File name pattern"></el-table-column>
        </el-table-column>
      </el-table>
    </el-card>
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
    this.updateDetails();
  },
  methods: {
    updateDetails: function() {
      fetch("http://localhost:8081/api/v1/definitions/" + this.id)
        .then(response => {
          return response.json();
        })
        .then(json => {
          this.details = json;
        });
    }
  }
};
</script>

<style>
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

.box-card {
  width: 80%;
}
</style>
