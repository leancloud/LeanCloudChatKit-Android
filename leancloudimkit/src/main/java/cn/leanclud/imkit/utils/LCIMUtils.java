package cn.leanclud.imkit.utils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;

import cn.leanclud.imkit.LCIMKit;

/**
 * Created by wli on 16/3/2.
 */
public class LCIMUtils {
  //调用前保证 Conversation 数据完整
  private static String getConversationPeerId(AVIMConversation conversation) {
    if (null != conversation && 2 == conversation.getMembers().size()) {
      String currentUserId = LCIMKit.getInstance().getCurrentUserId();
      String firstMemeberId = conversation.getMembers().get(0);
      return conversation.getMembers().get(firstMemeberId.equals(currentUserId) ? 1 : 0);
    }
    return "";
  }

  //调用前保证 Conversation 数据完整
  public static void getConversationName(AVIMConversation conversation, AVCallback<String> callback) {
    if (null != conversation) {
      if (conversation.isTransient() && conversation.getMembers().size() > 2) {
        callback.internalDone(conversation.getName(), null);
      } else {
        String peerId = getConversationPeerId(conversation);
        LCIMKit.getInstance().getUserName(peerId, callback);
      }
    }
  }
}
