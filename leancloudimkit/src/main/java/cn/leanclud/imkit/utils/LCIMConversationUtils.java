package cn.leanclud.imkit.utils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;

import cn.leanclud.imkit.LCIMKit;
import cn.leanclud.imkit.cache.ProfileCache;

/**
 * Created by wli on 16/3/2.
 */
public class LCIMConversationUtils {
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
        ProfileCache.getInstance().getUserName(peerId, callback);
      }
    } else {
      callback.internalDone(null, new AVException(new Throwable("conversation is null!")));
    }
  }

  public static void getConversationIcon(AVIMConversation conversation, AVCallback<String> callback) {
    if (null != conversation) {
      if (conversation.isTransient() && conversation.getMembers().size() > 2) {
        ProfileCache.getInstance().getUserAvatar(conversation.getCreator(), callback);
      } else {
        String peerId = getConversationPeerId(conversation);
        ProfileCache.getInstance().getUserAvatar(peerId, callback);
      }
    } else {
      callback.internalDone(null, new AVException(new Throwable("conversation is null!")));
    }
  }
}
