package cn.leanclud.imkitapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leanclud.imkit.LCIMKit;
import cn.leanclud.imkit.activity.LCIMConversationActivity;
import cn.leanclud.imkit.utils.LCIMConstants;

public class MainActivity extends AppCompatActivity {

  private TextView peerIdView;
  private Button chatButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    peerIdView = (TextView)findViewById(R.id.peer_id_view);
    chatButton = (Button)findViewById(R.id.chat_btn);

    chatButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String content = peerIdView.getText().toString();
        if (content.contains(",")) {
          gotoGroupConversation(content);
        } else {
          Intent intent = new Intent(MainActivity.this, LCIMConversationActivity.class);
          intent.putExtra(LCIMConstants.PEER_ID, content);
          startActivity(intent);
        }
      }
    });
  }

  /**
   * 用来测试群组聊天，输入时把 id 用逗号隔开就可以
   * @param content
   */
  private void gotoGroupConversation(String content) {
    List<String> idList = new ArrayList<>();
    idList.addAll(Arrays.asList(TextUtils.split(content, ",")));
    LCIMKit.getInstance().getClient().createConversation(
      idList, "", null, false, true, new AVIMConversationCreatedCallback() {
        @Override
        public void done(AVIMConversation avimConversation, AVIMException e) {
          Intent intent = new Intent(MainActivity.this, LCIMConversationActivity.class);
          intent.putExtra(LCIMConstants.CONVERSATION_ID, avimConversation.getConversationId());
          startActivity(intent);
        }
      });
  }
}
