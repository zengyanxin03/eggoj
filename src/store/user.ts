import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/ACCESS_ENUM";
import { UserControllerService } from "../../generated";

export default {
  namespaced: true,
  // state存储状态信息
  state: () => ({
    loginUser: {
      userName: "未登录",
    },
  }),
  // getters获取状态信息
  getters: {},
  actions: {
    async getLoginUser({ commit, state }, payload) {
      const res = await UserControllerService.getLoginUserUsingGet();
      if (res.code === 0) {
        commit("updateUser", res.data);
      } else {
        commit("updateUser", {
          ...state.loginUser,
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
    },
  },
  //mutations修改状态变量
  mutations: {
    // 外层可以传递payload参数来改变state变量内部的状态
    updateUser(state, payload) {
      console.log("store/user.ts action received payload:", payload); // 看这里
      state.loginUser = payload;
    },
  },
} as StoreOptions<any>;
