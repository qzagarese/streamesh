import Vue from 'vue'
import VueRouter from 'vue-router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App.vue'
import './plugins/element.js'
import ServicesTable from './components/service/ServicesTable.vue'
import ServiceDetails from './components/service/ServiceDetails.vue'
import TasksTable from './components/task/TasksTable.vue'
import TasksDetails from './components/task/TaskDetails.vue'
import ServiceRunner from './components/service/ServiceRunner.vue'
import FlowInstancesTable from './components/flow/FlowInstancesTable.vue'
import FlowInstanceDetails from './components/flow/FlowInstanceDetails.vue'

Vue.config.productionTip = false
Vue.use(VueRouter)
Vue.use(ElementUI)

const routes = [
  { path: '/services', component: ServicesTable },
  { path: '/tasks', component: TasksTable},
  { path: '/flow-instances/:flowInstanceId/tasks', component: TasksTable, props: true},
  { path: '/flow-instances/:flowInstanceId/details', component: FlowInstanceDetails, props: true},
  { path: '/services/:id', component: ServiceDetails, props: true },
  { path: '/services/:id/instances', component: ServiceRunner, props: true},
  { path: '/flows', component: FlowInstancesTable},
  { path: '/tasks/:id', component: TasksDetails, props: true }
]

const router = new VueRouter({
  routes
})


new Vue({
  router,
  render: h => h(App),
  el: '#app',
  data: function () {
    return { visible: false }
  }
}).$mount('#app')
