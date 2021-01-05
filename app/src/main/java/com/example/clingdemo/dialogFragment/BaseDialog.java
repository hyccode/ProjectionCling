package com.example.clingdemo.dialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.clingdemo.R;


/**
 * author : hyc
 * date   : 2019/3/15 1:05 PM
 * desc   : Dialog通用样式
 */
public abstract class BaseDialog extends DialogFragment {
    private static long lastClickTime;


    private float mDimAmount = 0.75f;//背景昏暗度
    private boolean mShowBottomEnable;//是否底部显示
    private int mAnimStyle = 0;//进入退出动画
    private boolean mOutCancel = true;//点击外部取消
    private Context mContext;

    //dialog宽高
    private int mWidth;
    private int mHeight;
    private DialogDismissListener dialogDismissListener;

    //dialog的位置，默认居中
    private int gravity = Gravity.CENTER;

    //相对与原始位置的偏移量
    private int x = 0;
    private int y = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.dialog);
        //全屏
        setStyle(DialogFragment.STYLE_NORMAL, R.style.baseDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setUpLayoutId(), container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        convertView(ViewHolder.create(view), this);
    }


    @Override
    public void onStart() {
        super.onStart();
        initParams();
    }

    private void initParams() {
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = mDimAmount;
            //设置dialog显示位置
            if (mShowBottomEnable) {
                params.gravity = Gravity.BOTTOM;
            } else {
                params.gravity = gravity;
            }

            //设置dialog偏移量
            if (x > 0) {
                params.x = x;
            }
            if (y > 0) {
                params.y = y;
            }

            //设置dialog宽度
            if (mWidth == 0) {
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            } else {
                params.width = mWidth;
            }

            //设置dialog高度
            if (mHeight == 0) {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            } else {
                params.height = mHeight;
            }

            //设置dialog动画
            if (mAnimStyle != 0) {
                window.setWindowAnimations(mAnimStyle);
            }
            window.setAttributes(params);
        }
        setCancelable(mOutCancel);
    }

    /**
     * 设置背景昏暗度
     *
     * @param dimAmount
     * @return
     */
    public BaseDialog setDimAmout(@FloatRange(from = 0, to = 1) float dimAmount) {
        mDimAmount = dimAmount;
        return this;
    }

    /**
     * 设置dialog偏移量
     * <p>
     * lp.x与lp.y表示相对于原始位置的偏移.
     * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值无效.
     * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值无效.
     * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值无效.
     * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值无效.
     * 当参数值包含Gravity.CENTER_HORIZONTAL时
     * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
     * 当参数值包含Gravity.CENTER_VERTICAL时
     * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
     * gravity的默认值为Gravity.CENTER
     *
     * @param x
     * @param y
     * @return
     */
    public BaseDialog setLayoutParamsWY(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * 是否显示底部
     *
     * @param showBottom
     * @return
     */
    public BaseDialog setShowBottom(boolean showBottom) {
        mShowBottomEnable = showBottom;
        return this;
    }

    /**
     * 是否显示的位置
     *
     * @param gravity
     * @return
     */
    public BaseDialog setShowGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * 设置宽高
     * 可以传WindowManager.LayoutParams.WRAP_CONTENT，WindowManager.LayoutParams.MATCH_PARENT，具体的宽高
     *
     * @param width
     * @param height
     * @return
     */
    public BaseDialog setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        return this;
    }


    /**
     * 设置进入退出动画
     *
     * @param animStyle
     * @return
     */
    public BaseDialog setAnimStyle(@StyleRes int animStyle) {
        mAnimStyle = animStyle;
        return this;
    }

    /**
     * 设置是否点击外部取消
     *
     * @param outCancel
     * @return
     */
    public BaseDialog setOutCancel(boolean outCancel) {
        mOutCancel = outCancel;
        return this;
    }


    public BaseDialog show(FragmentManager manager) {
        super.show(manager, String.valueOf(System.currentTimeMillis()));
        return this;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        // 这里吧原来的commit()方法换成了commitAllowingStateLoss()
        ft.commitAllowingStateLoss();
    }

    /**
     * 设置dialog布局
     *
     * @return
     */
    public abstract int setUpLayoutId();


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dialogDismissListener != null) {
            dialogDismissListener.dismiss();
        }
    }

    public BaseDialog setOnDismissListener(DialogDismissListener dialogDismissListener) {
        this.dialogDismissListener = dialogDismissListener;
        return this;
    }

    /**
     * 操作dialog布局
     *
     * @param holder
     * @param dialog
     */
    public abstract void convertView(ViewHolder holder, BaseDialog dialog);



    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /**
     * 防止快速点击 true快
     */
    public static boolean isFastClick(long MIN_DELAY_TIME) {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

}
