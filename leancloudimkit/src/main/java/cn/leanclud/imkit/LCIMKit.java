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

import java.util.Arrays;
import java.util.List;

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

  public void setSignatureFactory(SignatureFactory signatureFactory) {
    AVIMClient.setSignatureFactory(signatureFactory);
  }

  public void open(final String clientId, final AVIMClientCallback callback) {
    AVIMClient.getInstance(clientId).open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if (null == e) {
          currentClientId = clientId;
//          ProfileCache.getInstance().initDB(AVOSCloud.applicationContext, clientId);
//          UnreadCountCache.getInstance().initDB(AVOSCloud.applicationContext, clientId);
        }
        callback.internalDone(avimClient, e);
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

  public void getUserProfile(String id, final AVCallback<LCIMUserProfile> callback) {
    if (null != profileProvider) {
      profileProvider.getProfiles(Arrays.asList(id), new LCIMProfilesCallBack() {
        @Override
        public void done(List<LCIMUserProfile> userList, Exception e) {
          if (null == userList || userList.isEmpty()) {
            callback.internalDone(null, new AVException(new Throwable("can not find current id!")));
          } else {
            callback.internalDone(userList.get(0), null);
          }
        }
      });
    }
  }

  public void getUserName(String id, final AVCallback<String> callback) {
    getUserProfile(id, new AVCallback<LCIMUserProfile>() {
      @Override
      protected void internalDone0(LCIMUserProfile userProfile, AVException e) {
        if (null != e) {
          callback.internalDone(null, e);
        } else {
          callback.internalDone(userProfile.getUserName(), null);
        }
      }
    });
  }

  public void getUserAvatar(String id, final AVCallback<String> callback) {
    getUserProfile(id, new AVCallback<LCIMUserProfile>() {
      @Override
      protected void internalDone0(LCIMUserProfile userProfile, AVException e) {
        if (null != e) {
          callback.internalDone(null, e);
        } else {
          callback.internalDone(userProfile.getAvatarUrl(), null);
        }
      }
    });
  }

  public String getCurrentUserId() {
    return currentClientId;
  }

  public AVIMClient getClient() {
    if (TextUtils.isEmpty(currentClientId)) {
      return AVIMClient.getInstance(currentClientId);
    }
    return null;
  }
}
