package cn.leancloud.chatkit.event;

import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMTypedMessage;

/**
 * Created by wli on 15/8/23.
 * 收到 AVIMTypedMessage 消息后的事件
 */
public class LCIMIMTypeMessageEvent {
  public LCIMTypedMessage message;
  public LCIMConversation conversation;
}
