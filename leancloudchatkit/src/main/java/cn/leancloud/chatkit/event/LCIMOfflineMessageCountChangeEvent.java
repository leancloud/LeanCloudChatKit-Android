package cn.leancloud.chatkit.event;

import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMMessage;

/**
 * Created by wli on 16/3/7.
 * 离线消息数量发生变化的事件
 */
public class LCIMOfflineMessageCountChangeEvent {
  public LCIMConversation conversation;
  public LCIMMessage lastMessage;
  private LCIMOfflineMessageCountChangeEvent() {
    ;
  }
  public LCIMOfflineMessageCountChangeEvent(LCIMConversation conversation, LCIMMessage lastMessage) {
    this.conversation = conversation;
    this.lastMessage = lastMessage;
  }
}
