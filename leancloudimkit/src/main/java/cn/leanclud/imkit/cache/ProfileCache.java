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

  public void initDB(Context context, String clientId) {
    profileDBHelper = new LocalStorage(context, clientId, "ProfileCache");
  }

  public void getCachedUser(final String objectId, final AVCallback<LCIMUserProfile> callback) {
    if (userMap.containsKey(objectId)) {
      callback.internalDone(userMap.get(objectId), null);
    } else {
      profileDBHelper.getDatas(Arrays.asList(objectId), new AVCallback<List<String>>() {
        @Override
        protected void internalDone0(List<String> dataList, AVException e) {
          if (null != dataList && !dataList.isEmpty()) {
            LCIMUserProfile userProfile = getUserProfileFromJson(dataList.get(0));
            userMap.put(userProfile.getUserId(), userProfile);
            callback.internalDone(getUserProfileFromJson(dataList.get(0)), null);
          } else {
            getUserProfile(objectId, new AVCallback<LCIMUserProfile>() {
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

  public boolean hasCachedUser(String id) {
    return userMap.containsKey(id);
  }

  public void cacheUser(LCIMUserProfile userProfile) {
    if (null != userProfile) {
      userMap.put(userProfile.getUserId(), userProfile);
      profileDBHelper.insertData(userProfile.getUserId(), getStringFormUserProfile(userProfile));
    }
  }

  private LCIMUserProfile getUserProfileFromJson(String str) {
    JSONObject jsonObject = JSONObject.parseObject(str);
    String userName = jsonObject.getString(USER_NAME);
    String userId = jsonObject.getString(USER_ID);
    String userAvatar = jsonObject.getString(USER_AVATAR);
    return new LCIMUserProfile(userId, userName, userAvatar);
  }

  private String getStringFormUserProfile(LCIMUserProfile userProfile) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(USER_NAME, userProfile.getUserName());
    jsonObject.put(USER_AVATAR, userProfile.getAvatarUrl());
    jsonObject.put(USER_ID, userProfile.getUserId());
    return jsonObject.toString();
  }
}
