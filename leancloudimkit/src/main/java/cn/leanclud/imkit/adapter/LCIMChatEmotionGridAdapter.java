package cn.leanclud.imkit.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.leanclud.imkit.R;
import cn.leanclud.imkit.utils.EmotionHelper;


/**
 * Created by lzw on 14-9-25.
 * TODO 替换为 recyclerview
 */
public class LCIMChatEmotionGridAdapter extends BaseAdapter {
  private Context context;
  private List<String> datas = new ArrayList<>();

  public LCIMChatEmotionGridAdapter(Context ctx) {
    this.context = ctx;
  }

  public void setDatas(List<String> datas) {
    this.datas = datas;
  }

  @Override
  public int getCount() {
    return datas.size();
  }

  @Override
  public Object getItem(int i) {
    return datas.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      conView = View.inflate(context, R.layout.lcim_chat_emotion_item, null);
    }
    ImageView emotionImageView = findViewById(conView, R.id.emotionImageView);
    String emotion = (String) getItem(position);
    emotion = emotion.substring(1, emotion.length() - 1);
    Bitmap bitmap = EmotionHelper.getEmojiDrawable(context, emotion);
    emotionImageView.setImageBitmap(bitmap);
    return conView;
  }

  public static <T extends View> T findViewById(View view, int id) {
    SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
    if (viewHolder == null) {
      viewHolder = new SparseArray<>();
      view.setTag(viewHolder);
    }
    View childView = viewHolder.get(id);
    if (childView == null) {
      childView = view.findViewById(id);
      viewHolder.put(id, childView);
    }
    return (T) childView;
  }
}
