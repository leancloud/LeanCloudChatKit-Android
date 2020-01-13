package cn.leancloud.chatkit;

import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.annotation.AVIMMessageField;
import cn.leancloud.im.v2.annotation.AVIMMessageType;

@AVIMMessageType(type = 1)
public class LCFacetimeInvitation extends AVIMTypedMessage {
  public static final int Status_Open = 0;
  public static final int Status_Closed = 1;
  public LCFacetimeInvitation() {
    super();
  }

  @AVIMMessageField(name = "status")
  int status;

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
