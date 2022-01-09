package com.example.android.BTBLE905.bmobdata;

import cn.bmob.v3.BmobObject;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class brige_acount extends BmobObject {
    private String name;
    private Integer acount;

    public Integer getAcount() {
        return acount;
    }

    public void setAcount(Integer acount) {
        this.acount = acount;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
