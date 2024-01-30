import { createApp } from 'vue';
import { createPinia } from 'pinia';


import App from './App.vue';
import router from './router';

import axios from 'axios';

axios.defaults.baseURL = 'https://localhost:8080';
axios.defaults.withCredentials = true;
axios.defaults.headers.post['Access-Control-Allow-Origin'] = '*';




// npm install bootstrap-vue-3
import BootstrapVue3 from 'bootstrap-vue-3';
import 'bootstrap/dist/css/bootstrap.css';

// npm i bootstrap-icons
import 'bootstrap-icons/font/bootstrap-icons.css';

// npm install @vuepic/vue-datepicker
import VueDatePicker from '@vuepic/vue-datepicker';
import '@vuepic/vue-datepicker/dist/main.css';

import './assets/main.css';
const app = createApp(App);

app.use(createPinia());
app.use(router);

//부트스트랩
app.use(BootstrapVue3);
//데이트피커
app.component('VueDatePicker', VueDatePicker);


app.mount('#app');

