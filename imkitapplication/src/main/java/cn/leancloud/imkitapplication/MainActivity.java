package cn.leancloud.imkitapplication;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import cn.leancloud.imkit.activity.LCIMConversationListFragment;

public class MainActivity extends AppCompatActivity {

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
  }

  private void initTabLayout() {
    String[] tabList = new String[]{"会话", "联系人"};
    final Fragment[] fragmentList = new Fragment[] {new LCIMConversationListFragment(),
      new ContactFragment()};

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


//  /**
//   * 用来测试群组聊天，输入时把 id 用逗号隔开就可以
//   * @param content
//   */
//  private void gotoGroupConversation(String content) {
//    List<String> idList = new ArrayList<>();
//    idList.addAll(Arrays.asList(TextUtils.split(content, ",")));
//    LCIMKit.getInstance().getClient().createConversation(
//      idList, "", null, false, true, new AVIMConversationCreatedCallback() {
//        @Override
//        public void done(AVIMConversation avimConversation, AVIMException e) {
//          Intent intent = new Intent(MainActivity.this, LCIMConversationActivity.class);
//          intent.putExtra(LCIMConstants.CONVERSATION_ID, avimConversation.getConversationId());
//          startActivity(intent);
//        }
//      });
//  }
}
