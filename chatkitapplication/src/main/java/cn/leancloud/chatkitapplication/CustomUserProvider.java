package cn.leancloud.chatkitapplication;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.LCChatProfileProvider;
import cn.leancloud.chatkit.LCChatProfilesCallBack;

/**
 * Created by wli on 15/12/4.
 * 实现自定义用户体系
 */
public class CustomUserProvider implements LCChatProfileProvider {

  private static CustomUserProvider customUserProvider;

  public synchronized static CustomUserProvider getInstance() {
    if (null == customUserProvider) {
      customUserProvider = new CustomUserProvider();
    }
    return customUserProvider;
  }

  private CustomUserProvider() {
  }

  private static List<LCChatKitUser> partUsers = new ArrayList<LCChatKitUser>();

  // 此数据均为 fake，仅供参考
  static {
    partUsers.add(new LCChatKitUser("Tom", "Tom", "http://www.avatarsdb.com/avatars/tom_and_jerry2.jpg"));
    partUsers.add(new LCChatKitUser("Jerry", "Jerry", "http://www.avatarsdb.com/avatars/jerry.jpg"));
    partUsers.add(new LCChatKitUser("Harry", "Harry", "http://www.avatarsdb.com/avatars/young_harry.jpg"));
    partUsers.add(new LCChatKitUser("William", "William", "http://www.avatarsdb.com/avatars/william_shakespeare.jpg"));
    partUsers.add(new LCChatKitUser("Bob", "Bob", "http://www.avatarsdb.com/avatars/bath_bob.jpg"));
  }

  @Override
  public void fetchProfiles(List<String> list, LCChatProfilesCallBack callBack) {
    List<LCChatKitUser> userList = new ArrayList<LCChatKitUser>();
    for (String userId : list) {
      for (LCChatKitUser user : partUsers) {
        if (user.getUserId().equals(userId)) {
          userList.add(user);
          break;
        }
      }
    }
    callBack.done(userList, null);
  }

  @Override
  public List<LCChatKitUser> getAllUsers() {
    return partUsers;
  }
}
