package cn.leanclud.imkit.cache;


import android.os.AsyncTask;
import android.text.TextUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wli on 15/9/29.
 */
public class LocalCacheUtils {

  /**
   * 用于记录 DownLoadCallback，如果对于同一个 url 有多个请求，则下载完后应该执行所有回调
   * 此变量就是用于记录这些请求
   */
  private static Map<String, ArrayList<DownLoadCallback>> downloadCallBackMap;

  /**
   * 判断当前 url 是否正在下载，如果已经在下载，则没有必要再去做请求
   */
  private static Set<String> isDownloadingFile;

  private static DefaultHttpClient httpClient;

  static {
    downloadCallBackMap = new HashMap<String, ArrayList<DownLoadCallback>>();
    isDownloadingFile = new HashSet<String>();
  }

  private static void addDownloadCallback(String path, DownLoadCallback callback) {
    if (null != callback) {
      ArrayList<DownLoadCallback> callbacks;
      if (downloadCallBackMap.containsKey(path)) {
        callbacks = downloadCallBackMap.get(path);
      } else {
        callbacks = new ArrayList<DownLoadCallback>();
      }
      callbacks.add(callback);
    }
  }

  private static void executeDownloadCallBack(String path, Exception e) {
    if (downloadCallBackMap.containsKey(path)) {
      ArrayList<DownLoadCallback> callbacks = downloadCallBackMap.get(path);
      for (DownLoadCallback callback : callbacks) {
        callback.done(e);
      }
    }
  }

  private synchronized static DefaultHttpClient getDefaultHttpClient() {
    if (httpClient == null) {
      httpClient = new DefaultHttpClient();
    }
    return httpClient;
  }

  public static void downloadFileAsync(final String url, final String localPath) {
    downloadFileAsync(url, localPath, false);
  }

  public static void downloadFileAsync(final String url, final String localPath, boolean overlay) {
    downloadFile(url, localPath, overlay, null);
  }

  public static void downloadFile(final String url, final String localPath,
    boolean overlay, final DownLoadCallback callback) {
    if (TextUtils.isEmpty(url) || TextUtils.isEmpty(localPath)) {
      throw new IllegalArgumentException("url or localPath can not be null");
    } else if (!overlay && isFileExist(localPath)) {
      if (null != callback) {
        callback.done(null);
      }
    } else {
      addDownloadCallback(url, callback);
      if (!isDownloadingFile.contains(url)) {
        new AsyncTask<Void, Void, Exception>() {
          @Override
          protected Exception doInBackground(Void... params) {
            return downloadFile(url, localPath);
          }

          @Override
          protected void onPostExecute(Exception e) {
            executeDownloadCallBack(url, e);
            isDownloadingFile.remove(url);
          }
        }.execute();
      }
    }
  }

  private static Exception downloadFile(String url, String localPath) {
    File file = new File(localPath);
    Exception result = null;
    FileOutputStream outputStream = null;
    InputStream inputStream = null;
    try {
      outputStream = new FileOutputStream(file);
      HttpGet get = new HttpGet(url);
      HttpResponse response = getDefaultHttpClient().execute(get);
      HttpEntity entity = response.getEntity();
      inputStream = entity.getContent();
      byte[] buffer = new byte[4096];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, len);
      }
    } catch (Exception e) {
      result = e;
      if (file.exists()) {
        file.delete();
      }
    } finally {
      closeQuietly(inputStream);
      closeQuietly(outputStream);
    }
    return result;
  }

  private static void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (Exception e) {
    }
  }

  private static boolean isFileExist(String localPath) {
    File file = new File(localPath);
    return file.exists();
  }


  public static class DownLoadCallback {
    public void done(Exception e) {
    }
  }
}
