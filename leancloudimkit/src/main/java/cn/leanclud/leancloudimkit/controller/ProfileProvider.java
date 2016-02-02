package cn.leanclud.leancloudimkit.controller;

import java.util.List;

/**
 * Created by wli on 16/2/2.
 */
public interface ProfileProvider {
  public void getProfiles(List<String> list, ProfilesCallBack callBack);
}
