package cn.leancloud.chatkit.handler;

import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMClientEventHandler;

import cn.leancloud.chatkit.event.LCIMConnectionChangeEvent;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/16.
 * 与网络相关的 handler
 * 注意，此 handler 并不是网络状态通知，而是当前 client 的连接状态
 */
public class ChatKitClientEventHandler extends LCIMClientEventHandler {

  private static ChatKitClientEventHandler eventHandler;

  public static synchronized ChatKitClientEventHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new ChatKitClientEventHandler();
    }
    return eventHandler;
  }

  private ChatKitClientEventHandler() {
  }


  private volatile boolean connect = false;

  /**
   * 是否连上聊天服务
   *
   * @return
   */
  public boolean isConnect() {
    return connect;
  }

  public void setConnectAndNotify(boolean isConnect) {
    connect = isConnect;
    EventBus.getDefault().post(new LCIMConnectionChangeEvent(connect));
  }

  @Override
  public void onConnectionPaused(LCIMClient LCIMClient) {
    setConnectAndNotify(false);
  }

  @Override
  public void onConnectionResume(LCIMClient LCIMClient) {
    setConnectAndNotify(true);
  }

  @Override
  public void onClientOffline(LCIMClient LCIMClient, int i) {
    LCIMLogUtils.d("client " + LCIMClient.getClientId() + " is offline!");
  }
}
