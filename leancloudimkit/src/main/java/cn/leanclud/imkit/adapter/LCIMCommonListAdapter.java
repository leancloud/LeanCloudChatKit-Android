package cn.leanclud.imkit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.leanclud.imkit.viewholder.LCIMCommonViewHolder;

/**
 * Created by wli on 15/11/23.
 * 现在还仅仅支持单类型 item，多类型 item 稍后在重构
 */
public class LCIMCommonListAdapter<T> extends RecyclerView.Adapter<LCIMCommonViewHolder> {

  private static HashMap<String, LCIMCommonViewHolder.ViewHolderCreator> creatorHashMap = new HashMap<>();

  private Class<?> vhClass;

  protected List<T> dataList = new ArrayList<T>();

  public LCIMCommonListAdapter() {}

  public LCIMCommonListAdapter(Class<?> vhClass) {
    this.vhClass = vhClass;
  }

  public List<T> getDataList() {
    return dataList;
  }

  public void setDataList(List<T> datas) {
    dataList.clear();
    if (null != datas) {
      dataList.addAll(datas);
    }
  }

  /**
   * 默认在最后插入
   * @param datas
   */
  public void addDataList(List<T> datas) {
    dataList.addAll(datas);
  }

  @Override
  public LCIMCommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (null == vhClass) {
      try {
        throw new IllegalArgumentException("please use CommonListAdapter(Class<VH> vhClass)");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    LCIMCommonViewHolder.ViewHolderCreator<?> creator = null;
    if (creatorHashMap.containsKey(vhClass.getName())) {
      creator = creatorHashMap.get(vhClass.getName());
    } else {
      try {
        creator = (LCIMCommonViewHolder.ViewHolderCreator)vhClass.getField("HOLDER_CREATOR").get(null);
        creatorHashMap.put(vhClass.getName(), creator);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
    if (null != creator) {
      return creator.createByViewGroupAndType(parent, viewType);
    } else {
      throw new IllegalArgumentException(vhClass.getName() + " HOLDER_CREATOR should be instantiated");
    }
  }

  @Override
  public void onBindViewHolder(LCIMCommonViewHolder holder, int position) {
    if (position >= 0 && position < dataList.size()) {
      holder.bindData(dataList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }
}