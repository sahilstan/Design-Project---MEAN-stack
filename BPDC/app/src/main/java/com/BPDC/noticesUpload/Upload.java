package com.BPDC.noticesUpload;

import com.google.firebase.database.Exclude;

/**
 * Created by sahilsharma on 5/23/18.
 */

public class Upload {

    private String mName;
    private String mImageUrl;
    private String mKey;

    public Upload(){
        //empty constructor needed
    }

    public Upload(String name, String imageUrl){

        if(name.trim().equals("")){
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
    }

    public String getmName() {
        return mName;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    @Exclude
    public String getmKey(){
        return mKey;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}
