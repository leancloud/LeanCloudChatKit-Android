package cn.leancloud.chatkit.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;

import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.adapter.LCIMCommonListAdapter;

public class LCIMUserSelectActivity extends AppCompatActivity {
  public static final String KEY_USERS = "users";
  public static final String KEY_TITLE = "title";
  public static final String KEY_RESULT_DATA = "result_data";

  private LCIMContactFragment contactFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lcim_contact_activity);
    contactFragment = (LCIMContactFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
    contactFragment.setHasOptionsMenu(true);
    contactFragment.setListMode(LCIMCommonListAdapter.ListMode.SELECT);
    String title = getIntent().getStringExtra(KEY_TITLE);
    initActionBar(title);
    LCChatKitUser[] users = (LCChatKitUser[])getIntent().getParcelableArrayExtra(KEY_USERS);
    if (null != users) {
      contactFragment.setSpecifiedUsers(Arrays.asList(users));
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


}
