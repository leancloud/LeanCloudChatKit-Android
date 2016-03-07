package cn.leanclud.imkit;

/**
 * Created by wli on 16/2/2.
 */
public class LCIMUserProfile {
  private String userId;
  private String avatarUrl;
  private String userName;

  public LCIMUserProfile(String userId, String userName, String avatarUrl) {
    this.userId = userId;
    this.avatarUrl = avatarUrl;
    this.userName = userName;
  }

  public String getUserId() {
    return userId;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getUserName() {
    return userName;
  }
}
