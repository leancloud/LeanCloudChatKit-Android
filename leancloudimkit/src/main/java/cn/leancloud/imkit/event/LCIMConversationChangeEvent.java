package cn.leancloud.imkit.event;

import com.avos.avoscloud.im.v2.AVIMConversation;

/**
 * Created by lzw on 15/3/5.
 * 当 Conversation 成员出现变化时的事件
 */
public class LCIMConversationChangeEvent {
  private AVIMConversation conv;

  public LCIMConversationChangeEvent(AVIMConversation conv) {
    this.conv = conv;
  }

  public AVIMConversation getConv() {
    return conv;
  }
}
