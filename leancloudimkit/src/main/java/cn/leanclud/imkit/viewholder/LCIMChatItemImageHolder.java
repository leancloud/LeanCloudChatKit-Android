package cn.leanclud.imkit.viewholder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.squareup.picasso.Picasso;

import java.io.File;

import cn.leanclud.imkit.activity.LCIMImageActivity;
import cn.leanclud.imkit.R;
import cn.leanclud.imkit.utils.Constants;
import cn.leanclud.imkit.utils.PathUtils;

/**
 * Created by wli on 15/9/17.
 */
public class LCIMChatItemImageHolder extends LCIMChatItemHolder {

  protected ImageView contentView;

  public LCIMChatItemImageHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    conventLayout.addView(View.inflate(getContext(), R.layout.lcim_chat_item_image_layout, null));
    contentView = (ImageView) itemView.findViewById(R.id.chat_item_image_view);
    if (isLeft) {
      contentView.setBackgroundResource(R.drawable.lcim_chat_item_left_bg);
    } else {
      contentView.setBackgroundResource(R.drawable.lcim_chat_item_right_bg);
    }

    contentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getContext(), LCIMImageActivity.class);
        intent.putExtra(Constants.IMAGE_LOCAL_PATH, PathUtils.getChatFilePath(getContext(), message.getMessageId()));
        intent.putExtra(Constants.IMAGE_URL, ((AVIMImageMessage) message).getFileUrl());
        getContext().startActivity(intent);
      }
    });
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    contentView.setImageResource(0);
    AVIMMessage message = (AVIMMessage) o;
    if (message instanceof AVIMImageMessage) {
      AVIMImageMessage imageMsg = (AVIMImageMessage) message;
      String localFilePath = imageMsg.getLocalFilePath();
      if (!TextUtils.isEmpty(localFilePath)) {
        Picasso.with(getContext().getApplicationContext()).load(new File(localFilePath)).into(contentView);
      } else {
        //TODO test
        String url = PathUtils.getChatFilePath(getContext(), imageMsg.getMessageId());
        Picasso.with(getContext().getApplicationContext()).load(url).into(contentView);
      }
    }
  }
}