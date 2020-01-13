package cn.leancloud.chatkit.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cn.leancloud.chatkit.LCFacetimeInvitation;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.activity.VideoChatViewActivity;

public class LCIMChatItemFacetimeHolder extends LCIMChatItemHolder {

  private TextView eventView;
  private Button responseButton;

  public LCIMChatItemFacetimeHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    contentLayout.addView(View.inflate(getContext(), R.layout.lcim_chat_item_facetime_layout, null));
    eventView = itemView.findViewById(R.id.chat_item_invitation_event);
    responseButton = itemView.findViewById(R.id.chat_item_invitation_response);
    if (isLeft) {
      responseButton.setVisibility(View.VISIBLE);
    } else {
      responseButton.setVisibility(View.GONE);
    }
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    if (o instanceof LCFacetimeInvitation) {
      final LCFacetimeInvitation invitation = (LCFacetimeInvitation) o;
      boolean invitationStillValid = false;
      if (invitation.getStatus() == LCFacetimeInvitation.Status_Open) {
        eventView.setText(invitation.getFrom() + " invite a face-time chat.");
        long sentTime = invitation.getTimestamp();
        invitationStillValid = (System.currentTimeMillis() - sentTime) < 120000;
      } else {
        eventView.setText(invitation.getFrom() + " left face-time chat.");
      }

      if (isLeft && invitationStillValid) {
        responseButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(getContext(), VideoChatViewActivity.class);
            intent.putExtra("channel", invitation.getConversationId());
            getContext().startActivity(intent);
          }
        });
      } else {
        responseButton.setVisibility(View.GONE);
      }
    }
  }
}
