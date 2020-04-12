<template v-on:show="updateList">
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ details.name }}</span>
        <el-button v-show="isMicropipe()" style="float: right; padding: 3px 0" type="text" 
        @click="goToTaskCreation(id)">Run task</el-button>
        <el-button v-show="!isMicropipe()" style="float: right; padding: 3px 0" type="text" 
        @click="goToTaskCreation(id)">Run flow</el-button>
      </div>
      <div class="text item">
        <b>Id:</b>
        {{ details.id }}
      </div>
      <div class="text item">
        <b>Description:</b>
        {{ (details.description === undefined) ? 'Not provided' : details.description }}
      </div>
      <div v-if="isMicropipe()" class="text item">
        <b>Image name:</b>
        {{ details.image }}
      </div>
      <div v-if="isMicropipe()" class="text item">
        <b>Image id:</b>
        {{ details.imageId }}
      </div>
      <div v-if="isMicropipe() && details.inputMapping" class="text item">
        <b>Command:</b>
        {{ details.inputMapping.baseCmd}}
      </div>


      <el-divider></el-divider>

      <el-table v-if="serviceInput" :data="serviceInput" stripe style="width: 100%">
        <el-table-column label="Input">
          <el-table-column prop="name" label="Name"></el-table-column>
          <el-table-column v-if="isMicropipe()" prop="internalName" label="Command line"></el-table-column>
          <el-table-column prop="optional" label="Required">
            <template slot-scope="scope">{{scope.row.optional ? 'No' : 'Yes'}}</template>
          </el-table-column>
          <el-table-column prop="repeatable" label="Multiple values">
            <template slot-scope="scope">{{scope.row.repeatable ? 'Yes' : 'No'}}</template>
          </el-table-column>
        </el-table-column>
      </el-table>

      <el-divider></el-divider>

      <el-table v-if="serviceOutput" :data="serviceOutput" stripe style="width: 100%">
        <el-table-column label="Output">
          <el-table-column prop="name" label="Name"></el-table-column>
          <el-table-column v-if="isMicropipe()" prop="outputDir" label="Container output directory"></el-table-column>
          <el-table-column v-if="isMicropipe()" prop="fileNamePattern" label="File name pattern"></el-table-column>
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
      details: {},
      serviceInput : [],
      serviceOutput: []
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
          if (this.details.type == "micropipe") {
            console.log()
            this.serviceInput = this.details.inputMapping.parameters;
            this.serviceOutput = this.details.outputMapping;
          } else {
            this.serviceInput = this.details.input;
            this.serviceOutput = this.details.output;
          }
        });
    },
    goToTaskCreation: function(id) {
        this.$router.push({ path: `/services/${id}/instances` })
    },
    isMicropipe: function() {
      return this.details.type == 'micropipe'
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
