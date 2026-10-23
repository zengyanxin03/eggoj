import ACCESS_ENUM from "@/access/ACCESS_ENUM";

/**
 * 校验权限（判断当前登录用户是否具有某个权限）
 * @param loginUser 当前登录的用户信息
 * @param needAccess 需要具有的权限
 * @return boolean 有无权限
 */
const checkAccess = (loginUser: any, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
  // 获取当前登录用户具有的权限
  // 如果没有loginUser.userRole字段，则表示未登录
  const loginUserAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN;
  console.log(loginUser.userRole);
  console.log(needAccess);
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true;
  }
  // 如果需要用户权限
  if (needAccess === ACCESS_ENUM.USER) {
    // 只要登录就可以了
    if (loginUserAccess === ACCESS_ENUM.NOT_LOGIN) {
      return false;
    }
  }
  // 如果需要管理员权限
  if (needAccess === ACCESS_ENUM.ADMIN) {
    // 如果不为管理员，表示无权限
    if (loginUserAccess !== ACCESS_ENUM.ADMIN) {
      return false;
    }
  }
  return true;
};
export default checkAccess;
