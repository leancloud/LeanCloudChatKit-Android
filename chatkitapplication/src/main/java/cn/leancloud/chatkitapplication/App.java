package cn.leancloud.chatkitapplication;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.avos.avoscloud.*;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.*;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.im.v2.AVIMOptions;
import com.avos.avoscloud.AVOSCloud.*;

import cn.leancloud.chatkit.LCChatKit;

/**
 * Created by wli on 16/2/24.
 */
public class App extends Application {

  // 此 id 与 key 仅供测试使用
//  private final String APP_ID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
//  private final String APP_KEY = "ye24iIK6ys8IvaISMC4Bs5WK";
  private final String APP_ID = "l8j5lm8c9f9d2l90213i00wsdhhljbrwrn6g0apptblu7l90";
  private final String APP_KEY = "b3uyj9cmk84s5t9n6z1rqs9pvf2azofgacy9bfigmiehhheg";


  @Override
  public void onCreate() {
    super.onCreate();
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);
    AVOSCloud.useAVCloudUS();
    LCChatKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
    AVIMClient.setAutoOpen(true);
    PushService.setDefaultPushCallback(this, MainActivity.class);
    PushService.setAutoWakeUp(true);
    PushService.setDefaultChannelId(this, "default");

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
