package cn.leancloud.chatkit.event;

import cn.leancloud.im.v2.LCIMConversation;

/**
 * Created by wli on 16/9/14.
 */
public class LCIMConversationItemLongClickEvent {
  public LCIMConversation conversation;

  public LCIMConversationItemLongClickEvent(LCIMConversation conversation) {
    this.conversation = conversation;
  }
}
