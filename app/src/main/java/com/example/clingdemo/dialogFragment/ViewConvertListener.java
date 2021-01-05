package com.example.clingdemo.dialogFragment;

import java.io.Serializable;

/**
 * author : hyc
 * date   : 2019/3/15 1:05 PM
 * desc   :
 */
public interface ViewConvertListener extends Serializable {
    long serialVersionUID = System.currentTimeMillis();

    void convertView(ViewHolder holder, BaseDialog dialog);
}
