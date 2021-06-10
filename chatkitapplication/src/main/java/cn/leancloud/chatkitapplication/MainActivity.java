package cn.leancloud.chatkitapplication;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cn.leancloud.im.v2.LCIMChatRoom;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.callback.LCIMClientCallback;
import cn.leancloud.im.v2.callback.LCIMConversationCreatedCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.activity.LCIMContactFragment;
import cn.leancloud.chatkit.activity.LCIMConversationActivity;
import cn.leancloud.chatkit.activity.LCIMConversationListFragment;
import cn.leancloud.chatkit.utils.LCIMConstants;

public class MainActivity extends AppCompatActivity {
  private static Logger logger = Logger.getLogger(MainActivity.class.getSimpleName());

  private Toolbar toolbar;
  private ViewPager viewPager;
  private TabLayout tabLayout;

  /**
   * 上一次点击 back 键的时间
   * 用于双击退出的判断
   */
  private static long lastBackTime = 0;

  /**
   * 当双击 back 键在此间隔内是直接触发 onBackPressed
   */
  private final int BACK_INTERVAL = 1000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    viewPager = (ViewPager)findViewById(R.id.pager);
    tabLayout = (TabLayout)findViewById(R.id.tablayout);
    setTitle(R.string.app_name);
    setSupportActionBar(toolbar);
    initTabLayout();
    Log.d("MainActivity", "onCreate finished.");
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d("MainActivity", "onStart finished.");
  }

  @Override
  protected void onResume() {
    super.onResume();
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_square, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int menuId = item.getItemId();
    if (menuId == R.id.menu_square_members) {
      gotoSquareConversation();
    } else if (menuId == R.id.menu_quit) {
      LCChatKit.getInstance().close(new LCIMClientCallback() {
        @Override
        public void done(LCIMClient LCIMClient, LCIMException e) {
          if (null!= e) {
            e.printStackTrace();
          } else {
            MainActivity.this.finish();
          }
        }
      });
    }
    return super.onOptionsItemSelected(item);
  }

  private void initTabLayout() {
    String[] tabList = new String[]{"会话", "联系人"};
    final Fragment[] fragmentList = new Fragment[] {new LCIMConversationListFragment(),
      new LCIMContactFragment()};

    tabLayout.setTabMode(TabLayout.MODE_FIXED);
    for (int i = 0; i < tabList.length; i++) {
      tabLayout.addTab(tabLayout.newTab().setText(tabList[i]));
    }

    TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(),
      Arrays.asList(fragmentList), Arrays.asList(tabList));
    viewPager.setAdapter(adapter);
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
        if (0 == position) {
//          EventBus.getDefault().post(new ConversationFragmentUpdateEvent());
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });
    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setTabsFromPagerAdapter(adapter);
  }

  @Override
  public void onBackPressed() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastBackTime < BACK_INTERVAL) {
      super.onBackPressed();
    } else {
      Toast.makeText(this, "双击 back 退出", Toast.LENGTH_SHORT).show();
    }
    lastBackTime = currentTime;
  }

  public class TabFragmentAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragments;
    private List<String> mTitles;

    public TabFragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
      super(fm);
      mFragments = fragments;
      mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override
    public int getCount() {
      return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mTitles.get(position);
    }
  }

  private void gotoSquareConversation() {
    List<LCChatKitUser> userList = CustomUserProvider.getInstance().getAllUsers();
    List<String> idList = new ArrayList<>();
    for (LCChatKitUser user : userList) {
      idList.add(user.getUserId());
    }
    LCChatKit.getInstance().getClient().createChatRoom(
      idList, getString(R.string.square), null, true, new LCIMConversationCreatedCallback() {
        @Override
        public void done(LCIMConversation LCIMConversation, LCIMException e) {
          if (LCIMConversation instanceof LCIMChatRoom) {
            Intent intent = new Intent(MainActivity.this, LCIMConversationActivity.class);
            intent.putExtra(LCIMConstants.CONVERSATION_ID, LCIMConversation.getConversationId());
            startActivity(intent);
          } else {
            logger.log(Level.WARNING, "createChatRoom is wrong!");
          }
        }
      });
  }
}
