package cn.leancloud.chatkit.activity;

import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import cn.leancloud.json.JSON;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.LCChatProfilesCallBack;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.adapter.LCIMCommonListAdapter;
import cn.leancloud.chatkit.event.LCIMMemberSelectedChangeEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.view.LCIMDividerItemDecoration;
import cn.leancloud.chatkit.viewholder.LCIMContactItemHolder;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.callback.LCIMConversationCallback;
import cn.leancloud.im.v2.callback.LCIMConversationMemberQueryCallback;
import cn.leancloud.im.v2.callback.LCIMConversationSimpleResultCallback;
import cn.leancloud.im.v2.callback.LCIMOperationFailure;
import cn.leancloud.im.v2.callback.LCIMOperationPartiallySucceededCallback;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;
import cn.leancloud.im.v2.conversation.ConversationMemberRole;
import de.greenrobot.event.EventBus;

public class LCIMConversationDetailActivity extends AppCompatActivity {
  private static final int REQUEST_CODE_ADD_ADMIN = 10;
  private static final int REQUEST_CODE_MANAGE_ADMIN = 15;
  private static final int REQUEST_CODE_ADD_BLACKLIST = 20;
  private static final int REQUEST_CODE_MANAGE_BLACKLIST = 25;
  private static final int REQUEST_CODE_INVITATION = 30;

  LCIMConversation LCIMConversation;
  List<String> adminMembers = new ArrayList<>();
  List<String> blockedUsers = new ArrayList<>();

  ImageButton adminAddButton;
  ImageButton adminUserButton;
  ImageButton adminMoreButton;
  ImageButton blacklistAddButton;
  ImageButton blacklistUserButton;
  ImageButton blacklistMoreButton;
  Button inviteButton;
  Button removeButton;

  RecyclerView recyclerView;
  protected LCIMCommonListAdapter<LCChatKitUser> itemAdapter;

  private Set<LCChatKitUser> selectedUsers = new HashSet<>();

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
    removeButton = (Button) findViewById(R.id.remove_button);

    recyclerView = (RecyclerView) findViewById(R.id.convMemberList);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new LCIMDividerItemDecoration(this));
    itemAdapter = new LCIMCommonListAdapter<LCChatKitUser>(LCIMContactItemHolder.class);
    itemAdapter.setMode(LCIMCommonListAdapter.ListMode.SELECT);
    recyclerView.setAdapter(itemAdapter);

    adminAddButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null == LCIMConversation) {
          return;
        }
        List<String> members = LCIMConversation.getMembers();
        LCChatKit.getInstance().getProfileProvider().fetchProfiles(members, new LCChatProfilesCallBack() {
          @Override
          public void done(List<LCChatKitUser> users, Exception exception) {
            if (null != exception) {
              ;
            } else {
              System.out.println("members: " + JSON.toJSONString(users));
              final Intent intent = new Intent(LCIMConversationDetailActivity.this, LCIMUserSelectActivity.class);
              intent.putExtra(LCIMUserSelectActivity.KEY_USERS, JSON.toJSONString(users));
              intent.putExtra(LCIMUserSelectActivity.KEY_TITLE, "Select Admin Member");
              startActivityForResult(intent, REQUEST_CODE_ADD_ADMIN);
            }
          }
        });
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
        intent.putExtra(LCIMUserSelectActivity.KEY_TITLE, "Contact");
        startActivityForResult(intent, REQUEST_CODE_INVITATION);
      }
    });

    removeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (selectedUsers.size() < 1) {
          showToast("please select some members at first.");
        } else {
          List<String> removeMembers = new ArrayList<>(selectedUsers.size());
          Iterator<LCChatKitUser> it = selectedUsers.iterator();
          while(it.hasNext()) {
            removeMembers.add(it.next().getUserId());
          }
          LCIMConversation.kickMembers(removeMembers, new LCIMOperationPartiallySucceededCallback() {
            @Override
            public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
              if (null != e) {
                showToast("failed to kick members. cause: " + e.getMessage());
              } else {
                selectedUsers.clear();
                refreshMemberList(true);
              }
            }
          });
        }
      }
    });

    String title = getResources().getString(R.string.lcim_conversation_detail);
    initActionBar(title);

    String conversationId = getIntent().getStringExtra(LCIMConstants.CONVERSATION_ID);
    initConversation(conversationId);
  }

  @Override
  public void onResume() {
    super.onResume();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    EventBus.getDefault().unregister(this);
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
      this.LCIMConversation.addMembers(userIds, new LCIMOperationPartiallySucceededCallback() {
        @Override
        public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
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
        this.LCIMConversation.updateMemberRole(m.getUserId(), ConversationMemberRole.MANAGER, new LCIMConversationCallback() {
          @Override
          public void done(LCIMException e) {
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
      this.LCIMConversation.blockMembers(userIds, new LCIMOperationPartiallySucceededCallback() {
        @Override
        public void done(LCIMException e, List<String> list, List<LCIMOperationFailure> list1) {
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
    LCIMConversation = LCChatKit.getInstance().getClient().getConversation(conversationId);

    adminMembers.clear();
    blockedUsers.clear();

    LCIMConversation.getAllMemberInfo(0, 100, new LCIMConversationMemberQueryCallback() {
      @Override
      public void done(List<LCIMConversationMemberInfo> list, LCIMException e) {
        if (null != e) {
          showToast("faied to query memberInfo. cause: " + e.getMessage());
        } else if (null != list && list.size() > 0) {
          for (LCIMConversationMemberInfo m: list) {
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
    LCIMConversation.queryBlockedMembers(0, 100, new LCIMConversationSimpleResultCallback() {
      @Override
      public void done(List<String> list, LCIMException e) {
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
      LCIMConversation.updateInfoInBackground(new LCIMConversationCallback() {
        @Override
        public void done(LCIMException e) {
          if (null == e) {
            List<String> members = LCIMConversation.getMembers();
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
      List<String> members = LCIMConversation.getMembers();
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

  public void onEvent(LCIMMemberSelectedChangeEvent event) {
    System.out.println("eventHandler. isChecked=" + event.isSelected + ", user=" + event.member);
    if (null != event && null != event.member) {
      if (event.isSelected) {
        this.selectedUsers.add(event.member);
      } else {
        this.selectedUsers.remove(event.member);
      }
    }
  }
}
