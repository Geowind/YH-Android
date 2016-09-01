package com.intfocus.yh_android.util;

import android.util.Log;

/**
 * Created by lijunjie on 16/7/22.
 */
public class LogUtil {

  /*
   * Log.d(tag, str, limit)
   */
  public static void d(String tag, String str, int limit) {
    int maxLength = 2000;
    str = str.trim();
    Log.d(tag, str.substring(0, str.length() > maxLength ? maxLength : str.length()));
    if(str.length() > maxLength && limit < 4) {
      str = str.substring(maxLength, str.length());
<<<<<<< HEAD
      LogUtil.d(tag, str, limit);
=======
//      LogUtil.d(tag, str, limit);
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
    }
  }

  /*
   * Log.d(tag, str)
   */
  public static void d(String tag, String str) {
<<<<<<< HEAD
     LogUtil.d(tag, str, 0);
=======
//     LogUtil.d(tag, str, 0);
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
  }
}
