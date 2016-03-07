package cn.leanclud.imkit.handler;


import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;

import java.util.List;

import cn.leanclud.imkit.cache.UnreadCountCache;
import cn.leanclud.imkit.event.LCIMConversationChangeEvent;
import cn.leanclud.imkit.event.LCIMUnreadCountChangeEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/1.
 * 和 Conversation 相关的事件的 handler
 * 需要应用主动调用  AVIMMessageManager.setConversationEventHandler
 */
public class LCIMConversationHandler extends AVIMConversationEventHandler {

  private static LCIMConversationHandler eventHandler;

  public static synchronized LCIMConversationHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new LCIMConversationHandler();
    }
    return eventHandler;
  }

  private LCIMConversationHandler() {}

  @Override
  public void onOfflineMessagesUnread(AVIMClient client, AVIMConversation conversation, int unreadCount) {
    if (unreadCount > 0) {
      UnreadCountCache.getInstance().increaseUnreadCount(conversation.getConversationId(), unreadCount);
      EventBus.getDefault().post(new LCIMUnreadCountChangeEvent());
    }
  }

  @Override
  public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
    refreshCacheAndNotify(conversation);
  }

  @Override
  public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
    refreshCacheAndNotify(conversation);
  }

  @Override
  public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
    refreshCacheAndNotify(conversation);
  }

  @Override
  public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
//    refreshCacheAndNotify(convwersation);
  }

  private void refreshCacheAndNotify(AVIMConversation conversation) {
    LCIMConversationChangeEvent conversationChangeEvent = new LCIMConversationChangeEvent(conversation);
    EventBus.getDefault().post(conversationChangeEvent);
  }
}
