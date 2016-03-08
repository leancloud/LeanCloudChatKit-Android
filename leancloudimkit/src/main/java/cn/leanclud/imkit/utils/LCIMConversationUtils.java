package cn.leanclud.imkit.utils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import cn.leanclud.imkit.LCIMKit;
import cn.leanclud.imkit.cache.ProfileCache;

/**
 * Created by wli on 16/3/2.
 * 和 Conversation 相关的 Util 类
 */
public class LCIMConversationUtils {

  /**
   * 获取会话名称
   * 优先级：
   * 1、AVIMConersation name 属性
   * 2、单聊：对方用户名
   *    群聊：成员用户名合并
   * @param conversation
   * @param callback
   */
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

  /**
   * 获取回话的 icon
   * 单聊：对方用户的头像
   * 群聊：成员头像合并
   * TODO 群聊头像合并
   * @param conversation
   * @param callback
   */
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