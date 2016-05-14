package cn.leancloud.chatkit;

/**
 * Created by wli on 16/2/2.
 * LCChatKit 中的用户类，仅包含三个变量，暂不支持继承扩展
 */
public final class LCChatKitUser {
  private String userId;
  private String avatarUrl;
  private String name;

  public LCChatKitUser(String userId, String userName, String avatarUrl) {
    this.userId = userId;
    this.avatarUrl = avatarUrl;
    this.name = userName;
  }

  public String getUserId() {
    return userId;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getUserName() {
    return name;
  }
}
