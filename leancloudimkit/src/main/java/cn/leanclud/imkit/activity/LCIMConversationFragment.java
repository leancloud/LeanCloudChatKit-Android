package cn.leanclud.imkit.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.leanclud.imkit.R;
import cn.leanclud.imkit.adapter.LCIMMultipleItemAdapter;
import cn.leanclud.imkit.event.LCIMIMTypeMessageEvent;
import cn.leanclud.imkit.event.LCIMIMTypeMessageResendEvent;
import cn.leanclud.imkit.event.LCIMInputBottomBarEvent;
import cn.leanclud.imkit.event.LCIMInputBottomBarRecordEvent;
import cn.leanclud.imkit.event.LCIMInputBottomBarTextEvent;
import cn.leanclud.imkit.utils.LCIMNotificationUtils;
import cn.leanclud.imkit.utils.LCIMPathUtils;
import cn.leanclud.imkit.view.LCIMInputBottomBar;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/8/27.
 * 将聊天相关的封装到此 Fragment 里边，只需要通过 setConversation 传入 Conversation 即可
 */
public class LCIMConversationFragment extends Fragment {

  static final int REQUEST_IMAGE_CAPTURE = 1;
  static final int REQUEST_IMAGE_PICK = 2;

  protected AVIMConversation imConversation;
  protected LCIMMultipleItemAdapter itemAdapter;
  protected RecyclerView recyclerView;
  protected LinearLayoutManager layoutManager;
  protected SwipeRefreshLayout refreshLayout;
  protected LCIMInputBottomBar inputBottomBar;
  protected String localCameraPath;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.lcim_conversation_fragment, container, false);

    recyclerView = (RecyclerView) view.findViewById(R.id.fragment_chat_rv_chat);
    refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_chat_srl_pullrefresh);
    refreshLayout.setEnabled(false);
    inputBottomBar = (LCIMInputBottomBar) view.findViewById(R.id.fragment_chat_inputbottombar);
    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    itemAdapter = new LCIMMultipleItemAdapter();
    itemAdapter.resetRecycledViewPoolSize(recyclerView);
    recyclerView.setAdapter(itemAdapter);

    EventBus.getDefault().register(this);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        AVIMMessage message = itemAdapter.getFirstMessage();
        if (null == message) {
          refreshLayout.setRefreshing(false);
        } else {
          imConversation.queryMessages(message.getMessageId(), message.getTimestamp(), 20, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
              refreshLayout.setRefreshing(false);
              if (filterException(e)) {
                if (null != list && list.size() > 0) {
                  itemAdapter.addMessageList(list);
                  itemAdapter.notifyDataSetChanged();
                  layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                }
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    if (null != imConversation) {
      LCIMNotificationUtils.addTag(imConversation.getConversationId());
    }
  }

  @Override
  public void onPause() {
    super.onResume();
    if (null != imConversation) {
      LCIMNotificationUtils.removeTag(imConversation.getConversationId());
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
  }

  public void setConversation(final AVIMConversation conversation) {
    imConversation = conversation;
    refreshLayout.setEnabled(true);
    inputBottomBar.setTag(imConversation.getConversationId());
    fetchMessages();
    LCIMNotificationUtils.addTag(conversation.getConversationId());
    if (!conversation.isTransient()) {
      if (conversation.getMembers().size() == 0) {
        conversation.fetchInfoInBackground(new AVIMConversationCallback() {
          @Override
          public void done(AVIMException e) {
            itemAdapter.showUserName(conversation.getMembers().size() > 2);
          }
        });
      } else {
        itemAdapter.showUserName(conversation.getMembers().size() > 2);
      }
    } else {
      itemAdapter.showUserName(true);
    }

  }

  /**
   * 拉取消息，必须加入 conversation 后才能拉取消息
   */
  private void fetchMessages() {
    imConversation.queryMessages(new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> list, AVIMException e) {
        if (filterException(e)) {
          itemAdapter.setMessageList(list);
          recyclerView.setAdapter(itemAdapter);
          itemAdapter.notifyDataSetChanged();
          scrollToBottom();
        }
      }
    });
  }

  /**
   * 输入事件处理，接收后构造成 AVIMTextMessage 然后发送
   * 因为不排除某些特殊情况会受到其他页面过来的无效消息，所以此处加了 tag 判断
   */
  public void onEvent(LCIMInputBottomBarTextEvent textEvent) {
    if (null != imConversation && null != textEvent) {
      if (!TextUtils.isEmpty(textEvent.sendContent) && imConversation.getConversationId().equals(textEvent.tag)) {
        sendText(textEvent.sendContent);
      }
    }
  }

  /**
   * 处理推送过来的消息
   * 同理，避免无效消息，此处加了 conversation id 判断
   */
  public void onEvent(LCIMIMTypeMessageEvent event) {
    if (null != imConversation && null != event &&
      imConversation.getConversationId().equals(event.conversation.getConversationId())) {
      itemAdapter.addMessage(event.message);
      itemAdapter.notifyDataSetChanged();
      scrollToBottom();
    }
  }

  /**
   * 重新发送已经发送失败的消息
   */
  public void onEvent(LCIMIMTypeMessageResendEvent event) {
    if (null != imConversation && null != event &&
      null != event.message &&  imConversation.getConversationId().equals(event.message.getConversationId())) {
      if (AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed == event.message.getMessageStatus()
        && imConversation.getConversationId().equals(event.message.getConversationId())) {
        imConversation.sendMessage(event.message, new AVIMConversationCallback() {
          @Override
          public void done(AVIMException e) {
            itemAdapter.notifyDataSetChanged();
          }
        });
        itemAdapter.notifyDataSetChanged();
      }
    }
  }

//  public void onEvent(MessageEvent messageEvent) {
//    final AVIMTypedMessage message = messageEvent.getMessage();
//    if (message.getConversationId().equals(conversation
//      .getConversationId())) {
//      if (messageEvent.getType() == MessageEvent.Type.Come) {
//        new CacheMessagesTask(this, Arrays.asList(message)) {
//          @Override
//          void onPostRun(List<AVIMTypedMessage> messages, Exception e) {
//            if (filterException(e)) {
//              addMessageAndScroll(message);
//            }
//          }
//        }.execute();
//      } else if (messageEvent.getType() == MessageEvent.Type.Receipt) {
//        //Utils.i("receipt");
//        AVIMTypedMessage originMessage = findMessage(message.getMessageId());
//        if (originMessage != null) {
//          originMessage.setMessageStatus(message.getMessageStatus());
//          originMessage.setReceiptTimestamp(message.getReceiptTimestamp());
//          adapter.notifyDataSetChanged();
//        }
//      }
//    }
//  }

  public void onEvent(LCIMInputBottomBarEvent event) {
    if (null != imConversation && null != event && imConversation.getConversationId().equals(event.tag)) {
      switch (event.eventAction) {
        case LCIMInputBottomBarEvent.INPUTBOTTOMBAR_IMAGE_ACTION:
          dispatchPickPictureIntent();
          break;
        case LCIMInputBottomBarEvent.INPUTBOTTOMBAR_CAMERA_ACTION:
          dispatchTakePictureIntent();
          break;
      }
    }
  }

  public void onEvent(LCIMInputBottomBarRecordEvent recordEvent) {
    if (null != imConversation && null != recordEvent &&
      !TextUtils.isEmpty(recordEvent.audioPath) &&
      imConversation.getConversationId().equals(recordEvent.tag)) {
      sendAudio(recordEvent.audioPath);
    }
  }

  private void dispatchTakePictureIntent() {
    localCameraPath = LCIMPathUtils.getPicturePathByCurrentTime(getContext());
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Uri imageUri = Uri.fromFile(new File(localCameraPath));
    takePictureIntent.putExtra("return-data", false);
    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
  }

  private void dispatchPickPictureIntent() {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, null);
    photoPickerIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    startActivityForResult(photoPickerIntent, REQUEST_IMAGE_PICK);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (Activity.RESULT_OK == resultCode) {
      switch (requestCode) {
        case REQUEST_IMAGE_CAPTURE:
          sendImage(localCameraPath);
          break;
        case REQUEST_IMAGE_PICK:
          sendImage(getRealPathFromURI(getActivity(), data.getData()));
          break;
      }
    }
  }

  private void scrollToBottom() {
    layoutManager.scrollToPositionWithOffset(itemAdapter.getItemCount() - 1, 0);
  }

  private String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = { MediaStore.Images.Media.DATA };
      cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private void sendText(String content) {
    AVIMTextMessage message = new AVIMTextMessage();
    message.setText(content);
    sendMessage(message);
  }

  /**
   * TODO 上传的图片最好要压缩一下
   * @param imagePath
   */
  private void sendImage(String imagePath) {
    try {
      sendMessage(new AVIMImageMessage(imagePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendAudio(String audioPath) {
    try {
      AVIMAudioMessage audioMessage = new AVIMAudioMessage(audioPath);
      sendMessage(audioMessage);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendMessage(AVIMTypedMessage message) {
    itemAdapter.addMessage(message);
    itemAdapter.notifyDataSetChanged();
    scrollToBottom();
    imConversation.sendMessage(message, new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        itemAdapter.notifyDataSetChanged();
      }
    });
  }

  private boolean filterException(Exception e) {
    return true;
  }
}
