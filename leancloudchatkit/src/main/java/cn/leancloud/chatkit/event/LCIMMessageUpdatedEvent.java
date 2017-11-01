package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMMessage;

/**
 * Created by wli on 2017/6/29.
 */

public class LCIMMessageUpdatedEvent {

  public AVIMMessage message;

  public LCIMMessageUpdatedEvent(AVIMMessage message) {
    this.message = message;
  }
}
