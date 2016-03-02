package cn.leanclud.imkit.adapter;

import android.view.View;
import android.view.ViewGroup;

import cn.leanclud.imkit.viewholder.LCIMCommonFooterItemHolder;
import cn.leanclud.imkit.viewholder.LCIMCommonHeaderItemHolder;
import cn.leanclud.imkit.viewholder.LCIMCommonViewHolder;

/**
 * Created by wli on 15/11/25.
 * 现在还仅仅支持单类型 item，多类型 item 稍后在重构
 */
public class LCIMHeaderListAdapter<T> extends LCIMCommonListAdapter<T> {

  public static final int HEADER_ITEM_TYPE = -1;
  public static final int FOOTER_ITEM_TYPE = -2;
  public static final int COMMON_ITEM_TYPE = 1;

  private View headerView = null;
  private View footerView = null;

  public LCIMHeaderListAdapter(Class<?> vhClass) {
    super(vhClass);
  }

  public void setHeaderView(View view) {
    headerView = view;
  }

  public void setFooterView(View view) {
    footerView = view;
  }

  @Override
  public int getItemCount() {
    int itemCount = super.getItemCount();
    if (null != headerView) {
      ++itemCount;
    }
    if (null != footerView) {
      ++itemCount;
    }
    return itemCount;
  }

  @Override
  public long getItemId(int position) {
    if (position == 0 && position == getItemCount() - 1) {
      return -1;
    }
    return super.getItemId(position);
  }

  @Override
  public int getItemViewType(int position) {
    if (null != headerView && 0 == position) {
      return HEADER_ITEM_TYPE;
    }

    if (null != footerView && position == getItemCount() - 1) {
      return FOOTER_ITEM_TYPE;
    }

    return COMMON_ITEM_TYPE;
  }

  @Override
  public void onBindViewHolder(LCIMCommonViewHolder holder, int position) {
    if (position == 0 && position == getItemCount() - 1) {
      return;
    }
    super.onBindViewHolder(holder, position);
  }

  @Override
  public LCIMCommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (HEADER_ITEM_TYPE == viewType) {
      LCIMCommonHeaderItemHolder itemHolder = new LCIMCommonHeaderItemHolder(parent.getContext(), parent);
      itemHolder.setView(headerView);
      return itemHolder;
    }

    if (FOOTER_ITEM_TYPE == viewType) {
      LCIMCommonFooterItemHolder itemHolder = new LCIMCommonFooterItemHolder(parent.getContext(), parent);
      itemHolder.setView(footerView);
      return itemHolder;
    }

    return super.onCreateViewHolder(parent, viewType);
  }
}
