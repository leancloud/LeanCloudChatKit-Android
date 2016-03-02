package cn.leanclud.imkit;

import android.content.Context;

import com.avos.avoscloud.SignatureFactory;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;

/**
 * Created by wli on 16/2/2.
 * TODO: 稍后添加注释
 */
public final class LCIMKit {

  private static LCIMKit lcimKit;
  private LCIMProfileProvider profileProvider;
  private String currentClientId;

  private LCIMKit() {}

  public static synchronized LCIMKit getInstance() {
    if (null == lcimKit) {
      lcimKit = new LCIMKit();
    }
    return lcimKit;
  }

  public void init(Context context, String appId, String appKey) {
  }

  public void setProfileProvider(LCIMProfileProvider profileProvider) {
    this.profileProvider =  profileProvider;
  }

  public void setSignatureFactory(SignatureFactory signatureFactory) {
  }

  public void open(String clientId, AVIMClientCallback callback) {
  }

  public void close(AVIMClientCallback callback) {
  }
}
