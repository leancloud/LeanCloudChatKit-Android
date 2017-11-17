package cn.leancloud.chatkitapplication;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.PushService;

import cn.leancloud.chatkit.LCChatKit;

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
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);
    LCChatKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
    AVIMClient.setAutoOpen(false);
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }
}
