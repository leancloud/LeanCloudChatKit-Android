package cn.leancloud.chatkitapplication;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

import cn.leancloud.chatkit.LCIMKit;

/**
 * Created by wli on 16/2/24.
 */
public class App extends Application {

  // 此 id 与 key 仅供测试使用
  private final String APP_ID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
  private final String APP_KEY = "ye24iIK6ys8IvaISMC4Bs5WK";

  @Override
  public void onCreate() {
    super.onCreate();
    LCIMKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);
    LCIMKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
  }
}
