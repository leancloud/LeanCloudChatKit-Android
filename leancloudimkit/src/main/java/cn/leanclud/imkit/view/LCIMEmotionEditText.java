package cn.leanclud.imkit.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import cn.leanclud.imkit.utils.LCIMEmotionHelper;


public class LCIMEmotionEditText extends EditText {

  public LCIMEmotionEditText(Context context) {
    super(context);
  }

  public LCIMEmotionEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public LCIMEmotionEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    if (!TextUtils.isEmpty(text)) {
      super.setText(LCIMEmotionHelper.replace(getContext(), text.toString()), type);
    } else {
      super.setText(text, type);
    }
  }
}
