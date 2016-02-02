package cn.leanclud.leancloudimkit.controller;

import android.content.Context;

import com.avos.avoscloud.SignatureFactory;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;

/**
 * Created by wli on 16/2/2.
 * TODO: 稍后添加注释
 */
public final class ChatManager {

  private static ChatManager chatManager;
  private ProfileProvider profileProvider;
  private String currentClientId;

  private ChatManager() {}

  public static synchronized ChatManager getInstance() {
    if (null == chatManager) {
      chatManager = new ChatManager();
    }
    return chatManager;
  }

  public void init(Context context, String appId, String appKey) {
  }

  public void setProfileProvider(ProfileProvider profileProvider) {
    this.profileProvider =  profileProvider;
  }

  public void setSignatureFactory(SignatureFactory signatureFactory) {
  }

  public void open(String clientId, AVIMClientCallback callback) {
  }

  public void close(AVIMClientCallback callback) {
  }
}
