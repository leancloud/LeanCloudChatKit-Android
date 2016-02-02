package cn.leanclud.leancloudimkit.controller;

/**
 * Created by wli on 16/2/2.
 */
public class UserProfile {
  private String userId;
  private String avatarUrl;
  private String userName;

  public UserProfile(String userId, String avatarUrl, String userName) {
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
