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

  public static void getConversationName(final AVIMConversation conversation, final AVCallback<String> callback) {
    if (null == callback) {
      return;
    }
    if (null == conversation) {
      callback.internalDone(null, new AVException(new Throwable("conversation can not be null!")));
      return;
    }
    if (conversation.isTransient() && conversation.getMembers().size() > 2) {
      callback.internalDone(conversation.getName(), null);
    } else {
      String peerId = getConversationPeerId(conversation);
      ProfileCache.getInstance().getUserName(peerId, callback);
    }
  }

  public static void getConversationIcon(final AVIMConversation conversation, AVCallback<String> callback) {
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

  private static String getConversationPeerId(AVIMConversation conversation) {
    if (null != conversation && 2 == conversation.getMembers().size()) {
      String currentUserId = LCIMKit.getInstance().getCurrentUserId();
      String firstMemeberId = conversation.getMembers().get(0);
      return conversation.getMembers().get(firstMemeberId.equals(currentUserId) ? 1 : 0);
    }
    return "";
  }
}