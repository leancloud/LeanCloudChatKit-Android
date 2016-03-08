package cn.leanclud.imkit.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;

import java.util.Arrays;

import cn.leanclud.imkit.LCIMKit;
import cn.leanclud.imkit.R;
import cn.leanclud.imkit.cache.ConversationItemCache;
import cn.leanclud.imkit.utils.LCIMConstants;
import cn.leanclud.imkit.utils.LCIMConversationUtils;

/**
 * Created by wli on 16/2/29.
 * 会话详情页
 */
public class LCIMConversationActivity extends AppCompatActivity {

  protected LCIMConversationFragment conversationFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lcim_conversation_activity);
    conversationFragment = (LCIMConversationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
    initByIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    initByIntent(intent);
  }

  private void initByIntent(Intent intent) {
    if (null == LCIMKit.getInstance().getClient()) {
      showToast("please login first!");
      finish();
      return;
    }

    Bundle extras = intent.getExtras();
    if (null != extras) {
      if (extras.containsKey(LCIMConstants.PEER_ID)) {
        getConversation(extras.getString(LCIMConstants.PEER_ID));
      } else if (extras.containsKey(LCIMConstants.CONVERSATION_ID)) {
        String conversationId = extras.getString(LCIMConstants.CONVERSATION_ID);
        updateConversation(LCIMKit.getInstance().getClient().getConversation(conversationId));
      } else {
        showToast("memberId or conversationId is needed");
        finish();
      }
    }
  }

  protected void initActionBar(String title) {
    ActionBar actionBar = getActionBar();
    if (null != actionBar) {
      if (null != title) {
        actionBar.setTitle(title);
      }
      actionBar.setDisplayUseLogoEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
      finishActivity(RESULT_OK);
    }
  }

  protected void updateConversation(AVIMConversation conversation) {
    if (null != conversation) {
      conversationFragment.setConversation(conversation);
      ConversationItemCache.getInstance().clearUnread(conversation.getConversationId());
      LCIMConversationUtils.getConversationName(conversation, new AVCallback<String>() {
        @Override
        protected void internalDone0(String s, AVException e) {
          initActionBar(s);
        }
      });
    }
  }

  /**
   * 获取 conversation，为了避免重复的创建，此处先 query 是否已经存在只包含该 member 的 conversation
   * 如果存在，则直接赋值给 ChatFragment，否者创建后再赋值
   */
  private void getConversation(final String memberId) {
    LCIMKit.getInstance().getClient().createConversation(
      Arrays.asList(memberId), "", null, false, true, new AVIMConversationCreatedCallback() {
        @Override
        public void done(AVIMConversation avimConversation, AVIMException e) {
          if (null != e) {
            showToast(e.getMessage());
          } else {
            updateConversation(avimConversation);
          }
        }
      });
  }

  private void showToast(String str) {
    Toast.makeText(LCIMConversationActivity.this, str, Toast.LENGTH_SHORT).show();
  }
}