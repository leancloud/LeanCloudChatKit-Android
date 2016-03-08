package cn.leanclud.imkit.cache;

import android.content.Context;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by wli on 16/2/26.
 * 缓存未读消息数量
 * <p/>
 * 流程
 * 1、初始化时从 db 里同步数据到缓存
 * 2、插入数据时先更新缓存，在更新 db
 * 3、获取的话只从缓存里读取数据
 */
public class ConversationItemCache {

  private Map<String, ConversationItem> conversationItemMap;
  private LocalStorage conversationItemDBHelper;

  private ConversationItemCache() {
    conversationItemMap = new HashMap<String, ConversationItem>();
  }

  private static ConversationItemCache conversationItemCache;

  public static synchronized ConversationItemCache getInstance() {
    if (null == conversationItemCache) {
      conversationItemCache = new ConversationItemCache();
    }
    return conversationItemCache;
  }

  /**
   * 因为只有在第一次的时候需要设置 Context 以及 clientId，所以单独拎出一个函数主动调用初始化
   * 避免 getInstance 传入过多参数
   */
  public synchronized void initDB(Context context, String clientId, AVCallback callback) {
    conversationItemDBHelper = new LocalStorage(context, clientId, "unreadCount");
    syncData(callback);
  }

  /**
   * 此处的消息未读数量仅仅指的是本机的未读消息数量，并没有存储到 server 端
   * 在收到消息时消息未读数量 + 1
   *
   * @param convid
   */
  public void increaseUnreadCount(String convid) {
    increaseUnreadCount(convid, 1);
  }

  /**
   * 在原来的基础增加未读的消息数量
   *
   * @param convId
   * @param increment
   */
  public synchronized void increaseUnreadCount(String convId, int increment) {
    ConversationItem conversationItem = getConversationItemFromMap(convId);
    conversationItem.unreadCount += increment;
    syncToCache(conversationItem);
  }

  /**
   * 清空未读的消息数量
   *
   * @param conviId
   */
  public synchronized void clearUnread(String conviId) {
    ConversationItem unreadCountItem = getConversationItemFromMap(conviId);
    unreadCountItem.unreadCount = 0;
    syncToCache(unreadCountItem);
  }

  /**
   * 删除该 Conversation 未读数量的缓存
   *
   * @param convid
   */
  public synchronized void deleteConversation(String convid) {
    conversationItemMap.remove(convid);
    conversationItemDBHelper.deleteDatas(Arrays.asList(convid));
  }

  /**
   * 缓存该 Conversastoin，默认未读数量为 0
   *
   * @param convId
   */
  public synchronized void insertConversation(String convId) {
    syncToCache(getConversationItemFromMap(convId));
  }

  /**
   * 获取该 Conversation 的未读数量
   *
   * @param convId
   * @return
   */
  public synchronized int getUnreadCount(String convId) {
    return getConversationItemFromMap(convId).unreadCount;
  }

  /**
   * 获得排序后的 Conversation Id list，根据本地更新时间降序排列
   * @return
   */
  public synchronized List<String> getSortedConversationList() {
    List<String> idList = new ArrayList<>();
    SortedSet<ConversationItem> sortedSet = new TreeSet<>();
    sortedSet.addAll(conversationItemMap.values());
    for (ConversationItem item : sortedSet) {
      idList.add(item.conversationId);
    }
    return idList;
  }

  /**
   * 同步 db 数据到内存中
   */
  private void syncData(final AVCallback callback) {
    conversationItemDBHelper.getIds(new AVCallback<List<String>>() {
      @Override
      protected void internalDone0(final List<String> idList, AVException e) {
        conversationItemDBHelper.getDatas(idList, new AVCallback<List<String>>() {
          @Override
          protected void internalDone0(final List<String> dataList, AVException e) {
            for (int i = 0; i < idList.size(); i++) {
              conversationItemMap.put(idList.get(i), ConversationItem.fromJsonString(dataList.get(i)));
            }
            callback.internalDone(null);
          }
        });
      }
    });
  }

  /**
   * 从 map 中获取 ConversationItem，如缓存中没有，则 new 一个新实力返回
   * @param convId
   * @return
   */
  private ConversationItem getConversationItemFromMap(String convId) {
    if (conversationItemMap.containsKey(convId)) {
      return conversationItemMap.get(convId);
    }
    return new ConversationItem(convId);
  }

  /**
   * 存储未读消息数量到内存
   */
  private void syncToCache(ConversationItem item) {
    item.updateTime = System.currentTimeMillis();
    conversationItemMap.put(item.conversationId, item);
    conversationItemDBHelper.insertData(item.conversationId, item.toJsonString());
  }
}
