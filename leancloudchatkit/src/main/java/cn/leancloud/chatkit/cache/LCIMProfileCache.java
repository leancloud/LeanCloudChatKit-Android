package cn.leancloud.chatkit.cache;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.chatkit.LCIMKit;
import cn.leancloud.chatkit.LCIMKitUser;
import cn.leancloud.chatkit.LCIMProfileProvider;
import cn.leancloud.chatkit.LCIMProfilesCallBack;


/**
 * Created by wli on 16/2/25.
 * 用户信息缓存
 * 流程：
 * 1、如果内存中有则从内存中获取
 * 2、如果内存中没有则从 db 中获取
 * 3、如果 db 中没有则通过调用开发者设置的回调 LCIMProfileProvider.fetchProfiles 来获取
 * 同时获取到的数据会缓存到内存与 db
 */
public class LCIMProfileCache {

  private static final String USER_NAME = "user_name";
  private static final String USER_AVATAR = "user_avatar";
  private static final String USER_ID = "user_id";

  private Map<String, LCIMKitUser> userMap;
  private LCIMLocalStorage profileDBHelper;

  private LCIMProfileCache() {
    userMap = new HashMap<>();
  }

  private static LCIMProfileCache profileCache;

  public static synchronized LCIMProfileCache getInstance() {
    if (null == profileCache) {
      profileCache = new LCIMProfileCache();
    }
    return profileCache;
  }

  /**
   * 因为只有在第一次的时候需要设置 Context 以及 clientId，所以单独拎出一个函数主动调用初始化
   * 避免 getInstance 传入过多参数
   *
   * @param context
   * @param clientId
   */
  public synchronized void initDB(Context context, String clientId) {
    profileDBHelper = new LCIMLocalStorage(context, clientId, "ProfileCache");
  }

  /**
   * 根据 id 获取用户信息
   * 先从缓存中获取，若没有再调用用户回调获取
   *
   * @param id
   * @param callback
   */
  public synchronized void getCachedUser(final String id, final AVCallback<LCIMKitUser> callback) {
    getCachedUsers(Arrays.asList(id), new AVCallback<List<LCIMKitUser>>() {
      @Override
      protected void internalDone0(List<LCIMKitUser> lcimUserProfiles, AVException e) {
        LCIMKitUser lcimKitUser =
          (null != lcimUserProfiles && !lcimUserProfiles.isEmpty() ? lcimUserProfiles.get(0) : null);
        callback.internalDone(lcimKitUser, e);
      }
    });
  }

  /**
   * 获取多个用户的信息
   * 先从缓存中获取，若没有再调用用户回调获取
   *
   * @param idList
   * @param callback
   */
  public synchronized void getCachedUsers(List<String> idList, final AVCallback<List<LCIMKitUser>> callback) {
    if (null != callback) {
      if (null == idList || idList.isEmpty()) {
        callback.internalDone(null, new AVException(new Throwable("idList is empty!")));
      } else {
        final List<LCIMKitUser> profileList = new ArrayList<LCIMKitUser>();
        final List<String> unCachedIdList = new ArrayList<String>();

        for (String id : idList) {
          if (userMap.containsKey(id)) {
            profileList.add(userMap.get(id));
          } else {
            unCachedIdList.add(id);
          }
        }

        if (unCachedIdList.isEmpty()) {
          callback.internalDone(profileList, null);
        } else if (null != profileDBHelper) {
          profileDBHelper.getData(idList, new AVCallback<List<String>>() {
            @Override
            protected void internalDone0(List<String> strings, AVException e) {
              if (null != strings && !strings.isEmpty() && strings.size() == unCachedIdList.size()) {
                List<LCIMKitUser> profileList = new ArrayList<LCIMKitUser>();
                for (String data : strings) {
                  LCIMKitUser userProfile = getUserProfileFromJson(data);
                  userMap.put(userProfile.getUserId(), userProfile);
                  profileList.add(userProfile);
                }
                callback.internalDone(profileList, null);
              } else {
                getProfilesFromProvider(unCachedIdList, profileList, callback);
              }
            }
          });
        } else {
          getProfilesFromProvider(unCachedIdList, profileList, callback);
        }
      }
    }
  }

  /**
   * 根据 id 通过开发者设置的回调获取用户信息
   *
   * @param idList
   * @param callback
   */
  private void getProfilesFromProvider(List<String> idList, final List<LCIMKitUser> profileList,
                                       final AVCallback<List<LCIMKitUser>> callback) {
    LCIMProfileProvider profileProvider = LCIMKit.getInstance().getProfileProvider();
    if (null != profileProvider) {
      profileProvider.fetchProfiles(idList, new LCIMProfilesCallBack() {
        @Override
        public void done(List<LCIMKitUser> userList, Exception e) {
          if (null != userList) {
            for (LCIMKitUser userProfile : userList) {
              cacheUser(userProfile);
            }
          }
          profileList.addAll(userList);
          callback.internalDone(profileList, null != e ? new AVException(e) : null);
        }
      });
    } else {
      callback.internalDone(null, new AVException(new Throwable("please setProfileProvider first!")));
    }
  }

  /**
   * 根据 id 获取用户名
   *
   * @param id
   * @param callback
   */
  public void getUserName(String id, final AVCallback<String> callback) {
    getCachedUser(id, new AVCallback<LCIMKitUser>() {
      @Override
      protected void internalDone0(LCIMKitUser userProfile, AVException e) {
        String userName = (null != userProfile ? userProfile.getUserName() : null);
        callback.internalDone(userName, e);
      }
    });
  }

  /**
   * 根据 id 获取用户头像
   *
   * @param id
   * @param callback
   */
  public void getUserAvatar(String id, final AVCallback<String> callback) {
    getCachedUser(id, new AVCallback<LCIMKitUser>() {
      @Override
      protected void internalDone0(LCIMKitUser userProfile, AVException e) {
        String avatarUrl = (null != userProfile ? userProfile.getAvatarUrl() : null);
        callback.internalDone(avatarUrl, e);
      }
    });
  }

  /**
   * 内存中是否包相关 LCIMKitUser 的信息
   *
   * @param id
   * @return
   */
  public synchronized boolean hasCachedUser(String id) {
    return userMap.containsKey(id);
  }

  /**
   * 缓存 LCIMKitUser 信息，更新缓存同时也更新 db
   * 如果开发者 LCIMKitUser 信息变化，可以通过调用此方法刷新缓存
   *
   * @param userProfile
   */
  public synchronized void cacheUser(LCIMKitUser userProfile) {
    if (null != userProfile && null != profileDBHelper) {
      userMap.put(userProfile.getUserId(), userProfile);
      profileDBHelper.insertData(userProfile.getUserId(), getStringFormUserProfile(userProfile));
    }
  }

  /**
   * 从 db 中的 String 解析出 LCIMKitUser
   *
   * @param str
   * @return
   */
  private LCIMKitUser getUserProfileFromJson(String str) {
    JSONObject jsonObject = JSONObject.parseObject(str);
    String userName = jsonObject.getString(USER_NAME);
    String userId = jsonObject.getString(USER_ID);
    String userAvatar = jsonObject.getString(USER_AVATAR);
    return new LCIMKitUser(userId, userName, userAvatar);
  }

  /**
   * LCIMKitUser 转换成 json String
   *
   * @param userProfile
   * @return
   */
  private String getStringFormUserProfile(LCIMKitUser userProfile) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(USER_NAME, userProfile.getUserName());
    jsonObject.put(USER_AVATAR, userProfile.getAvatarUrl());
    jsonObject.put(USER_ID, userProfile.getUserId());
    return jsonObject.toJSONString();
  }
}
