package cn.leanclud.imkit.utils;

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
  public static final String AVATAR_CLICK_ACTION = getPrefixConstant("avatar_click_action");

  /**
   * LCIMConversationListFragment item 点击事件
   * 如果开发者不想跳转到 LCIMConversationActivity，可以在 Mainfest 里接管该事件
   */
  public static final String CONVERSATION_ITEM_CLICK_ACTION = getPrefixConstant("conversation_item_click_action");

  public static final String LCIM_LOG_TAG = getPrefixConstant("lcim_log_tag");


  // LCIMImageActivity
  public static final String IMAGE_LOCAL_PATH = getPrefixConstant("image_local_path");
  public static final String IMAGE_URL = getPrefixConstant("image_url");

  //Notification
  public static final String NOTOFICATION_TAG = getPrefixConstant("notification_tag");
  public static final String NOTIFICATION_SYSTEM = getPrefixConstant("notification_system_chat");
}
