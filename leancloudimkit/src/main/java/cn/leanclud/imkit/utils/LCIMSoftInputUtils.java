package cn.leanclud.imkit.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by wli on 15/7/29.
 */
public class LCIMSoftInputUtils {

  public static void toggleSoftInput(Context context) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
  }

  public static void showSoftInput(Context context, View view) {
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(view, 0);
    }
  }

  public static void hideSoftInput(Context context, View view) {
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }
}
