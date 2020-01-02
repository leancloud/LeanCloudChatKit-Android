package cn.leancloud.chatkit;

import android.content.Context;
import android.text.TextUtils;

import cn.leancloud.callback.AVCallback;
import cn.leancloud.AVException;
import cn.leancloud.AVOSCloud;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.SignatureFactory;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessageManager;
import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.callback.AVIMClientCallback;

import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.handler.LCIMClientEventHandler;
import cn.leancloud.chatkit.handler.LCIMConversationHandler;
import cn.leancloud.chatkit.handler.LCIMMessageHandler;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/2/2.
 * LeanCloudChatKit 的管理类
 */
public final class LCChatKit {

  private static LCChatKit lcChatKit;
  private LCChatProfileProvider profileProvider;
  private String currentUserId;

  private LCChatKit() {
  }

  public static synchronized LCChatKit getInstance() {
    if (null == lcChatKit) {
      lcChatKit = new LCChatKit();
    }
    return lcChatKit;
  }

  /**
   * 初始化 LeanCloudChatKit，此函数要在 Application 的 onCreate 中调用
   *
   * @param context
   * @param appId
   * @param appKey
   */
  public void init(Context context, String appId, String appKey, String serverUrl) {
    if (TextUtils.isEmpty(appId)) {
      throw new IllegalArgumentException("appId can not be empty!");
    }
    if (TextUtils.isEmpty(appKey)) {
      throw new IllegalArgumentException("appKey can not be empty!");
    }

    AVOSCloud.initialize(context.getApplicationContext(), appId, appKey, serverUrl);

    // 消息处理 handler
    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, new LCIMMessageHandler(context));

    // 与网络相关的 handler
    AVIMClient.setClientEventHandler(LCIMClientEventHandler.getInstance());
    AVIMOptions.getGlobalOptions().setResetConnectionWhileBroken(true);
    AVIMOptions.getGlobalOptions().setUnreadNotificationEnabled(true);

    // 和 Conversation 相关的事件的 handler
    AVIMMessageManager.setConversationEventHandler(LCIMConversationHandler.getInstance());

    // 默认设置为离线消息仅推送数量
    AVIMOptions.getGlobalOptions().setUnreadNotificationEnabled(true);
  }

  /**
   * 设置用户体系
   *
   * @param profileProvider
   */
  public void setProfileProvider(LCChatProfileProvider profileProvider) {
    this.profileProvider = profileProvider;
  }

  /**
   * 获取当前的用户体系
   *
   * @return
   */
  public LCChatProfileProvider getProfileProvider() {
    return profileProvider;
  }

  /**
   * 设置签名工厂
   *
   * @param signatureFactory
   */
  public void setSignatureFactory(SignatureFactory signatureFactory) {
    AVIMOptions.getGlobalOptions().setSignatureFactory(signatureFactory);
  }

  /**
   * 开启实时聊天
   *
   * @param userId
   * @param callback
   */
  public void open(final String userId, final AVIMClientCallback callback) {
    open(userId, null, callback);
  }

  /**
   * 开启实时聊天
   * @param userId 实时聊天的 clientId
   * @param tag 单点登录标示
   * @param callback
   */
  public void open(final String userId, String tag, final AVIMClientCallback callback) {
    if (TextUtils.isEmpty(userId)) {
      throw new IllegalArgumentException("userId can not be empty!");
    }
    if (null == callback) {
      throw new IllegalArgumentException("callback can not be null!");
    }

    AVIMClientCallback openCallback = new AVIMClientCallback() {
      @Override
      public void done(final AVIMClient avimClient, AVIMException e) {
        if (null == e) {
          currentUserId = userId;
          LCIMProfileCache.getInstance().initDB(AVOSCloud.getContext(), userId);
          LCIMConversationItemCache.getInstance().initDB(AVOSCloud.getContext(), userId, new AVCallback() {
            @Override
            protected void internalDone0(Object o, AVException e) {
              callback.internalDone(avimClient, e);
            }
          });
        } else {
          callback.internalDone(avimClient, e);
        }
      }
    };

    if (StringUtil.isEmpty(tag)) {
      AVIMClient.getInstance(userId).open(openCallback);
    } else {
      AVIMClient.getInstance(userId, tag).open(openCallback);
    }
  }

  /**
   * 关闭实时聊天
   *
   * @param callback
   */
  public void close(final AVIMClientCallback callback) {
    AVIMClient.getInstance(currentUserId).close(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        currentUserId = null;
        LCIMConversationItemCache.getInstance().cleanup();
        if (null != callback) {
          callback.internalDone(avimClient, e);
        }
      }
    });
  }

  /**
   * 获取当前的实时聊天的用户
   *
   * @return
   */
  public String getCurrentUserId() {
    return currentUserId;
  }

  /**
   * 获取当前的 AVIMClient 实例
   *
   * @return
   */
  public AVIMClient getClient() {
    if (!TextUtils.isEmpty(currentUserId)) {
      return AVIMClient.getInstance(currentUserId);
    }
    return null;
  }
}
