package cn.leanclud.imkit;

import java.util.List;

/**
 * Created by wli on 16/2/2.
 */
public interface LCIMProfileProvider {
  public void getProfiles(List<String> list, LCIMProfilesCallBack callBack);
}
