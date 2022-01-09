package com.example.android.BTBLE905;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import static cn.bmob.v3.Bmob.getApplicationContext;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class MyDataBaseHelper extends SQLiteOpenHelper {

     private  String creat_brigesystem="create table brigesystem_new(id integer primary key autoincrement,name nvarchar(40),baseinformation text,loction_X real,loction_Y real,report text,result text);";
    private  Context mcontext;
    public MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "brigesystem_new", null, version);
        mcontext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(creat_brigesystem);
        Toast.makeText(mcontext,"创建数据库成功", Toast.LENGTH_SHORT).show();
        final BmobQuery<person> query = new BmobQuery<>("person");
        query.addQueryKeys("name");
        query.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.person>() {
            @Override
            public void onSuccess(List<person> list) {
                for (int i = 0; i < list.size(); i++) {
                    person pnew=list.get(i);
                    final BmobQuery<person> query1 = new BmobQuery<>("person");
                    query1.addWhereEqualTo("name",pnew.getName());
                    query1.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.person>() {
                        @Override
                        public void onSuccess(List<person> list) {
                            for (int i = 0; i < list.size(); i++) {
                                person p = list.get(i);
                                brigesysteminfodao mBrigesystemdao = new brigesysteminfodao(mcontext.getApplicationContext(),1);
                                mBrigesystemdao.add(p.getName(), p.getBaseinformation(), Double.valueOf(p.getLocation_X()), Double.valueOf(p.getLocation_Y()), p.getReport(), p.getResult());
                            }
                        }
                        @Override
                        public void onError(int i, String s) {

                        }
                    });

                }
                Toast.makeText(getApplicationContext(),"数据库下载成功!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(getApplicationContext(),"数据库更新失败！", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            File version = new File(mcontext.getFilesDir(), "version.txt");
            FileOutputStream fos = new FileOutputStream(version);
            int v=1;
            fos.write(String.valueOf(v).getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("delete  from brigesystem_new");
        Toast.makeText(mcontext,"数据库更新了", Toast.LENGTH_SHORT).show();
    }
}
