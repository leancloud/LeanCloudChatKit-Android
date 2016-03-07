package cn.leanclud.imkit.cache;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wli on 16/2/26.
 * 缓存未读消息数量
 *
 * 流程
 * 1、初始化时从 db 里同步数据到缓存
 * 2、插入数据时先更新缓存，在更新 db
 * 3、获取的话只从缓存里读取数据
 */
public class UnreadCountCache {

  private static final String CONVERSATION_UNREADCOUNT = "conversation_unreadcount";
  private Map<String, Integer> unreadCountMap;
  private LocalStorage unreadCountDBHelper;

  private UnreadCountCache() {
    unreadCountMap = new HashMap<>();
  }

  private static UnreadCountCache unreadCountCache;

  public static synchronized UnreadCountCache getInstance() {
    if (null == unreadCountCache) {
      unreadCountCache = new UnreadCountCache();
    }
    return unreadCountCache;
  }

  /**
   * 因为只有在第一次的时候需要设置 Context 以及 clientId，所以单独拎出一个函数主动调用初始化
   * 避免 getInstance 传入过多参数
   * @param context
   * @param clientId
   */
  public synchronized void initDB(Context context, String clientId) {
    unreadCountDBHelper = new LocalStorage(context, clientId, "unreadCount");
    syncData();
  }

  /**
   * 此处的消息未读数量仅仅指的是本机的未读消息数量，并没有存储到 server 端
   * 在收到消息时消息未读数量 + 1
   * @param convid
   */
  public void increaseUnreadCount(String convid) {
    increaseUnreadCount(convid, 1);
  }

  /**
   * 在原来的基础增加未读的消息数量
   * @param convId
   * @param increment
   */
  public synchronized void increaseUnreadCount(String convId, int increment) {
    int unreadCount = getUnreadCountFromMap(convId);
    unreadCount += increment;
    setUnreadCountToMap(convId, unreadCount);
    setUnreadCountToDb(convId, unreadCount);
  }

  /**
   * 清空未读的消息数量
   * @param conviId
   */
  public synchronized void clearUnread(String conviId) {
    setUnreadCountToMap(conviId, 0);
    setUnreadCountToDb(conviId, 0);
  }

  /**
   * 删除该 Conversation 未读数量的缓存
   * @param convid
   */
  public synchronized void deleteConversation(String convid) {
    unreadCountMap.remove(convid);
    unreadCountDBHelper.deleteDatas(Arrays.asList(convid));
  }

  /**
   * 缓存该 Conversastoin，默认未读数量为 0
   * @param convId
   */
  public synchronized void insertConversation(String convId) {
    setUnreadCountToMap(convId, 0);
    setUnreadCountToDb(convId, 0);
  }

  /**
   * 获取该 Conversation 的未读数量
   * @param convId
   * @return
   */
  public synchronized int getUnreadCount(String convId) {
    return getUnreadCountFromMap(convId);
  }

  public synchronized List<String> getConversationList() {
    return new ArrayList<String>(unreadCountMap.keySet());
  }

  /**
   * 同步 db 数据到内存中
   */
  private void syncData() {
    unreadCountDBHelper.getIds(new AVCallback<List<String>>() {
      @Override
      protected void internalDone0(final List<String> idList, AVException e) {
        unreadCountDBHelper.getDatas(idList, new AVCallback<List<String>>() {
          @Override
          protected void internalDone0(final List<String> dataList, AVException e) {
            for (int i = 0; i < idList.size(); i++) {
              unreadCountMap.put(idList.get(i), getUnreadCountFromJson(dataList.get(i)));
            }
          }
        });
      }
    });
  }

  private Integer getUnreadCountFromJson(String str) {
    JSONObject jsonObject = JSONObject.parseObject(str);
    return jsonObject.getInteger(CONVERSATION_UNREADCOUNT);
  }

  private String getStringUnreadCount(Integer integer) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(CONVERSATION_UNREADCOUNT, integer);
    return jsonObject.toString();
  }

  private Integer getUnreadCountFromMap(String convId) {
    if (unreadCountMap.containsKey(convId)) {
      return unreadCountMap.get(convId);
    }
    return 0;
  }

  /**
   * 存储未读消息数量到内存
   * @param convId
   * @param unread
   */
  private void setUnreadCountToMap(String convId, Integer unread) {
    unreadCountMap.put(convId, unread);
  }

  /**
   * 存储未读消息数量到 db
   * @param conversationId
   * @param unread
   */
  private synchronized void setUnreadCountToDb(String conversationId, int unread) {
    unreadCountDBHelper.insertData(conversationId, getStringUnreadCount(unread));
  }
}
