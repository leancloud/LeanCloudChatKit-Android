package cn.leanclud.imkit.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVUtils;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by wli on 16/2/25.
 * 1、因为忽略了具体数据格式，所以更新具体属性的时候必须更新整条记录
 */
public class LocalStorage extends SQLiteOpenHelper {

  /**
   * db 的名字，加前缀避免与用户自己的逻辑冲突
   */
  private static final String DB_NAME_PREFIX = "LeanCloudIMKit_DB";

  /**
   * 具体 id 的 key，文本、主键、不能为空
   */
  private static final String TABLE_KEY_ID = "id";

  /**
   * 具体内容的 key，文本（非文本的可以通过转化成 json 存进来）
   */
  private static final String TABLE_KEY_CONTENT = "content";

  /**
   * 具体内容的更新时间，方便排序使用
   */
  private static final String TABLE_KEY_UPDATE_TIME = "update_time";

  private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s(" +
    TABLE_KEY_ID + " TEXT PRIMARY KEY NOT NULL, " +
    TABLE_KEY_CONTENT + " TEXT, " +
    TABLE_KEY_UPDATE_TIME + " VARCHAR(32)" +
    ")";
  private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS %s";
  private static final String SQL_SELECT_BY_ID = "select %s from %s";
  private static final String SQL_GET_COUNT = "select count(%s) from %s";

  private static final int DB_VERSION = 1;

  private String tableName;

  private HandlerThread readDbThread;
  private Handler readDbHandler;
  private Handler mainThreadHandler;

  public LocalStorage(Context context, String clientId, String tableName) {
    super(context, DB_NAME_PREFIX, null, DB_VERSION);
    this.tableName = tableName + "_" + clientId;
    this.tableName = this.tableName.toLowerCase();
    if (TextUtils.isEmpty(tableName)) {
      throw new IllegalArgumentException("tableName can not be null");
    }
    if (TextUtils.isEmpty(clientId)) {
      throw new IllegalArgumentException("clientId can not be null");
    }
    createTable();

    readDbThread = new HandlerThread("");
    readDbThread.start();
    readDbHandler = new Handler(readDbThread.getLooper());
    mainThreadHandler = new Handler(context.getMainLooper());
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(String.format(SQL_CREATE_TABLE, tableName));
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (!isIgnoreUpgrade()) {
      db.execSQL(String.format(SQL_DROP_TABLE, tableName));
      onCreate(db);
    }
  }

  private void createTable() {
    getWritableDatabase().execSQL(String.format(SQL_CREATE_TABLE, tableName));
  }

  protected boolean isIgnoreUpgrade() {
    return true;
  }

  public void getIds(final AVCallback<List<String>> callback) {
    readDbHandler.post(new Runnable() {
      @Override
      public void run() {
        callback.internalDone(getIdsSync(), null);
      }
    });
  }

  public void getDatas(final List<String> ids, final AVCallback<List<String>> callback) {
    readDbHandler.post(new Runnable() {
      @Override
      public void run() {
        callback.internalDone(getDatasSync(ids), null);
      }
    });
  }

  public void insertDatas(final List<String> idList, final List<String> valueList) {
    readDbHandler.post(new Runnable() {
      @Override
      public void run() {
        insert(idList, valueList);
      }
    });
  }

  public void insertData(String id, String value) {
    insertDatas(Arrays.asList(id), Arrays.asList(value));
  }

  public void deleteDatas(final List<String> ids) {
    readDbHandler.post(new Runnable() {
      @Override
      public void run() {
        deleteSync(ids);
      }
    });
  }

  private List<String> getIdsSync() {
    String queryString = "SELECT " + TABLE_KEY_ID + " FROM " + tableName;
    SQLiteDatabase database = getReadableDatabase();
    Cursor cursor = database.rawQuery(queryString, null);
    List<String> dataList = new ArrayList<>();
    while (cursor.moveToNext()) {
      dataList.add(cursor.getString(cursor.getColumnIndex(TABLE_KEY_ID)));
    }
    return dataList;
  }

  /**
   * 查
   *
   * @param ids 若 id 为空则返回所有数据
   * @return
   */
  private List<String> getDatasSync(List<String> ids) {
    String queryString = "SELECT * FROM " + tableName;
    if (null != ids && !ids.isEmpty()) {
      queryString += (" WHERE " + TABLE_KEY_ID + " in ('" + AVUtils.joinCollection(ids, "','") + "')");
    }

    SQLiteDatabase database = getReadableDatabase();
    Cursor cursor = database.rawQuery(queryString, null);
    List<String> dataList = new ArrayList<>();
    while (cursor.moveToNext()) {
      dataList.add(cursor.getString(cursor.getColumnIndex(TABLE_KEY_CONTENT)));
    }
    return dataList;
  }

  /**
   * 增
   *
   * @param idList
   * @param valueList
   */
  private void insert(List<String> idList, List<String> valueList) {
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    for (int i = 0; i < valueList.size(); i++) {
      ContentValues values = new ContentValues();
      values.put(TABLE_KEY_ID, idList.get(i));
      values.put(TABLE_KEY_CONTENT, valueList.get(0));
      values.put(TABLE_KEY_UPDATE_TIME, System.currentTimeMillis());
      db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    db.setTransactionSuccessful();
    db.endTransaction();
  }

  /**
   * 删
   */
  private void deleteSync(List<String> ids) {
    String queryString = joinListWithApostrophe(ids);
    getWritableDatabase().delete(tableName, TABLE_KEY_ID + " in (%s)", new String[]{queryString});
  }

  private static String joinListWithApostrophe(List<String> strList) {
    String queryString = TextUtils.join("',", strList);
    if (!TextUtils.isEmpty(queryString)) {
      queryString = "'" + queryString + "'";
    }
    return queryString;
  }
}
