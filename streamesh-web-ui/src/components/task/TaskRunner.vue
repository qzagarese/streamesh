<template v-on:show="updateList">
  <div>
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ details.name }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack()">Back</el-button>
      </div>

      <div class="text item">
        <b>Command:</b>
        {{ fullCommand }}
      </div>

      <el-divider></el-divider>

      <el-table
        v-if="details.inputMapping"
        :data="details.inputMapping.parameters"
        stripe
        style="width: 100%"
      >
        <el-table-column label="Input">
          <el-table-column prop="name" label="Name" width="150px"></el-table-column>
          <el-table-column prop="internalName" label="Value">
            <template slot-scope="scope">
              <div>
              <el-input 
                :placeholder="scope.row.internalName"
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
        @click="runTask()"
      >Run</el-button>
    </el-card>
  </div>
</template>


<script>
export default {
  name: "TaskRunner",
  data: () => {
    return {
      details: {},
      command: String,
      options: "",
      optionsList: [],
      runnable: Boolean
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
          if (json.inputMapping.parameters) {
            json.inputMapping.parameters.forEach(element => {
              element.inputValue = "";
              element.removable = false;
            });
          }
          this.details = json;
          this.command = json.inputMapping.baseCmd;
          this.computeRunnable();
        });
    },
    goBack: function() {
      this.$router.back();
    },
    runTask: function() {
      let body = {};
      this.details.inputMapping.parameters.forEach(element => {
        if (element.repeatable) {
          if (!body[element.name]) {
            body[element.name] = [];
          }
          body[element.name].push(element.inputValue);
        } else {
          body[element.name] = element.inputValue;
        }
      });

      fetch("http://localhost:8081/api/v1/definitions/" + this.id + "/tasks", {
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
          this.$message("Task scheduled. Id: " + json.taskId);
          this.updateDetails();
        })
        .catch(() => {
          this.$message("Oops, something went wrong :-(");
        });
    },
    addParameterRow: function(scope, row) {
      this.details.inputMapping.parameters.push({
        name: row.name,
        internalName: row.internalName,
        optional: row.optional,
        repeatable: row.repeatable,
        removable: true
      });
    },
    removeParameterRow: function(index) {
      this.details.inputMapping.parameters.splice(index, 1);
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
      let parameters = this.details.inputMapping.parameters;
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
