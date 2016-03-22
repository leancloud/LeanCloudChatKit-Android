package cn.leanclud.imkit.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMSingleMessageQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.leanclud.imkit.R;
import cn.leanclud.imkit.cache.ConversationItemCache;
import cn.leanclud.imkit.event.LCIMConversationItemClickEvent;
import cn.leanclud.imkit.utils.LCIMConversationUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/10/8.
 * 会话 item 对应的 holder
 */
public class LCIMConversationItemHolder extends LCIMCommonViewHolder {

  ImageView avatarView;
  TextView unreadView;
  TextView messageView;
  TextView timeView;
  TextView nameView;
  RelativeLayout avatarLayout;
  LinearLayout contentLayout;

  public LCIMConversationItemHolder(ViewGroup root) {
    super(root.getContext(), root, R.layout.lcim_conversation_item);
    initView();
  }

  public void initView() {
    avatarView = (ImageView) itemView.findViewById(R.id.conversation_item_iv_avatar);
    nameView = (TextView) itemView.findViewById(R.id.conversation_item_tv_name);
    timeView = (TextView) itemView.findViewById(R.id.conversation_item_tv_time);
    unreadView = (TextView) itemView.findViewById(R.id.conversation_item_tv_unread);
    messageView = (TextView) itemView.findViewById(R.id.conversation_item_tv_message);
    avatarLayout = (RelativeLayout) itemView.findViewById(R.id.conversation_item_layout_avatar);
    contentLayout = (LinearLayout) itemView.findViewById(R.id.conversation_item_layout_content);
  }

  @Override
  public void bindData(Object o) {
    final AVIMConversation conversation = (AVIMConversation) o;
    if (null != conversation) {
      if (TextUtils.isEmpty(conversation.getCreator())) {
        conversation.fetchInfoInBackground(new AVIMConversationCallback() {
          @Override
          public void done(AVIMException e) {
            updateNameAndIcon(conversation);
          }
        });
      } else {
        updateNameAndIcon(conversation);
      }

      updateUnreadCount(conversation);
      updateLastMessage(conversation);
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          EventBus.getDefault().post(new LCIMConversationItemClickEvent(conversation.getConversationId()));
        }
      });
    }
  }

  private void updateNameAndIcon(AVIMConversation conversation) {
    LCIMConversationUtils.getConversationName(conversation, new AVCallback<String>() {
      @Override
      protected void internalDone0(String s, AVException e) {
        if (null != e) {
          e.printStackTrace();
        } else {
          nameView.setText(s);
        }
      }
    });

    LCIMConversationUtils.getConversationIcon(conversation, new AVCallback<String>() {
      @Override
      protected void internalDone0(String s, AVException e) {
        if (!TextUtils.isEmpty(s)) {
          Picasso.with(getContext()).load(s).into(avatarView);
        }
      }
    });
  }

  private void updateUnreadCount(AVIMConversation conversation) {
    int num = ConversationItemCache.getInstance().getUnreadCount(conversation.getConversationId());
    unreadView.setText(num + "");
    unreadView.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
  }

  /**
   * 更新最后一条消息
   * queryMessages
   * @param conversation
   */
  private void updateLastMessage(final AVIMConversation conversation) {
    // TODO 此处如果调用 AVIMConversation.getLastMessage 的话会造成一直读取缓存数据造成展示不对
    // 所以使用 queryMessages，但是这个接口还是很难有，需要 sdk 对这个进行支持
    conversation.getLastMessage(new AVIMSingleMessageQueryCallback() {
      @Override
      public void done(AVIMMessage avimMessage, AVIMException e) {
        if (null != avimMessage) {
          updateLastMessage(avimMessage);
        } else {
          conversation.queryMessages(1, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
              if (null != list && !list.isEmpty()) {
                updateLastMessage(list.get(0));
              }
            }
          });
        }
      }
    });
  }

  private void updateLastMessage(AVIMMessage message) {
    if (null != message) {
      Date date = new Date(message.getTimestamp());
      SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
      timeView.setText(format.format(date));
      messageView.setText(getMessageeShorthand(getContext(), message));
    } else {
      timeView.setText("");
      messageView.setText("");
    }
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<LCIMConversationItemHolder>() {
    @Override
    public LCIMConversationItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new LCIMConversationItemHolder(parent);
    }
  };

  private static CharSequence getMessageeShorthand(Context context, AVIMMessage message) {
    if (message instanceof AVIMTypedMessage) {
      AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(
        ((AVIMTypedMessage) message).getMessageType());
      switch (type) {
        case TextMessageType:
          return ((AVIMTextMessage) message).getText();
        case ImageMessageType:
          return context.getString(R.string.lcim_message_shorthand_image);
        case LocationMessageType:
          return context.getString(R.string.lcim_message_shorthand_location);
        case AudioMessageType:
          return context.getString(R.string.lcim_message_shorthand_audio);
        default:
          return context.getString(R.string.lcim_message_shorthand_unknown);
      }
    } else {
      return message.getContent();
    }
  }
}
