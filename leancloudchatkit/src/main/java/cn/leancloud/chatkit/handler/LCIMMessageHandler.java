package cn.leancloud.chatkit.handler;

import android.content.Context;
import android.content.Intent;

import cn.leancloud.callback.LCCallback;
import cn.leancloud.LCException;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMTypedMessage;
import cn.leancloud.im.v2.LCIMTypedMessageHandler;
import cn.leancloud.im.v2.messages.LCIMTextMessage;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.event.LCIMIMTypeMessageEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.leancloud.chatkit.utils.LCIMNotificationUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by zhangxiaobo on 15/4/20.
 * LCIMTypedMessage 的 handler，socket 过来的 LCIMTypedMessage 都会通过此 handler 与应用交互
 * 需要应用主动调用 AVIMMessageManager.registerMessageHandler 来注册
 * 当然，自定义的消息也可以通过这种方式来处理
 */
public class LCIMMessageHandler extends LCIMTypedMessageHandler<LCIMTypedMessage> {

  private Context context;

  public LCIMMessageHandler(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  public void onMessage(LCIMTypedMessage message, LCIMConversation conversation, LCIMClient client) {
    if (message == null || message.getMessageId() == null) {
      LCIMLogUtils.d("may be SDK Bug, message or message id is null");
      return;
    }

    if (LCChatKit.getInstance().getCurrentUserId() == null) {
      LCIMLogUtils.d("selfId is null, please call LCChatKit.open!");
      client.close(null);
    } else {
      if (!client.getClientId().equals(LCChatKit.getInstance().getCurrentUserId())) {
        client.close(null);
      } else {
        if (LCIMNotificationUtils.isShowNotification(conversation.getConversationId())) {
          sendNotification(message, conversation);
        }
        LCIMConversationItemCache.getInstance().insertConversation(message.getConversationId());
        if (!message.getFrom().equals(client.getClientId())) {
          sendEvent(message, conversation);
        }
      }
    }
  }

  @Override
  public void onMessageReceipt(LCIMTypedMessage message, LCIMConversation conversation, LCIMClient client) {
    super.onMessageReceipt(message, conversation, client);
  }

  /**
   * 发送消息到来的通知事件
   *
   * @param message
   * @param conversation
   */
  private void sendEvent(LCIMTypedMessage message, LCIMConversation conversation) {
    LCIMIMTypeMessageEvent event = new LCIMIMTypeMessageEvent();
    event.message = message;
    event.conversation = conversation;
    EventBus.getDefault().post(event);
  }

  private void sendNotification(final LCIMTypedMessage message, final LCIMConversation conversation) {
    if (null != conversation && null != message) {
      final String notificationContent = message instanceof LCIMTextMessage ?
        ((LCIMTextMessage) message).getText() : context.getString(R.string.lcim_unspport_message_type);
      LCIMProfileCache.getInstance().getCachedUser(message.getFrom(), new LCCallback<LCChatKitUser>() {
        @Override
        protected void internalDone0(LCChatKitUser userProfile, LCException e) {
          if (e != null) {
            LCIMLogUtils.logException(e);
          } else if (null != userProfile) {
            String title = userProfile.getName();
            Intent intent = getIMNotificationIntent(conversation.getConversationId(), message.getFrom());
            LCIMNotificationUtils.showNotification(context, title, notificationContent, null, intent);
          }
        }
      });
    }
  }

  /**
   * 点击 notification 触发的 Intent
   * 注意要设置 package 已经 Category，避免多 app 同时引用 lib 造成消息干扰
   * @param conversationId
   * @param peerId
   * @return
   */
  private Intent getIMNotificationIntent(String conversationId, String peerId) {
    Intent intent = new Intent();
    intent.setAction(LCIMConstants.CHAT_NOTIFICATION_ACTION);
    intent.putExtra(LCIMConstants.CONVERSATION_ID, conversationId);
    intent.putExtra(LCIMConstants.PEER_ID, peerId);
    intent.setPackage(context.getPackageName());
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    return intent;
  }
}
