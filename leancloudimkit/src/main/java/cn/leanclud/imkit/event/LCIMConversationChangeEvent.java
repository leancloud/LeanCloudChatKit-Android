package cn.leanclud.imkit.event;

import com.avos.avoscloud.im.v2.AVIMConversation;

/**
 * Created by lzw on 15/3/5.
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
