package cn.leanclud.imkit;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.SignatureFactory;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;

import cn.leanclud.imkit.cache.ConversationItemCache;
import cn.leanclud.imkit.cache.ProfileCache;
import cn.leanclud.imkit.handler.LCIMClientEventHandler;
import cn.leanclud.imkit.handler.LCIMConversationHandler;
import cn.leanclud.imkit.handler.LCIMMessageHandler;

/**
 * Created by wli on 16/2/2.
 * TODO: 稍后添加注释
 */
public final class LCIMKit {

  private static LCIMKit lcimKit;
  private LCIMProfileProvider profileProvider;
  private String currentClientId;

  private LCIMKit() {
  }

  public static synchronized LCIMKit getInstance() {
    if (null == lcimKit) {
      lcimKit = new LCIMKit();
    }
    return lcimKit;
  }

  public void init(Context context, String appId, String appKey) {
    if (TextUtils.isEmpty(appId)) {
      throw new IllegalArgumentException("appId can not be empty!");
    }
    if (TextUtils.isEmpty(appKey)) {
      throw new IllegalArgumentException("appKey can not be empty!");
    }

    AVOSCloud.setDebugLogEnabled(true);
    AVOSCloud.initialize(context.getApplicationContext(), appId, appKey);

    // 消息处理 handler
    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, new LCIMMessageHandler(context));

    // 与网络相关的 handler
    AVIMClient.setClientEventHandler(LCIMClientEventHandler.getInstance());

    // 和 Conversation 相关的事件的 handler
    AVIMMessageManager.setConversationEventHandler(LCIMConversationHandler.getInstance());

    // 默认设置为离线消息仅推送数量
    AVIMClient.setOfflineMessagePush(true);
  }

  public void setProfileProvider(LCIMProfileProvider profileProvider) {
    this.profileProvider = profileProvider;
  }

  public LCIMProfileProvider getProfileProvider() {
    return profileProvider;
  }

  public void setSignatureFactory(SignatureFactory signatureFactory) {
    AVIMClient.setSignatureFactory(signatureFactory);
  }

  public void open(final String clientId, final AVIMClientCallback callback) {
    AVIMClient.getInstance(clientId).open(new AVIMClientCallback() {
      @Override
      public void done(final AVIMClient avimClient, AVIMException e) {
        if (null == e) {
          currentClientId = clientId;
          ProfileCache.getInstance().initDB(AVOSCloud.applicationContext, clientId);
          ConversationItemCache.getInstance().initDB(AVOSCloud.applicationContext, clientId, new AVCallback() {
            @Override
            protected void internalDone0(Object o, AVException e) {
              callback.internalDone(avimClient, e);
            }
          });
        } else {
          callback.internalDone(avimClient, e);
        }
      }
    });
  }

  public void close(final AVIMClientCallback callback) {
    AVIMClient.getInstance(currentClientId).close(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        currentClientId = null;
        callback.internalDone(avimClient, e);
      }
    });
  }

  public String getCurrentUserId() {
    return currentClientId;
  }

  public AVIMClient getClient() {
    if (!TextUtils.isEmpty(currentClientId)) {
      return AVIMClient.getInstance(currentClientId);
    }
    return null;
  }
}
