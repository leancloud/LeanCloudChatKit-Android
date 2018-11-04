package cn.leancloud.chatkit.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationMemberQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationSimpleResultCallback;
import com.avos.avoscloud.im.v2.callback.AVIMOperationFailure;
import com.avos.avoscloud.im.v2.callback.AVIMOperationPartiallySucceededCallback;
import com.avos.avoscloud.im.v2.conversation.AVIMConversationMemberInfo;
import com.avos.avoscloud.im.v2.conversation.ConversationMemberRole;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.LCChatProfilesCallBack;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.adapter.LCIMCommonListAdapter;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.view.LCIMDividerItemDecoration;
import cn.leancloud.chatkit.viewholder.LCIMContactItemHolder;

public class LCIMConversationDetailActivity extends AppCompatActivity {
  private static final int REQUEST_CODE_ADD_ADMIN = 10;
  private static final int REQUEST_CODE_MANAGE_ADMIN = 15;
  private static final int REQUEST_CODE_ADD_BLACKLIST = 20;
  private static final int REQUEST_CODE_MANAGE_BLACKLIST = 25;
  private static final int REQUEST_CODE_INVITATION = 30;

  AVIMConversation avimConversation;
  List<String> adminMembers = new ArrayList<>();
  List<String> blockedUsers = new ArrayList<>();

  ImageButton adminAddButton;
  ImageButton adminUserButton;
  ImageButton adminMoreButton;
  ImageButton blacklistAddButton;
  ImageButton blacklistUserButton;
  ImageButton blacklistMoreButton;
  Button inviteButton;

  RecyclerView recyclerView;
  protected LCIMCommonListAdapter<LCChatKitUser> itemAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_conversation_detail);
    adminAddButton = (ImageButton) findViewById(R.id.AdminAddButton);
    adminUserButton = (ImageButton) findViewById(R.id.AdminUserButton);
    adminMoreButton = (ImageButton) findViewById(R.id.AdminMoreButton);
    blacklistAddButton = (ImageButton) findViewById(R.id.BlackListAddButton);
    blacklistUserButton = (ImageButton) findViewById(R.id.BlackListUserButton);
    blacklistMoreButton = (ImageButton) findViewById(R.id.BlackListMoreButton);
    inviteButton = (Button) findViewById(R.id.invite_button);

    recyclerView = (RecyclerView) findViewById(R.id.convMemberList);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new LCIMDividerItemDecoration(this));
    itemAdapter = new LCIMCommonListAdapter<LCChatKitUser>(LCIMContactItemHolder.class);
    itemAdapter.setMode(LCIMCommonListAdapter.ListMode.SHOW);
    recyclerView.setAdapter(itemAdapter);

    adminAddButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LCIMConversationDetailActivity.this, LCIMUserSelectActivity.class);
        List<LCChatKitUser> users = LCChatKit.getInstance().getProfileProvider().getAllUsers();
        intent.putExtra(LCIMUserSelectActivity.KEY_USERS, users.toArray());
        intent.putExtra(LCIMUserSelectActivity.KEY_TITLE, "Select Admin Member");
        startActivityForResult(intent, REQUEST_CODE_ADD_ADMIN);
      }
    });

    adminMoreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LCIMConversationDetailActivity.this, LCIMUserSelectActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MANAGE_ADMIN);
      }
    });

    blacklistAddButton.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LCIMConversationDetailActivity.this, LCIMUserSelectActivity.class);
        List<LCChatKitUser> users = LCChatKit.getInstance().getProfileProvider().getAllUsers();
        intent.putExtra(LCIMUserSelectActivity.KEY_USERS, users.toArray());
        intent.putExtra(LCIMUserSelectActivity.KEY_TITLE, "Select Blacklist Contact");
        startActivityForResult(intent, REQUEST_CODE_ADD_BLACKLIST);
      }
    });
    blacklistMoreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LCIMConversationDetailActivity.this, LCIMUserSelectActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MANAGE_BLACKLIST);
      }
    });

    inviteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LCIMConversationDetailActivity.this, LCIMUserSelectActivity.class);
        List<LCChatKitUser> users = LCChatKit.getInstance().getProfileProvider().getAllUsers();
        intent.putExtra(LCIMUserSelectActivity.KEY_USERS, users.toArray());
        intent.putExtra(LCIMUserSelectActivity.KEY_TITLE, "Contact");
        startActivityForResult(intent, REQUEST_CODE_INVITATION);
      }
    });

    String title = getResources().getString(R.string.lcim_conversation_detail);
    initActionBar(title);

    String conversationId = getIntent().getStringExtra(LCIMConstants.CONVERSATION_ID);
    initConversation(conversationId);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (null == data || RESULT_OK != resultCode) {
      return;
    }

    String resultData = data.getStringExtra(LCIMUserSelectActivity.KEY_RESULT_DATA);
    List<LCChatKitUser> users = JSON.parseArray(resultData, LCChatKitUser.class);
    if (REQUEST_CODE_INVITATION == requestCode) {
      System.out.println("invite new members: " + users);
      List<String> userIds = new ArrayList<>(users.size());
      for (LCChatKitUser m : users) {
        userIds.add(m.getUserId());
      }
      this.avimConversation.addMembers(userIds, new AVIMConversationCallback() {
        @Override
        public void done(AVIMException e) {
          if (null != e) {
            showToast("failed to add member. cause: " + e.getMessage());
          } else {
            refreshMemberList(true);
          }
        }
      });
    } else if (REQUEST_CODE_ADD_ADMIN == requestCode) {
      System.out.println("add admins: " + users);
      for (LCChatKitUser m : users) {
        this.avimConversation.updateMemberRole(m.getUserId(), ConversationMemberRole.MANAGER, new AVIMConversationCallback() {
          @Override
          public void done(AVIMException e) {
            if (null != e) {
              showToast("failed to promote admin. cause:" + e.getMessage());
            }
          }
        });
      }
    } else if (REQUEST_CODE_ADD_BLACKLIST == requestCode) {
      System.out.println("add blacklist: " + users);
      List<String> userIds = new ArrayList<>(users.size());
      for (LCChatKitUser m : users) {
        userIds.add(m.getUserId());
      }
      this.avimConversation.blockMembers(userIds, new AVIMOperationPartiallySucceededCallback() {
        @Override
        public void done(AVIMException e, List<String> list, List<AVIMOperationFailure> list1) {
          if (null != e) {
            showToast("failed to block user. cause: " + e.getMessage());
          }
        }
      });
    }
  }
  /**
   * 设置 actionBar title 以及 up 按钮事件
   *
   * @param title
   */
  protected void initActionBar(String title) {
    ActionBar actionBar = getSupportActionBar();
    if (null != actionBar) {
      if (null != title) {
        actionBar.setTitle(title);
      }
      actionBar.setDisplayUseLogoEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
      finishActivity(RESULT_OK);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (android.R.id.home == item.getItemId()) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void initConversation(String conversationId) {
    avimConversation = LCChatKit.getInstance().getClient().getConversation(conversationId);

    adminMembers.clear();
    blockedUsers.clear();

    avimConversation.getAllMemberInfo(0, 100, new AVIMConversationMemberQueryCallback() {
      @Override
      public void done(List<AVIMConversationMemberInfo> list, AVIMException e) {
        if (null != e) {
          showToast("faied to query memberInfo. cause: " + e.getMessage());
        } else if (null != list && list.size() > 0) {
          for (AVIMConversationMemberInfo m: list) {
            if (ConversationMemberRole.MANAGER == m.getRole()) {
              adminMembers.add(m.getMemberId());
            } else {
              ;
            }
          }
        }
        if (adminMembers.size() > 0) {
          adminUserButton.setVisibility(View.VISIBLE);
          adminMoreButton.setVisibility(View.VISIBLE);
        } else {
          adminUserButton.setVisibility(View.GONE);
          adminMoreButton.setVisibility(View.GONE);
        }
      }
    });
    avimConversation.queryBlockedMembers(0, 100, new AVIMConversationSimpleResultCallback() {
      @Override
      public void done(List<String> list, AVIMException e) {
        if (null != e) {
          showToast("faied to query blocked memberInfo. cause: " + e.getMessage());
        } else if (null != list && list.size() > 0) {
          blockedUsers.addAll(list);
        }
        if (blockedUsers.size() > 0) {
          blacklistUserButton.setVisibility(View.VISIBLE);
          blacklistMoreButton.setVisibility(View.VISIBLE);
        } else {
          blacklistUserButton.setVisibility(View.GONE);
          blacklistMoreButton.setVisibility(View.GONE);
        }
      }
    });

    refreshMemberList(false);
  }

  private void refreshMemberList(boolean forceUpdate) {
    if (forceUpdate) {
      avimConversation.updateInfoInBackground(new AVIMConversationCallback() {
        @Override
        public void done(AVIMException e) {
          if (null == e) {
            List<String> members = avimConversation.getMembers();
            LCChatKit.getInstance().getProfileProvider().fetchProfiles(members, new LCChatProfilesCallBack() {
              @Override
              public void done(List<LCChatKitUser> userList, Exception exception) {
                if (null != exception) {
                  showToast("faied to query member list. cause: " + exception.getMessage());
                } else {
                  itemAdapter.setDataList(userList);
                }
              }
            });
          }
        }
      });
    } else {
      List<String> members = avimConversation.getMembers();
      LCChatKit.getInstance().getProfileProvider().fetchProfiles(members, new LCChatProfilesCallBack() {
        @Override
        public void done(List<LCChatKitUser> userList, Exception exception) {
          if (null != exception) {
            showToast("faied to query member list. cause: " + exception.getMessage());
          } else {
            itemAdapter.setDataList(userList);
          }
        }
      });
    }
  }
  /**
   * 弹出 toast
   *
   * @param content
   */
  private void showToast(String content) {
    Toast.makeText(LCIMConversationDetailActivity.this, content, Toast.LENGTH_SHORT).show();
  }
}
