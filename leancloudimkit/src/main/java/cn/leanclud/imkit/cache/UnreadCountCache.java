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
 * 2、异步
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

  public synchronized void increaseUnreadCount(String convId, int increment) {
    int unreadCount = getUnreadCountFromMap(convId);
    unreadCount += increment;
    setUnreadCountToMap(convId, unreadCount);
    setUnreadCountToDb(convId, unreadCount);
  }

  public synchronized void clearUnread(String conviId) {
    setUnreadCountToMap(conviId, 0);
    setUnreadCountToDb(conviId, 0);
  }

  public synchronized void deleteConversation(String convid) {
    unreadCountMap.remove(convid);
    unreadCountDBHelper.deleteDatas(Arrays.asList(convid));
  }

  public synchronized void insertConversation(String convId) {
    setUnreadCountToMap(convId, 0);
    setUnreadCountToDb(convId, 0);
  }

  public synchronized int getUnreadCount(String convId) {
    return getUnreadCountFromMap(convId);
  }

  public synchronized List<String> getConversationList() {
    return new ArrayList<String>(unreadCountMap.keySet());
  }

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

  private void setUnreadCountToMap(String convId, Integer unread) {
    unreadCountMap.put(convId, unread);
  }

  private synchronized void setUnreadCountToDb(String conversationId, int unread) {
    unreadCountDBHelper.insertData(conversationId, getStringUnreadCount(unread));
  }
}
