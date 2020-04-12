<template v-on:show="updateList">
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ details.name }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack()">Back</el-button>
      </div>

      <div v-if="isMicropipe()" class="text item">
        <b>Command:</b>
        {{ fullCommand }}
      </div>

      <el-divider></el-divider>

      <el-table
        v-if="serviceInput"
        :data="serviceInput"
        stripe
        style="width: 100%"
      >
        <el-table-column label="Input">
          <el-table-column prop="name" label="Name" width="150px"></el-table-column>
          <el-table-column v-if="isMicropipe()" prop="internalName" label="Value">
            <template slot-scope="scope">
              <div>
              <el-input v-if="isMicropipe()"
                :placeholder="scope.row.internalName"
                v-model="scope.row.inputValue"
                v-on:input="updateOptionsList(scope, scope.row)"
              >
              </el-input>                           
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMicropipe()" prop="name" label="Value">
            <template slot-scope="scope">
              <div>
              <el-input v-if="!isMicropipe()"
                :placeholder="scope.row.name"
                v-model="scope.row.inputValue"
                v-on:input="updateOptionsList(scope, scope.row)"
              >
              </el-input>
                           
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="optional" label="Required" width="80px">
            <template slot-scope="scope">{{scope.row.optional ? 'No' : 'Yes'}}</template>
          </el-table-column>
          <el-table-column prop="repeatable" label="Multiple values" width="120px">
            <template slot-scope="scope">
              <el-button
                v-if="!scope.row.removable"
                :disabled="!scope.row.repeatable || scope.row.removable"
                @click="addParameterRow(scope, scope.row)"
              >
                <el-icon class="el-icon-plus"></el-icon>
              </el-button>
              <el-button v-if="scope.row.removable" @click="removeParameterRow(scope.$index)">
                <el-icon class="el-icon-minus"></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>

      <el-divider></el-divider>

      <el-button
        :disabled="!this.runnable"
        style="float: right; margin-bottom: 20px"
        @click="runService()"
      >Run</el-button>
    </el-card>
  </div>
</template>


<script>
export default {
  name: "ServiceRunner",
  data: () => {
    return {
      details: {},
      command: String,
      options: "",
      optionsList: [],
      runnable: Boolean,
      serviceInput: [],
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
          if (json.type == "micropipe") {
            this.serviceInput = json.inputMapping.parameters;
            this.serviceOutput = json.outputMapping;
            this.command = json.inputMapping.baseCmd;
          } else {
            this.serviceInput = json.input;
            this.serviceOutput = json.output;
          }  
          
          if (this.serviceInput) {
              this.serviceInput.forEach(element => {
                element.inputValue = "";
                element.removable = false;
              });
            }
            
          this.computeRunnable();
          this.details = json;
        });
    },
    goBack: function() {
      this.$router.back();
    },
    runService: function() {
      let body = {};
      this.serviceInput.forEach(element => {
        if (element.repeatable) {
          if (!body[element.name]) {
            body[element.name] = [];
          }
          body[element.name].push(element.inputValue);
        } else {
          body[element.name] = element.inputValue;
        }
      });

      let pathSegment = this.isMicropipe() ? "/tasks" : "/instances";

      fetch("http://localhost:8081/api/v1/definitions/" + this.id + pathSegment, {
        headers: {
          "Content-Type": "application/json"
        },
        method: "POST",
        body: JSON.stringify(body)
      })
        .then(response => {
          if (response.ok) {
            return response.json();
          } else {
            throw response;
          }
        })
        .then(json => {
          let executableType = this.isMicropipe() ? "Task" : "Flow instance";
          let executableId = this.isMicropipe() ? json.taskId : json.flowInstanceId;
          this.$message(executableType + " scheduled. Id: " + executableId);
          this.updateDetails();
        })
        .catch(() => {
          this.$message("Oops, something went wrong :-(");
        });
    },
    addParameterRow: function(scope, row) {
      this.serviceInput.push({
        name: row.name,
        internalName: row.internalName,
        optional: row.optional,
        repeatable: row.repeatable,
        removable: true
      });
    },
    removeParameterRow: function(index) {
      this.serviceInput.splice(index, 1);
      this.removeFromOptionList(index);
      this.updateOptions();
    },
    removeFromOptionList: function(i) {
      this.optionsList.forEach(function(item, index, object) {
        if (item.index == i) {
          object.splice(index, 1);
        }
      });
    },
    updateOptionsList: function(scope, row) {
      this.removeFromOptionList(scope.$index);
      if (row.inputValue !== "") {
        this.optionsList.push({
          internalName: row.internalName,
          inputValue: row.inputValue,
          index: scope.$index
        });
      }
      this.updateOptions();
    },
    updateOptions: function() {
      var newOptions = "";
      this.optionsList.forEach(item => {
        newOptions += item.internalName + " " + item.inputValue + " ";
      });
      this.options = newOptions;
    },
    computeRunnable: function() {
      let parameters = this.serviceInput;
      for (let index = 0; index < parameters.length; index++) {
        const element = parameters[index];
        if (
          !element.optional &&
          !element.removable &&
          (!element.inputValue || element.inputValue === "")
        ) {
          this.runnable = false;
          return;
        }
      }
      this.runnable = true;
    },
    isMicropipe: function() {
      return this.details.type == 'micropipe'
    }
  },
  computed: {
    fullCommand: function() {
      return this.command + " " + this.options;
    }
  },
  watch: {
    "optionsList.length": function() {
      this.computeRunnable();
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
