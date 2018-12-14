package com.android.dcxiaolou.hrichedittest.mode;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Images extends BmobObject {

    private BmobFile img;

    public BmobFile getImg() {
        return img;
    }

    public void setImg(BmobFile img) {
        this.img = img;
    }
}
