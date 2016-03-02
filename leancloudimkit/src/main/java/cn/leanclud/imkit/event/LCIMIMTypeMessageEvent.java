package cn.leanclud.imkit.event;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by wli on 15/8/23.
 */
public class LCIMIMTypeMessageEvent {
  public AVIMTypedMessage message;
  public AVIMConversation conversation;
}
