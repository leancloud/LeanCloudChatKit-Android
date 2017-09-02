package cn.leancloud.chatkitapplication;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMOptions;

import cn.leancloud.chatkit.LCChatKit;

/**
 * Created by wli on 16/2/24.
 */
public class App extends Application {

  // 此 id 与 key 仅供测试使用
  private final String APP_ID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
  private final String APP_KEY = "ye24iIK6ys8IvaISMC4Bs5WK";

  // 请替换成小班会产品的 appid 和 appkey
  private final String APP_ID_XIAOBANHUI = "";
  private final String APP_KEY_XIAOBANHUI = "";

  @Override
  public void onCreate() {
    super.onCreate();
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);
    AVOSCloud.setLogLevel(AVOSCloud.LOG_LEVEL_VERBOSE);
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.API, "https://intviu-api.leancloud.cn");
    AVIMOptions.getGlobalOptions().setRTMServer("wss://intviu-rtm.leancloud.cn");
    LCChatKit.getInstance().init(getApplicationContext(), APP_ID_XIAOBANHUI, APP_KEY_XIAOBANHUI);
    AVIMClient.setAutoOpen(false);
  }
}
