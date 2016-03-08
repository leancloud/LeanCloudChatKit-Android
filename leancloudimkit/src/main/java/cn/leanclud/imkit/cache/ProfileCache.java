package cn.leanclud.imkit.cache;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leanclud.imkit.LCIMKit;
import cn.leanclud.imkit.LCIMProfileProvider;
import cn.leanclud.imkit.LCIMProfilesCallBack;
import cn.leanclud.imkit.LCIMUserProfile;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by wli on 16/2/25.
 * 用户信息缓存
 * 流程：
 * 1、如果内存中有则从内存中获取
 * 2、如果内存中没有则从 db 中获取
 * 3、如果 db 中没有则通过调用开发者设置的回调 LCIMProfileProvider.getProfiles 来获取
 *    同时获取到的数据会缓存到内存与 db
 */
public class ProfileCache {

  private static final String USER_NAME = "user_name";
  private static final String USER_AVATAR = "user_avatar";
  private static final String USER_ID = "user_id";

  private Map<String, LCIMUserProfile> userMap;
  private LocalStorage profileDBHelper;

  private ProfileCache() {
    userMap = new HashMap<>();
  }

  private static ProfileCache profileCache;

  public static synchronized ProfileCache getInstance() {
    if (null == profileCache) {
      profileCache = new ProfileCache();
    }
    return profileCache;
  }

  /**
   * 因为只有在第一次的时候需要设置 Context 以及 clientId，所以单独拎出一个函数主动调用初始化
   * 避免 getInstance 传入过多参数
   * @param context
   * @param clientId
   */
  public synchronized void initDB(Context context, String clientId) {
    profileDBHelper = new LocalStorage(context, clientId, "ProfileCache");
  }

  /**
   * 根据 id 获取用户信息
   * @param id
   * @param callback
   */
  public synchronized void getCachedUser(final String id, final AVCallback<LCIMUserProfile> callback) {
    if (userMap.containsKey(id)) {
      callback.internalDone(userMap.get(id), null);
    } else {
      profileDBHelper.getDatas(Arrays.asList(id), new AVCallback<List<String>>() {
        @Override
        protected void internalDone0(List<String> dataList, AVException e) {
          if (null != dataList && !dataList.isEmpty()) {
            LCIMUserProfile userProfile = getUserProfileFromJson(dataList.get(0));
            userMap.put(userProfile.getUserId(), userProfile);
            callback.internalDone(getUserProfileFromJson(dataList.get(0)), null);
          } else {
            getUserProfile(id, new AVCallback<LCIMUserProfile>() {
              @Override
              protected void internalDone0(LCIMUserProfile userProfile, AVException e) {
                if (null != e) {
                  callback.internalDone(null, e);
                } else if (null != userProfile) {
                  callback.internalDone(userProfile, null);
                  cacheUser(userProfile);
                } else {
                  callback.internalDone(userProfile, new AVException(new Throwable("can not find this objectId")));
                }
              }
            });
          }
        }
      });
    }
  }

  /**
   * 根据 id 通过开发者设置的回调获取用户信息
   * @param id
   * @param callback
   */
  private void getUserProfile(String id, final AVCallback<LCIMUserProfile> callback) {
    LCIMProfileProvider profileProvider = LCIMKit.getInstance().getProfileProvider();
    if (null != profileProvider) {
      profileProvider.getProfiles(Arrays.asList(id), new LCIMProfilesCallBack() {
        @Override
        public void done(List<LCIMUserProfile> userList, Exception e) {
          if (null == userList || userList.isEmpty()) {
            callback.internalDone(null, new AVException(new Throwable("can not find current id!")));
          } else {
            callback.internalDone(userList.get(0), null);
          }
        }
      });
    }
  }

  /**
   * 根据 id 获取用户名
   * @param id
   * @param callback
   */
  public void getUserName(String id, final AVCallback<String> callback) {
    getCachedUser(id, new AVCallback<LCIMUserProfile>() {
      @Override
      protected void internalDone0(LCIMUserProfile lcimUserProfile, AVException e) {
        if (null != e) {
          callback.internalDone(null, e);
        } else {
          callback.internalDone(lcimUserProfile.getUserName(), null);
        }
      }
    });
  }

  /**
   * 根据 id 获取用户头像
   * @param id
   * @param callback
   */
  public void getUserAvatar(String id, final AVCallback<String> callback) {
    getCachedUser(id, new AVCallback<LCIMUserProfile>() {
      @Override
      protected void internalDone0(LCIMUserProfile userProfile, AVException e) {
        if (null != e) {
          callback.internalDone(null, e);
        } else {
          callback.internalDone(userProfile.getAvatarUrl(), null);
        }
      }
    });
  }

  /**
   * 内存中是否包相关 LCIMUserProfile 的信息
   * @param id
   * @return
   */
  public synchronized boolean hasCachedUser(String id) {
    return userMap.containsKey(id);
  }

  /**
   * 缓存 LCIMUserProfile 信息，更新缓存同时也更新 db
   * 如果开发者 LCIMUserProfile 信息变化，可以通过调用此方法刷新缓存
   * @param userProfile
   */
  public synchronized void cacheUser(LCIMUserProfile userProfile) {
    if (null != userProfile) {
      userMap.put(userProfile.getUserId(), userProfile);
      profileDBHelper.insertData(userProfile.getUserId(), getStringFormUserProfile(userProfile));
    }
  }

  /**
   * 从 db 中的 String 解析出 LCIMUserProfile
   * @param str
   * @return
   */
  private LCIMUserProfile getUserProfileFromJson(String str) {
    JSONObject jsonObject = JSONObject.parseObject(str);
    String userName = jsonObject.getString(USER_NAME);
    String userId = jsonObject.getString(USER_ID);
    String userAvatar = jsonObject.getString(USER_AVATAR);
    return new LCIMUserProfile(userId, userName, userAvatar);
  }

  /**
   * LCIMUserProfile 转换成 json String
   * @param userProfile
   * @return
   */
  private String getStringFormUserProfile(LCIMUserProfile userProfile) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(USER_NAME, userProfile.getUserName());
    jsonObject.put(USER_AVATAR, userProfile.getAvatarUrl());
    jsonObject.put(USER_ID, userProfile.getUserId());
    return jsonObject.toJSONString();
  }
}
