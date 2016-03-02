package cn.leanclud.imkit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMConversation;

import java.util.ArrayList;
import java.util.List;

import cn.leanclud.imkit.LCIMKit;
import cn.leanclud.imkit.R;
import cn.leanclud.imkit.adapter.LCIMCommonListAdapter;
import cn.leanclud.imkit.cache.UnreadCountCache;
import cn.leanclud.imkit.event.LCIMConversationFragmentUpdateEvent;
import cn.leanclud.imkit.event.LCIMConversationItemClickEvent;
import cn.leanclud.imkit.event.LCIMIMTypeMessageEvent;
import cn.leanclud.imkit.utils.LCIMConstants;
import cn.leanclud.imkit.view.LCIMDividerItemDecoration;
import cn.leanclud.imkit.viewholder.LCIMConversationItemHolder;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 16/2/29.
 * 会话列表页
 */
public class LCIMConversationListFragment extends Fragment {
  protected SwipeRefreshLayout refreshLayout;
  protected RecyclerView recyclerView;

  protected LCIMCommonListAdapter<AVIMConversation> itemAdapter;
  protected LinearLayoutManager layoutManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.lcim_conversation_list_fragment, container, false);

    refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.fragment_conversation_srl_pullrefresh);
    recyclerView = (RecyclerView)view.findViewById(R.id.fragment_conversation_srl_view);

    refreshLayout.setEnabled(false);
    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new LCIMDividerItemDecoration(getActivity()));
    itemAdapter = new LCIMCommonListAdapter<AVIMConversation>(LCIMConversationItemHolder.class);
    recyclerView.setAdapter(itemAdapter);
    EventBus.getDefault().register(this);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    updateConversationList();
  }

  @Override
  public void onResume() {
    super.onResume();
    updateConversationList();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
  }

  public void onEvent(LCIMIMTypeMessageEvent event) {
    updateConversationList();
  }

  private void updateConversationList() {
    List<String> convIdList = UnreadCountCache.getInstance().getConversationList();
    List<AVIMConversation> conversationList = new ArrayList<>();
    for (String convId : convIdList) {
      conversationList.add(LCIMKit.getInstance().getClient().getConversation(convId));
    }

    itemAdapter.setDataList(conversationList);
    itemAdapter.notifyDataSetChanged();

//    ChatManager.getInstance().getConversationManager().findAndCacheRooms(new Room.MultiRoomsCallback() {
//      @Override
//      public void done(List<Room> roomList, AVException exception) {
//        if (null == exception) {
//          List<Room> sortedRooms = sortRooms(roomList);
//          updateLastMessage(sortedRooms);
//          itemAdapter.setDataList(sortedRooms);
//          itemAdapter.notifyDataSetChanged();
//        }
//      }
//    });
  }

//  private void updateLastMessage(final List<String> convList) {
//    for (final String conv : convList) {
//      AVIMConversation conversation = ;
//      if (null != conversation) {
//        conversation.getLastMessage(new AVIMSingleMessageQueryCallback() {
//          @Override
//          public void done(AVIMMessage avimMessage, AVIMException e) {
//            if (null != e && null != avimMessage) {
//              room.setLastMessage(avimMessage);
//              int index = roomList.indexOf(room);
//              itemAdapter.notifyItemChanged(index);
//            }
//          }
//        });
//      }
//    }
//  }
//
//  private List<Room> sortRooms(final List<Room> roomList) {
//    List<Room> sortedList = new ArrayList<Room>();
//    if (null != roomList) {
//      sortedList.addAll(roomList);
//      Collections.sort(sortedList, new Comparator<Room>() {
//        @Override
//        public int compare(Room lhs, Room rhs) {
//          long value = lhs.getLastModifyTime() - rhs.getLastModifyTime();
//          if (value > 0) {
//            return -1;
//          } else if (value < 0) {
//            return 1;
//          } else {
//            return 0;
//          }
//        }
//      });
//    }
//    return sortedList;
//  }

  public void onEvent(LCIMConversationItemClickEvent clickEvent) {
    Intent intent = new Intent(getContext(), LCIMConversationActivity.class);
    intent.putExtra(LCIMConstants.CONVERSATION_ID, clickEvent.conversationId);
    getContext().startActivity(intent);
  }

  public void onEvent(LCIMConversationFragmentUpdateEvent updateEvent) {
    updateConversationList();
  }
//
//  @Override
//  public void onConnectionChanged(boolean connect) {
//    imClientStateView.setVisibility(connect ? View.GONE : View.VISIBLE);
//  }

//  public long getLastModifyTime() {
//    if (lastMessage != null) {
//      return lastMessage.getTimestamp();
//    }
//
//    AVIMConversation conversation = getConversation();
//    if (null != conversation && null != conversation.getUpdatedAt()) {
//      return conversation.getUpdatedAt().getTime();
//    }
//
//    return 0;
//  }
}
