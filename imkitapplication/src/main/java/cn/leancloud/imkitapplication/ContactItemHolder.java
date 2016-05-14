package cn.leancloud.imkitapplication;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import cn.leancloud.chatkit.LCIMKitUser;
import cn.leancloud.chatkit.activity.LCIMConversationActivity;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.viewholder.LCIMCommonViewHolder;

/**
 * Created by wli on 15/11/24.
 */
public class ContactItemHolder extends LCIMCommonViewHolder<LCIMKitUser> {

  TextView nameView;
  ImageView avatarView;

  public LCIMKitUser lcimKitUser;

  public ContactItemHolder(Context context, ViewGroup root) {
    super(context, root, R.layout.common_user_item);
    initView();
  }

  public void initView() {
    nameView = (TextView)itemView.findViewById(R.id.tv_friend_name);
    avatarView = (ImageView)itemView.findViewById(R.id.img_friend_avatar);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getContext(), LCIMConversationActivity.class);
        intent.putExtra(LCIMConstants.PEER_ID, lcimKitUser.getUserId());
        getContext().startActivity(intent);
      }
    });
  }

  @Override
  public void bindData(LCIMKitUser lcimKitUser) {
    this.lcimKitUser = lcimKitUser;
    Picasso.with(getContext()).load(lcimKitUser.getAvatarUrl()).into(avatarView);
    nameView.setText(lcimKitUser.getUserName());
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<ContactItemHolder>() {
    @Override
    public ContactItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new ContactItemHolder(parent.getContext(), parent);
    }
  };
}
