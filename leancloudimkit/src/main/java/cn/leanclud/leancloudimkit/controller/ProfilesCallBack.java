package cn.leanclud.leancloudimkit.controller;

import java.util.List;

/**
 * Created by wli on 16/2/2.
 */
public interface ProfilesCallBack {
  public void done(List<UserProfile> userList, Exception e);
}
