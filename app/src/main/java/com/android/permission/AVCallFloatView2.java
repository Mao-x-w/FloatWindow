/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.android.permission;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.android.floatwindowpermission.R;

import java.io.DataOutputStream;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-05-19
 */
public class AVCallFloatView2 extends FrameLayout {

    private View mLlContent;
    private View mButtonTop;
    private View mButtonBottom;
    private View mTemp;
    private View mBottomRoot;
    private float xInView;
    private float yInView;
    private float moveXInView;
    private float moveYInView;
    private LayoutParams mLayoutParams;
    private LayoutParams mLayoutParams1;

    Handler mHandler=new Handler();
    private OnSimulateClickListener mOnSimulateClickListener;

    public AVCallFloatView2(Context context) {
        super(context);
        initView();
    }

    private void initView() {
//        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View floatView = inflater.inflate(R.layout.float_window_layout2, null);
        mLlContent = floatView.findViewById(R.id.ll_content);
        mButtonTop = floatView.findViewById(R.id.click_button_top);
        mButtonBottom = floatView.findViewById(R.id.click_button_bottom);
        mTemp = floatView.findViewById(R.id.temp);
        mBottomRoot = floatView.findViewById(R.id.bottom_root);
        floatView.findViewById(R.id.simulate_click).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float clickX = mButtonBottom.getX();
                float clickY = mBottomRoot.getY() + mButtonBottom.getY();
                mBottomRoot.setVisibility(GONE);
                mTemp.setVisibility(GONE);
                setSimulateClick2(mButtonTop,clickX,clickY);

//                mBottomRoot.setVisibility(VISIBLE);
//                mTemp.setVisibility(VISIBLE);
            }
        });

        floatView.findViewById(R.id.simulate_cancle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSimulateClickListener!=null)
                    mOnSimulateClickListener.onSimulateClick();
            }
        });

        mLayoutParams = (LayoutParams) mButtonTop.getLayoutParams();
        mLayoutParams1 = (LayoutParams) mButtonBottom.getLayoutParams();

        mButtonTop.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onButtonTouch(v,event);
                return true;
            }
        });

        addView(floatView);

    }

    /**
     * 这种方式只有在当前应用中有效
     * @param view
     * @param x
     * @param y
     */
    private void setSimulateClick(View view, float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += 1000;
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, x, y, 0);
        view.onTouchEvent(downEvent);
        view.onTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }

    private void setSimulateClick2(View view, final float x, final float y) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    String cmd = "/system/bin/input tap "+x+" "+y+" \n";
//                    String cmd = "/system/bin/input swipe "+x+" "+y+" "+x+100+" "+y+" "+ 100+"\n";
                    os.writeBytes(cmd);
                    os.writeBytes("exit\n");
                    os.flush();
                    os.close();
                    process.waitFor();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnSimulateClickListener!=null)
                                mOnSimulateClickListener.onSimulateClick();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onButtonTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveXInView = event.getX();
                moveYInView = event.getY();

                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition(moveXInView-xInView,moveYInView-yInView);
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
    }

    private void updateViewPosition(float deltaX,float deltaY) {
        int leftMargin = mButtonTop.getLeft() + (int) deltaX;
        int topMargin = mButtonTop.getTop() + (int) deltaY;

        if (leftMargin<0)
            leftMargin=0;

        if (leftMargin>1080-mButtonTop.getWidth())
            leftMargin=1080-mButtonTop.getWidth();

        if (topMargin<0)
            topMargin=0;

        if (topMargin>dp2px(200)-mButtonTop.getHeight())
            topMargin=dp2px(200)-mButtonTop.getHeight();

        mLayoutParams.leftMargin= leftMargin;
        mLayoutParams.topMargin= topMargin;
        mLayoutParams1.leftMargin= leftMargin;
        mLayoutParams1.topMargin= topMargin;
        mButtonTop.setLayoutParams(mLayoutParams);
        mButtonBottom.setLayoutParams(mLayoutParams1);
    }

    public int dp2px(float dp){
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setOnSimulateClickListener(OnSimulateClickListener onSimulateClickListener){
        mOnSimulateClickListener = onSimulateClickListener;
    }

    public interface OnSimulateClickListener{
        void onSimulateClick();
    }

}
