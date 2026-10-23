import { createStore } from "vuex";
import user from "./user";

export default createStore({
  state: {
    loginUser: null as any,
  },
  getters: {},
  mutations: {},
  actions: {},
  modules: {
    user,
  },
});
