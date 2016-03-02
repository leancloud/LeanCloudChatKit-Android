package cn.leanclud.imkit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.leanclud.imkit.R;


/**
 * Created by wli on 15/11/26.
 */
public class LCIMLoadMoreFooterView extends LinearLayout {
  private Context mContext;
  private View mProgressBar;
  private TextView mHintView;

  public LCIMLoadMoreFooterView(Context context) {
    super(context);
    initView(context);
  }

  public LCIMLoadMoreFooterView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    mContext = context;
    LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext)
      .inflate(R.layout.lcim_load_more_footer_layout, null);
    addView(moreView);
    moreView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
      android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

    mProgressBar = moreView.findViewById(R.id.load_more_footer_progressbar);
    mHintView = (TextView) moreView.findViewById(R.id.load_more_footer_hint_textview);
  }

  public void onLoadStatusChanged(int staus) {
    if (staus == LCIMRefreshableRecyclerView.STATUS_LAOD_MORE) {
      mProgressBar.setVisibility(View.VISIBLE);
      mHintView.setVisibility(GONE);
    } else {
      mProgressBar.setVisibility(GONE);
      mHintView.setVisibility(View.VISIBLE);
    }
  }
}
