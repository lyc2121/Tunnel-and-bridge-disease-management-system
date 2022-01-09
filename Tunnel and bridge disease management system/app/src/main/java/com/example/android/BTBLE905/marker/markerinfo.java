package com.example.android.BTBLE905.marker;

import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.Serializable;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class markerinfo implements Serializable{
    private LatLng mLatLng;
    private File mFile;
    private String mcontent;

    public String getMcontent() {
        return mcontent;
    }

    public void setMcontent(String mcontent) {
        this.mcontent = mcontent;
    }

    public markerinfo(LatLng latLng, File file, String content) {
        mLatLng = latLng;
        mFile = file;
        mcontent=content;


    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }
}
