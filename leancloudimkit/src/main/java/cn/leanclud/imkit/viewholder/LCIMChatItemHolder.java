package cn.leanclud.imkit.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.leanclud.imkit.LCIMUserProfile;
import cn.leanclud.imkit.R;
import cn.leanclud.imkit.cache.ProfileCache;
import cn.leanclud.imkit.event.LCIMMessageResendEvent;
import cn.leanclud.imkit.utils.LCIMConstants;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/9/17.
 */
public class LCIMChatItemHolder extends LCIMCommonViewHolder {

  protected boolean isLeft;

  protected AVIMMessage message;
  protected ImageView avatarView;
  protected TextView timeView;
  protected TextView nameView;
  protected LinearLayout conventLayout;
  protected FrameLayout statusLayout;
  protected ProgressBar progressBar;
  protected TextView statusView;
  protected ImageView errorView;

  public LCIMChatItemHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft ? R.layout.lcim_chat_item_left_layout : R.layout.lcim_chat_item_right_layout);
    this.isLeft = isLeft;
    initView();
  }

  public void initView() {
    if (isLeft) {
      avatarView = (ImageView) itemView.findViewById(R.id.chat_left_iv_avatar);
      timeView = (TextView) itemView.findViewById(R.id.chat_left_tv_time);
      nameView = (TextView) itemView.findViewById(R.id.chat_left_tv_name);
      conventLayout = (LinearLayout) itemView.findViewById(R.id.chat_left_layout_content);
      statusLayout = (FrameLayout) itemView.findViewById(R.id.chat_left_layout_status);
      statusView = (TextView) itemView.findViewById(R.id.chat_left_tv_status);
      progressBar = (ProgressBar) itemView.findViewById(R.id.chat_left_progressbar);
      errorView = (ImageView) itemView.findViewById(R.id.chat_left_tv_error);
    } else {
      avatarView = (ImageView) itemView.findViewById(R.id.chat_right_iv_avatar);
      timeView = (TextView) itemView.findViewById(R.id.chat_right_tv_time);
      nameView = (TextView) itemView.findViewById(R.id.chat_right_tv_name);
      conventLayout = (LinearLayout) itemView.findViewById(R.id.chat_right_layout_content);
      statusLayout = (FrameLayout) itemView.findViewById(R.id.chat_right_layout_status);
      progressBar = (ProgressBar) itemView.findViewById(R.id.chat_right_progressbar);
      errorView = (ImageView) itemView.findViewById(R.id.chat_right_tv_error);
      statusView = (TextView) itemView.findViewById(R.id.chat_right_tv_status);
    }

    setAvatarClickEvent();
    setResendClickEvent();
  }

  @Override
  public void bindData(Object o) {
    message = (AVIMMessage) o;
    timeView.setText(millisecsToDateString(message.getTimestamp()));

    ProfileCache.getInstance().getCachedUser(message.getFrom(), new AVCallback<LCIMUserProfile>() {
      @Override
      protected void internalDone0(LCIMUserProfile userProfile, AVException e) {
        nameView.setText(userProfile.getUserName());
        Picasso.with(getContext()).load(userProfile.getAvatarUrl()).into(avatarView);
      }
    });

    switch (message.getMessageStatus()) {
      case AVIMMessageStatusFailed:
        statusLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        break;
      case AVIMMessageStatusSent:
        statusLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        statusView.setVisibility(View.VISIBLE);
        statusView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        break;
      case AVIMMessageStatusSending:
        statusLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        statusView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        break;
      case AVIMMessageStatusNone:
      case AVIMMessageStatusReceipt:
        statusLayout.setVisibility(View.GONE);
        break;
    }
  }

  public void showTimeView(boolean isShow) {
    timeView.setVisibility(isShow ? View.VISIBLE : View.GONE);
  }

  public void showUserName(boolean isShow) {
    nameView.setVisibility(isShow ? View.VISIBLE : View.GONE);
  }

  /**
   * 设置头像点击按钮的事件
   */
  private void setAvatarClickEvent() {
    avatarView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(LCIMConstants.AVATAR_CLICK_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        getContext().startActivity(intent);
      }
    });
  }

  /**
   * 设置发送失败的叹号按钮的事件
   */
  private void setResendClickEvent() {
    errorView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LCIMMessageResendEvent event = new LCIMMessageResendEvent();
        event.message = message;
        EventBus.getDefault().post(event);
      }
    });
  }

  //TODO 展示更人性一点
  private static String millisecsToDateString(long timestamp) {
    long gap = System.currentTimeMillis() - timestamp;

    SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
    return format.format(new Date(timestamp));
  }
}

