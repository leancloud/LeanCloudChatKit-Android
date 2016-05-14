package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMConversation;

/**
 * Created by wli on 16/3/7.
 * 离线消息数量发生变化的事件
 */
public class LCIMOfflineMessageCountChangeEvent {
  public AVIMConversation conversation;
}
