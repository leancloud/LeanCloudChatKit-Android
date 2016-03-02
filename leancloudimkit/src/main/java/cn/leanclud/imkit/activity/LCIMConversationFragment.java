package cn.leanclud.imkit.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

  private static final int TAKE_CAMERA_REQUEST = 2;
  private static final int GALLERY_REQUEST = 0;
  private static final int GALLERY_KITKAT_REQUEST = 3;

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

    localCameraPath = LCIMPathUtils.getPicturePathByCurrentTime(getContext());

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

//TODO
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
          selectImageFromLocal();
          break;
        case LCIMInputBottomBarEvent.INPUTBOTTOMBAR_CAMERA_ACTION:
          selectImageFromCamera();
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

  public void selectImageFromLocal() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.lcim_chat_activity_select_picture)),
        GALLERY_REQUEST);
    } else {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("image/*");
      startActivityForResult(intent, GALLERY_KITKAT_REQUEST);
    }
  }

  public void selectImageFromCamera() {
    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    Uri imageUri = Uri.fromFile(new File(localCameraPath));
    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
      startActivityForResult(takePictureIntent, TAKE_CAMERA_REQUEST);
    }
  }

  private void scrollToBottom() {
    layoutManager.scrollToPositionWithOffset(itemAdapter.getItemCount() - 1, 0);
  }

  protected boolean filterException(Exception e) {
    if (e != null) {
      e.printStackTrace();
      toast(e.getMessage());
      return false;
    } else {
      return true;
    }
  }

  protected void toast(String str) {
    Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      switch (requestCode) {
        case GALLERY_REQUEST:
        case GALLERY_KITKAT_REQUEST:
          if (data == null) {
            toast("return intent is null");
            return;
          }
          Uri uri;
          if (requestCode == GALLERY_REQUEST) {
            uri = data.getData();
          } else {
            //for Android 4.4
            uri = data.getData();
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
              | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
          }

          //TODO
//          String localSelectPath = ProviderPathUtils.getPath(getActivity(), uri);
//          inputBottomBar.hideMoreLayout();
//          sendImage(localSelectPath);
          break;
        case TAKE_CAMERA_REQUEST:
          inputBottomBar.hideMoreLayout();
          sendImage(localCameraPath);
          break;
      }
    }
  }

  private void sendText(String content) {
    AVIMTextMessage message = new AVIMTextMessage();
    message.setText(content);
    sendMessage(message);
  }

  private void sendImage(String imagePath) {
    AVIMImageMessage imageMsg = null;
    try {
      imageMsg = new AVIMImageMessage(imagePath);
      sendMessage(imageMsg);
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
}
