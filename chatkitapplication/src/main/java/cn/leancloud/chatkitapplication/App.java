package cn.leancloud.chatkitapplication;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import cn.leancloud.cache.PersistenceUtil;

import java.io.File;

import cn.leancloud.*;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.push.PushService;

import cn.leancloud.chatkit.LCChatKit;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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
    File installationFile = new File(this.getFilesDir(), "installation");
    if (installationFile.exists()) {
      String oldInstallString = "{ \"objectId\":\"4DxgwI9RKKgwVt04VvKKfBKoOGIEQXXB\",\"updatedAt\":null,\"createdAt\":\"2018-12-29T01:49:05.561Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"df0c633543d24e29fd58293c3d07dfda\"}}";
      PersistenceUtil.sharedInstance().saveContentToFile(oldInstallString, installationFile);
      String cachedInstallation = PersistenceUtil.sharedInstance().readContentFromFile(installationFile);
      System.out.println(">>>> " + cachedInstallation);
    } else {
      System.out.println(">>>> not found cached installation.");
    }

    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
//    AVOSCloud.useAVCloudUS();
    LCChatKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);

    PushService.setDefaultPushCallback(this, MainActivity.class);
    PushService.setAutoWakeUp(true);
    PushService.setDefaultChannelId(this, "default");

    AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(AVObject avObject) {
        String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
        System.out.println("---  " + installationId);
      }

      @Override
      public void onError(Throwable e) {
        // 保存失败，输出错误信息
        System.out.println("failed to save installation.");
      }

      @Override
      public void onComplete() {

      }
    });
  }
}
