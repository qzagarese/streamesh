<template v-on:show="updateList">
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>Service: {{ details.serviceName }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" 
        @click="goBack()">Back</el-button>
      </div>
      
      <div class="text item">
        <b>Status:</b>
        {{ details.status }}
      </div>
      <div class="text item">
        <b>Started:</b>
        {{ details.started }}
      </div>
      <div class="text item">
        <b>Completed:</b>
        {{ details.exited }}
      </div>  

      <el-divider></el-divider>

      <el-table v-if="serviceDetails.outputMapping" :data="serviceDetails.outputMapping" stripe style="width: 100%">
        <el-table-column label="Output">
          <el-table-column prop="name" label="Name"></el-table-column>
          <el-table-column prop="outputDir" label="Container output directory"></el-table-column>
          <el-table-column prop="fileNamePattern" label="File name pattern"></el-table-column>
          <el-table-column label="">
              <template slot-scope="scope">
                <el-button @click="download(scope.row.name)">Download</el-button>
              </template>
          </el-table-column>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>


<script>
export default {
  name: "TaskDetails",
  data: () => {
    return {
      details: {},
      serviceDetails: {}
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
      fetch("http://localhost:8081/api/v1/tasks/" + this.id)
        .then(response => {
          return response.json();
        })
        .then(json => {
          this.details = json;
          return fetch("http://localhost:8081/api/v1/definitions/" + json.serviceId)
        }).then(response => {
          return response.json()   
        }).then(json => {
            this.serviceDetails = json
        })
    },
    goBack: function() {
        this.$router.back()
    },
    download: function(output) {
        window.open('http://localhost:8081/api/v1/tasks/' + this.id + '/' + output)
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
