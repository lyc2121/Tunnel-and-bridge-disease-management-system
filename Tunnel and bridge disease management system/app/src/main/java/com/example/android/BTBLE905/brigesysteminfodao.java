package com.example.android.BTBLE905;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class brigesysteminfodao {


    private  MyDataBaseHelper mBrigesystem;

    public brigesysteminfodao(Context context, int version) {
        mBrigesystem = new MyDataBaseHelper(context, "brigesystem_new", null, version);
    }
    public void add(String name, String baseinformation ,double loction_X,double loction_Y,String report,String result){
        SQLiteDatabase db = mBrigesystem.getWritableDatabase();
        db.execSQL("insert into brigesystem_new(name,baseinformation,loction_X,loction_Y,report,result)values(?,?,?,?,?,?);",new Object[]{name,baseinformation,loction_X,loction_Y,report,result});
        db.close();
    }
    public void delete(String name){
        SQLiteDatabase db = mBrigesystem.getWritableDatabase();
        db.execSQL("delete from brigesystem_new where name=?;",new Object[]{name});
        db.close();
    }
    public ArrayList query (String name) {
        SQLiteDatabase db = mBrigesystem.getReadableDatabase();
        ArrayList<Object> arr = new ArrayList<>();
        Cursor cursor = db.rawQuery("select name,baseinformation,loction_X,loction_Y,report,result from brigesystem_new where name=?;", new String[]{name});
        if (cursor.moveToNext()) {
            for (int i = 0; i < 6; i++) {
                if (i == 2 || i == 3) {
                    arr.add(cursor.getDouble(i));

                } else {
                    arr.add(cursor.getString(i));
                }
            }
        }
        db.close();
        return arr;
    }
    public ArrayList<String> query(){
        SQLiteDatabase db = mBrigesystem.getReadableDatabase();
        ArrayList<String> arr = new ArrayList<>();
        Cursor cursor = db.rawQuery("select name from brigesystem_new;",null);
        while (cursor.moveToNext()){
            arr.add(cursor.getString(0));
        }
        db.close();
        return arr;
    }
    public void update(String name,String result){
        SQLiteDatabase db = mBrigesystem.getWritableDatabase();
        db.execSQL("update brigesystem_new set result=? where name=?",new Object[]{result,name});
        db.close();
    }

}
