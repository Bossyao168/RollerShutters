package com.bossyao.rollershutter.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by BossYao on 15/3/13.
 */
public class ContentView extends RelativeLayout {


    private View _surfaceView;

    private View _insideView;

    public View getSurfaceView() {
        return _surfaceView;
    }

    public View getInsideView() {
        return _insideView;
    }

    public ContentView(Context context) {
        super(context);
    }

    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);


    }

    public ContentView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureTarget();
    }

    /**
     * 指定所要控制触摸的view
     */
    private void ensureTarget() {
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {

                View child = getChildAt(i);

                if (i == 0) {
                    _insideView = child;
                }

                if (i == 1) {
                    _surfaceView = child;
                }
            }
        }
    }

}
