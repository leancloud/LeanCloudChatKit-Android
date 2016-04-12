package cn.leancloud.imkitapplication;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.imkit.LCIMKitUser;
import cn.leancloud.imkit.LCIMProfileProvider;
import cn.leancloud.imkit.LCIMProfilesCallBack;

/**
 * Created by wli on 15/12/4.
 * 实现自定义用户体系
 */
public class CustomUserProvider implements LCIMProfileProvider {

  private static CustomUserProvider customUserProvider;

  public synchronized static CustomUserProvider getInstance() {
    if (null == customUserProvider) {
      customUserProvider = new CustomUserProvider();
    }
    return customUserProvider;
  }

  private CustomUserProvider() {
  }

  private static List<LCIMKitUser> partUsers = new ArrayList<LCIMKitUser>();

  // 此数据均为 fake，仅供参考
  static {
    partUsers.add(new LCIMKitUser("Tom", "Tom", "http://www.avatarsdb.com/avatars/tom_and_jerry2.jpg"));
    partUsers.add(new LCIMKitUser("Jerry", "Jerry", "http://www.avatarsdb.com/avatars/jerry.jpg"));
    partUsers.add(new LCIMKitUser("Harry", "Harry", "http://www.avatarsdb.com/avatars/young_harry.jpg"));
    partUsers.add(new LCIMKitUser("William", "William", "http://www.avatarsdb.com/avatars/william_shakespeare.jpg"));
    partUsers.add(new LCIMKitUser("Bob", "Bob", "http://www.avatarsdb.com/avatars/bath_bob.jpg"));
  }

  @Override
  public void fetchProfiles(List<String> list, LCIMProfilesCallBack callBack) {
    List<LCIMKitUser> userList = new ArrayList<LCIMKitUser>();
    for (String userId : list) {
      for (LCIMKitUser user : partUsers) {
        if (user.getUserId().equals(userId)) {
          userList.add(user);
          break;
        }
      }
    }
    callBack.done(userList, null);
  }

  public List<LCIMKitUser> getAllUsers() {
    return partUsers;
  }
}
