<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar">
            <img class="logo" src="../assets/oj-logo.svg" />
            <div class="title">蛋壳 OJ</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <div>
        {{ store.state.user.loginUser.userName }}
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
// 因为导出是用export，不是export default导出来的，只有export用{xxx},export default导出的不用加{}
import { routes } from "@/router/router";
import { useRoute, useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/ACCESS_ENUM";

const router = useRouter();
const doMenuClick = (key: string) => {
  router.push({ path: key });
};

// 默认当前选中的菜单项是主页
const selectedKeys = ref(["/"]);
// 根据用户选择的菜单项，在刷新页面后还是该菜单项
// const route = useRoute();
// const selectedKeys = ref([route.path]);
// 路由跳转后，更新选中的菜单项
// beforeEach 在路由更新跳转前去做判断是否需要跳转
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});

// 获取当前登录用户的信息
const store = useStore();
// console.log(store.state.user.loginUser);
// console.log(store.state.user?.loginUser?.userName ?? "未登录");
// const loginUser = store.state.user?.loginUser;
const loginUser = computed(() => store.state.user.loginUser);

// 展示在菜单的路由数组
// computed的作用：visibleRoutes 是 computed，它会在 loginUser.value 变化时自动重新计算
// computed会根据里面值的变化而动态的计算改变visibleRoutes的值，进而重新触发组件的渲染
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    // 根据是否隐藏标识过滤菜单
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据登录用户权限过滤菜单
    // console.log(loginUser);
    // 注意：这里使用 loginUser.value，因为 loginUser 是一个 ref响应式变量(会根据值改变而改变)
    if (!checkAccess(loginUser.value, item?.meta?.access as string)) {
      return false;
    }
    return true;
  });
});

// 测试：3s=3000ms后更新登录信息，查看登录信息是否发生变化
setTimeout(() => {
  // 用dispatch来调用actions
  // 参数1：指定调用actions的路径=模块名+具体的方法名
  // 参数2：要更新到变量的值payload
  store
    .dispatch("user/getLoginUser", {
      userName: "鱼皮",
      userRole: ACCESS_ENUM.ADMIN,
    })
    .then(() => {
      console.log(JSON.stringify(store.state.user));
    });
}, 3000);
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}
.title {
  color: #444;
  margin-left: 16px;
}
.logo {
  height: 48px;
}
</style>
