package cn.leanclud.imkit.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import cn.leanclud.imkit.R;


/**
 * Created by wli on 15/11/26.
 */
public class LCIMCommonFooterItemHolder extends LCIMCommonViewHolder {
  LinearLayout rootLayout;

  public LCIMCommonFooterItemHolder(Context context, ViewGroup root) {
    super(context, root, R.layout.lcim_common_footer_item_layout);
    rootLayout = (LinearLayout)itemView.findViewById(R.id.common_footer_root_view);
  }

  public void setView(View view) {
    rootLayout.removeAllViews();
    if (null != view) {
      rootLayout.addView(view);
    }
  }

  @Override
  public void bindData(Object o) {}
}
