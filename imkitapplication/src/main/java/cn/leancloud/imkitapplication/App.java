package cn.leancloud.imkitapplication;

import android.app.Application;

import cn.leancloud.imkit.LCIMKit;

/**
 * Created by wli on 16/2/24.
 */
public class App extends Application {

  // 此 id 与 key 仅供测试使用
  private final String APP_ID = "683jigxkqb10jrirelvd9vcn9ywbq2o436lfz1kngsvigm27";
  private final String APP_KEY = "ualzl8f8pxmryous77m3gf2z0dyhrhk6xdb7zkiu6flc0jxy";

  @Override
  public void onCreate() {
    super.onCreate();
    LCIMKit.getInstance().setProfileProvider(new CustomUserProvider());
    LCIMKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
  }
}
