package cn.leancloud.chatkitapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.leancloud.im.LCIMOptions;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.callback.LCIMClientCallback;

import cn.leancloud.chatkit.LCChatKit;

/**
 * 登陆页面
 */
public class LoginActivity extends AppCompatActivity {

  protected EditText nameView;
  protected Button loginButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    nameView = (EditText) findViewById(R.id.activity_login_et_username);
    loginButton = (Button) findViewById(R.id.activity_login_btn_login);
    loginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onLoginClick();
      }
    });
  }

  public void onLoginClick() {
    String clientId = nameView.getText().toString();
    if (TextUtils.isEmpty(clientId.trim())) {
      Toast.makeText(this, "不能为空", Toast.LENGTH_SHORT).show();
      return;
    }

    LCIMOptions.getGlobalOptions().setAutoOpen(true);
    LCChatKit.getInstance().open(clientId, new LCIMClientCallback() {
      @Override
      public void done(LCIMClient LCIMClient, LCIMException e) {
        if (null == e) {
          Intent intent = new Intent(LoginActivity.this, MainActivity.class);
          startActivity(intent);
        } else {
          Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }
}
