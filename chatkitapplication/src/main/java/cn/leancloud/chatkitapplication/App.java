package cn.leancloud.chatkitapplication;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.avos.avoscloud.*;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.*;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.im.v2.AVIMOptions;

import cn.leancloud.chatkit.LCChatKit;

/**
 * Created by wli on 16/2/24.
 */
public class App extends Application {

  // 此 id 与 key 仅供测试使用
//  private final String APP_ID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
//  private final String APP_KEY = "ye24iIK6ys8IvaISMC4Bs5WK";
  private final String APP_ID = "NlQwJxKuRDsRoTvTChS9MjHs-gzGzoHsz";
  private final String APP_KEY = "c2qUx8tQHqFvh0nVVB8Xku8H";

  @Override
  public void onCreate() {
    super.onCreate();
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);
//    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.PUSH, "nlqwjxku.push.lncld.net");
    AVIMOptions.getGlobalOptions().setRTMServer("wss://rtm51.leancloud.cn");
    LCChatKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
    AVIMClient.setAutoOpen(false);
    PushService.setDefaultPushCallback(this, MainActivity.class);
    AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
      public void done(AVException e) {
        if (e == null) {
          // 保存成功
          String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
          System.out.println("---  " + installationId);
        } else {
          // 保存失败，输出错误信息
          System.out.println("failed to save installation.");
        }
      }
    });
  }
}
