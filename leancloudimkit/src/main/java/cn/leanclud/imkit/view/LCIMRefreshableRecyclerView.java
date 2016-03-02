package cn.leanclud.imkit.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

import cn.leanclud.imkit.adapter.LCIMHeaderListAdapter;

/**
 * Created by wli on 15/12/7.
 * 支持下拉刷新以及上滑加载更多的 RecyclerView
 * <p/>
 * 下拉刷新需要配合 SwipeRefreshLayout 使用，需要在初始化 RefreshableRecyclerView后
 * 调用 setRelationSwipeLayout 来设置关联
 * <p/>
 * 因为下拉加载需要有 footer，所以需要配合 HeaderListAdapter 使用
 */
public class LCIMRefreshableRecyclerView extends RecyclerView {
  private final int DEFAULT_PAGE_NUM = 5;
  public static int STATUS_NORMAL = 0;
  public static int STATUS_LAOD_MORE = 2;

  public final double VISIBLE_SCALE = 0.75;

  private int pageNum = DEFAULT_PAGE_NUM;
  private int loadStatus = STATUS_NORMAL;
  public boolean enableLoadMore = true;

  private SwipeRefreshLayout swipeRefreshLayout;
  private LCIMLoadMoreFooterView loadMoreFooterView;
  private OnLoadDataListener onLoadDataListener;

  public LCIMRefreshableRecyclerView(Context context) {
    super(context);
    initView();
  }

  public LCIMRefreshableRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public LCIMRefreshableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView();
  }

  /**
   * 设置关联的 SwipeRefreshLayout， 下拉刷新时使用
   *
   * @param relationSwipeLayout
   */
  public void setRelationSwipeLayout(SwipeRefreshLayout relationSwipeLayout) {
    swipeRefreshLayout = relationSwipeLayout;
    if (null != swipeRefreshLayout) {
      swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          startRefresh();
        }
      });
    } else {
      throw new IllegalArgumentException("SwipeRefreshLayout can not be null");
    }
  }

  /**
   * RefreshableRecyclerView 需要配合 HeaderListAdapter 使用
   *
   * @param adapter
   */
  @Override
  public void setAdapter(Adapter adapter) {
    super.setAdapter(adapter);
    if (null != adapter) {
      if (adapter instanceof LCIMHeaderListAdapter) {
        ((LCIMHeaderListAdapter) adapter).setFooterView(loadMoreFooterView);
      } else {
        throw new IllegalArgumentException("adapter should be HeaderListAdapter");
      }
    } else {
      throw new IllegalArgumentException("adapter can not be null");
    }
  }

  @Override
  public LCIMHeaderListAdapter getAdapter() {
    return (LCIMHeaderListAdapter) super.getAdapter();
  }

  /**
   * 设置加载页的大小，默认为 DEFAULT_PAGE_NUM
   *
   * @param pageNum
   */
  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public void refreshData() {
    startRefresh();
  }

  public void setOnLoadDataListener(OnLoadDataListener loadDataListener) {
    onLoadDataListener = loadDataListener;
  }

  private void initView() {
    loadMoreFooterView = new LCIMLoadMoreFooterView(getContext());
    loadMoreFooterView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (enableLoadMore && STATUS_LAOD_MORE != getLoadStatus()) {
          startLoad();
        }
      }
    });
    addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (enableLoadMore && STATUS_LAOD_MORE != getLoadStatus()) {
          LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
          int totalItemCount = layoutManager.getItemCount();
          int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
          if (lastVisibleItem == totalItemCount - 1) {
            View view = layoutManager.findViewByPosition(lastVisibleItem);
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            if (rect.height() / view.getHeight() > VISIBLE_SCALE) {
              startLoad();
            }
          }
        }
      }
    });
  }

  private void startRefresh() {
    if (null != onLoadDataListener) {
      onLoadDataListener.onLoad(0, pageNum, true);
    }
  }

  private void startLoad() {
    if (STATUS_LAOD_MORE != getLoadStatus()) {
      LCIMHeaderListAdapter adapter = getAdapter();
      if (null != onLoadDataListener && null != adapter) {
        setLoadStatus(STATUS_LAOD_MORE);
        onLoadDataListener.onLoad(adapter.getDataList().size(), pageNum, false);
      } else {
        setLoadStatus(STATUS_NORMAL);
      }
    }
  }

  /**
   * 设置是否可用上滑加载
   *
   * @param enable
   */
  public void setEnableLoadMore(boolean enable) {
    enableLoadMore = enable;
  }

  private void setLoadStatus(int status) {
    loadStatus = status;
    loadMoreFooterView.onLoadStatusChanged(status);
  }

  /**
   * 设置刷新完毕
   */
  public void setLoadComplete() {
    setLoadStatus(STATUS_NORMAL);
    if (null != swipeRefreshLayout) {
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  public int getLoadStatus() {
    return loadStatus;
  }

  /**
   * 设置刷新完毕，如果 isRefresh 为 true，则清空所有数据，设置为 datas
   * 如果 isReresh 为 false，则把 datas 叠加到现有数据中
   *
   * @param datas
   * @param isRefresh
   */
  public void setLoadComplete(Object[] datas, boolean isRefresh) {
    setLoadStatus(STATUS_NORMAL);
    LCIMHeaderListAdapter adapter = getAdapter();
    if (null != adapter) {
      if (isRefresh) {
        adapter.setDataList(Arrays.asList(datas));
        adapter.notifyDataSetChanged();
        if (null != swipeRefreshLayout) {
          swipeRefreshLayout.setRefreshing(false);
        }
      } else {
        adapter.addDataList(Arrays.asList(datas));
        adapter.notifyDataSetChanged();
      }
    }
  }


  public interface OnLoadDataListener {
    public void onLoad(int skip, int limit, boolean isRefresh);
  }
}
