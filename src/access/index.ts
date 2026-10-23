import ACCESS_ENUM from "@/access/ACCESS_ENUM";
import router from "@/router";
import store from "@/store";
import checkAccess from "@/access/checkAccess";

// const loginUser = store.state.user.loginUser;
const loginPath = "/user/login";

router.beforeEach(async (to, from) => {
  let loginUser = store.state.user.loginUser;
  console.log("access/index.ts这里会显示没登录:");
  console.log(loginUser);

  // 如果之前没登录过，自动登录
  if (!loginUser || !loginUser.userRole) {
    // 加 await 是为了等用户登录成功之后，再执行后续的代码
    await store.dispatch("user/getLoginUser");
    // 结果有两种：
    // 正常的拥有用户的信息
    // 用户处于未登录状态 {userName: '未登录', userRole: ACCESS_ENUM.NOT_LOGIN }
    loginUser = store.state.user.loginUser;
    console.log("access/index.ts这里应显示登录:");
    console.log(loginUser);
  }

  const isLogin =
    loginUser &&
    loginUser.userRole &&
    loginUser.userRole !== ACCESS_ENUM.NOT_LOGIN;

  if (!isLogin && to.path !== loginPath) {
    return {
      path: loginPath,
      query: {
        redirect: to.fullPath,
      },
    };
  }
  // 用户已经登录了,在跳转页面前,校验用户的权限
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  console.log(needAccess);
  console.log(loginUser);
  console.log(loginUser.userRole);
  // 这里有两种结果：
  // 要跳转的页面不需要权限，即没有access字段，处于未登录状态
  // 要跳转的页面需要权限，处于其他状态

  // 要跳转的页面不需要权限，用户是登录状态还是未登录状态都可以访问
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    console.log(needAccess);
    console.log(store.state.user.loginUser);
    return;
  }
  console.log(needAccess);
  // 要跳转的页面需要权限,用户必须处于登录状态，未登录状态不可以访问,直接跳转到登录页面
  // 如果连登录都还没有调用，连userRole该字段都没有，也是跳转到登录页面
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    if (
      loginUser.userRole === ACCESS_ENUM.NOT_LOGIN ||
      !loginUser.userRole ||
      !loginUser
    ) {
      return `/user/login?redirect=${to.fullPath}`;
    }
    // 用户已经登录了，判断用户权限是否符合
    // 不符合，跳转到无权限页面
    // 符合，放行
    if (!checkAccess(loginUser, needAccess)) {
      return "noAuth";
    }
  }
  return true;
});
