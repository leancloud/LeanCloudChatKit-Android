package cn.leanclud.imkit;

/**
 * Created by wli on 16/2/29.
 * 所有常量值均放到此类里边
 */
public class LCIMConstants {
  private static final String LEANMESSAGE_CONSTANTS_PREFIX = "cn.leanclud.imkit.";
  private static String getPrefixConstant(String str) {
    return LEANMESSAGE_CONSTANTS_PREFIX + str;
  }

  /**
   * 参数传递的 key 值，表示对方的 id，跳转到 LCIMConversationActivity 时可以设置
   */
  public static final String PEER_ID = getPrefixConstant("peer_id");

  /**
   * 参数传递的 key 值，表示回话 id，跳转到 LCIMConversationActivity 时可以设置
   */
  public static final String CONVERSATION_ID = getPrefixConstant("conversation_id");

  /**
   * LCIMConversationActivity 中头像点击事件发送的 action
   */
  public static final String AVATAR_CLICK_action = "avatar_click_action";
}
