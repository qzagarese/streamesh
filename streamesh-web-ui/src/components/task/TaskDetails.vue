<template v-on:show="updateList">
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>Service: {{ details.serviceName }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack()">Back</el-button>
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

      <el-table
        v-if="serviceDetails.outputMapping"
        :data="serviceDetails.outputMapping"
        stripe
        style="width: 100%"
        ref="detailsTable"
        max-height="400"
      >
        <el-table-column label="Output">
          <el-table-column type="expand" prop="stream" v-if="details.status == 'RUNNING'">
            <template slot-scope="scope">
              <p v-for="item in scope.row.stream" :key="item.index">{{ item.date.toLocaleString() + ' - ' + item.value }}</p>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="Name"></el-table-column>
          <el-table-column prop="outputDir" label="Container output directory"></el-table-column>
          <el-table-column prop="fileNamePattern" label="File name pattern"></el-table-column>
          <el-table-column label>
            <template slot-scope="scope">
              <el-button
                v-if="details.status == 'COMPLETE'"
                @click="download(scope.row.name)"
              >Download</el-button>
              <el-button
                v-if="details.status == 'RUNNING'"
                v-show="!status(scope.$index)"
                @click="startStreaming(scope.row, scope)"
              >Show Stream</el-button>
              <el-button
                v-show="status(scope.$index)"
                @click="stopStreaming(scope.row, scope.$index, scope.row.name)"
              >Hide Stream</el-button>
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
      serviceDetails: {},
      controllers: {},
      statuses: []
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
          return fetch(
            "http://localhost:8081/api/v1/definitions/" + json.serviceId
          );
        })
        .then(response => {
          return response.json();
        })
        .then(json => {
          json.outputMapping.forEach(element => {
            element.stream = [];
            element.streaming = false;
          });
          this.serviceDetails = json;
        });
    },
    startStreaming: function(row, scope) {
      this.$set(
        this.serviceDetails.outputMapping[scope.$index],
        "streaming",
        true
      );
      let abortCtrl = new AbortController()
      let signal = abortCtrl.signal

      this.$refs.detailsTable.toggleRowExpansion(row, true);
      fetch("http://localhost:8081/api/v1/tasks/" + this.id + "/" + row.name, { signal })
        .then(response => {
          console.log(response)
          const reader = response.body.getReader();
          let vueComponent = this;
          let stream = new ReadableStream({
            start(controller) {
              vueComponent.controllers[row.name] = controller;
              function pump() {
                if (!vueComponent.serviceDetails.outputMapping[scope.$index].streaming) {
                  stream.cancel()
                  abortCtrl.abort()
                  return
                }
                reader.read().then(({ done, value }) => {
                  if (done) {
                    controller.close();
                    return;
                  }
                  vueComponent.renderLogValue(row, value);
                  pump();
                });
              }
              pump();
            }
          });
          return stream
        })
        .catch(err => console.error(err));
    },
    stopStreaming: function(row, index, name) {
      this.$set(this.serviceDetails.outputMapping[index], "streaming", false);
      this.$refs.detailsTable.toggleRowExpansion(row, false);

      this.controllers[name].close();
    },
    renderLogValue: function(row, blob) {
      let stream = row.stream;
      let date = new Date()
      stream.push({
        date : date,
        index: date.getTime(),
        value: new TextDecoder("utf-8").decode(blob)
      });
      if (stream.length > 10) {
        stream.splice(0, 1);
      }
      this.serviceDetails.outputMapping[row.$index] = stream;
    },
    goBack: function() {
      this.$router.back();
    },
    download: function(output) {
      window.open(
        "http://localhost:8081/api/v1/tasks/" + this.id + "/" + output
      );
    },
    status: function(index) {
      return this.statuses[index];
    }
  },
  watch: {
    "serviceDetails.outputMapping": {
      handler: function(val) {
        let streamStatuses = [];
        val.forEach(element => {
          streamStatuses.push(element.streaming);
        });
        this.statuses = streamStatuses;
      },
      deep: true
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
