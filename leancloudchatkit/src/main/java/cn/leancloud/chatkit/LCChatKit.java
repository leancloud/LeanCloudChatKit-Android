package cn.leancloud.chatkit;

import android.content.Context;
import android.text.TextUtils;

import cn.leancloud.callback.LCCallback;
import cn.leancloud.LCException;
import cn.leancloud.LeanCloud;
import cn.leancloud.im.LCIMOptions;
import cn.leancloud.im.SignatureFactory;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.LCIMMessageManager;
import cn.leancloud.im.v2.LCIMTypedMessage;
import cn.leancloud.im.v2.callback.LCIMClientCallback;

import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.handler.ChatKitClientEventHandler;
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

    LeanCloud.initialize(context.getApplicationContext(), appId, appKey, serverUrl);

    // 消息处理 handler
    LCIMMessageManager.registerMessageHandler(LCIMTypedMessage.class, new LCIMMessageHandler(context));

    // 与网络相关的 handler
    LCIMClient.setClientEventHandler(ChatKitClientEventHandler.getInstance());
    LCIMOptions.getGlobalOptions().setResetConnectionWhileBroken(true);
    LCIMOptions.getGlobalOptions().setUnreadNotificationEnabled(true);

    // 和 Conversation 相关的事件的 handler
    LCIMMessageManager.setConversationEventHandler(LCIMConversationHandler.getInstance());

    // 默认设置为离线消息仅推送数量
    LCIMOptions.getGlobalOptions().setUnreadNotificationEnabled(true);
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
    LCIMOptions.getGlobalOptions().setSignatureFactory(signatureFactory);
  }

  /**
   * 开启实时聊天
   *
   * @param userId
   * @param callback
   */
  public void open(final String userId, final LCIMClientCallback callback) {
    open(userId, null, callback);
  }

  /**
   * 开启实时聊天
   * @param userId 实时聊天的 clientId
   * @param tag 单点登录标示
   * @param callback
   */
  public void open(final String userId, String tag, final LCIMClientCallback callback) {
    if (TextUtils.isEmpty(userId)) {
      throw new IllegalArgumentException("userId can not be empty!");
    }
    if (null == callback) {
      throw new IllegalArgumentException("callback can not be null!");
    }

    LCIMClientCallback openCallback = new LCIMClientCallback() {
      @Override
      public void done(final LCIMClient LCIMClient, LCIMException e) {
        if (null == e) {
          currentUserId = userId;
          LCIMProfileCache.getInstance().initDB(LeanCloud.getContext(), userId);
          LCIMConversationItemCache.getInstance().initDB(LeanCloud.getContext(), userId, new LCCallback() {
            @Override
            protected void internalDone0(Object o, LCException e) {
              callback.internalDone(LCIMClient, e);
            }
          });
        } else {
          callback.internalDone(LCIMClient, e);
        }
      }
    };

    if (StringUtil.isEmpty(tag)) {
      LCIMClient.getInstance(userId).open(openCallback);
    } else {
      LCIMClient.getInstance(userId, tag).open(openCallback);
    }
  }

  /**
   * 关闭实时聊天
   *
   * @param callback
   */
  public void close(final LCIMClientCallback callback) {
    LCIMClient.getInstance(currentUserId).close(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient LCIMClient, LCIMException e) {
        currentUserId = null;
        LCIMConversationItemCache.getInstance().cleanup();
        if (null != callback) {
          callback.internalDone(LCIMClient, e);
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
   * 获取当前的 LCIMClient 实例
   *
   * @return
   */
  public LCIMClient getClient() {
    if (!TextUtils.isEmpty(currentUserId)) {
      return LCIMClient.getInstance(currentUserId);
    }
    return null;
  }
}
