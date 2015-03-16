package com.bossyao.rollershutter.library;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ScrollView;

/**
 * Created by BossYao on 15/3/13.
 */
public class RollerShuttersView extends ScrollView {

    private static final int DRAG_MAX_DISTANCE = 120;

    private static final int INVALID_POINTER = -1;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private static final float DRAG_RATE = .5f;

    public static final int MAX_OFFSET_ANIMATION_DURATION = 700;

    private ContentView mContentView;

    //触发下拉操作的view,一般都是头部以下
    private View mTarget;

//    private View mBaseRefreshView;

    private View mRefreshView;

    private Interpolator mDecelerateInterpolator;

    private boolean mIsBeingDragged;

    private boolean mRefreshing;

    private boolean mIsChildMoving;

    //当前下拉百分比
    private float mCurrentDragPercent;

    //拉伸状态判断值
    private int mTotalDragDistance;

    //当前下拉值
    private int mCurrentOffsetTop;

    private int mActivePointerId;

    //最开始按下位置
    private float mInitialMotionY;

    private int mTouchSlop;

    private int mFrom;

    private float mFromDragPercent;


    public RollerShuttersView(Context context) {
        super(context);
    }

    public RollerShuttersView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //各项参数填充
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //577 -243 284
        mTotalDragDistance = convertDpToPixel(DRAG_MAX_DISTANCE);
    }

    public RollerShuttersView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        Log.e("","canParentViewScrollUp : " + canParentViewScrollUp());

        if (!isEnabled() || canParentViewScrollUp()) {

            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTop(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                Log.e("", "onInterceptTouchEvent ACTION_DOWN" + initialMotionY);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("","onInterceptTouchEvent ACTION_MOVE");
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                Log.e("", "onInterceptTouchEvent ACTION_MOVE" + y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                Log.e("", "onInterceptTouchEvent ACTION_UP or ACTION_CANCEL");
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                Log.e("", "onInterceptTouchEvent ACTION_POINTER_UP ");
                break;
        }

        Log.e("","mIsBeingDragged : " + mIsBeingDragged);

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

//        if (!mIsBeingDragged ) {
////            return super.onTouchEvent(ev);
//
//            return true;
//        }



        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                return super.onTouchEvent(ev);
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                if(mIsChildMoving){
                    Log.e("","childmoving");
                    return super.onTouchEvent(ev);
                }


                //当前所在地
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                //距离原点差值
                final float yDiff = y - mInitialMotionY;

                if(!mIsBeingDragged && !mIsChildMoving){
                    if(Math.abs(yDiff) > mTouchSlop){

                        //一开始上滑
                        if (yDiff < 0 ){

                            mIsChildMoving = true;
                            return super.onTouchEvent(ev);
                        }else {

                            //如果是下拉到下方，就继续操作scrollview
                            if(canParentViewScrollUp()){
                                mIsChildMoving = true;
                                return super.onTouchEvent(ev);

                            //不然就判定为在顶部，要下拉
                            }else {
                                mIsBeingDragged = true;

                                //然后不返回，继续后面代码的操作
                            }
                        }

                    }else {
                        return true;
                    }

                }

//                if (yDiff > mTouchSlop && !mIsBeingDragged) {
//                    mIsBeingDragged = true;
//                }else {
//
//                    mIsBeingDragged = false;
//
//                    //如果是向下滑动的话
//                    if (yDiff < 0 ){
//                        mIsChildMoving = true;
//
//                        return super.onTouchEvent(ev);
//                    }else {
//
//                        return true;
//                    }
//
//
//                }

                //真正移动的值，这里为0.5即一半
                final float scrollTop = yDiff * DRAG_RATE;

                // 下拉程度百分比
                mCurrentDragPercent = scrollTop / mTotalDragDistance;
                if (mCurrentDragPercent < 0) {
                    return false;
                }

                //被限制的下拉百分比 百分比不能超过1
                float boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                Log.e("","boundedDragPercent : " +boundedDragPercent);

                //与最大展示边界的相对距离
                float extraOS = Math.abs(scrollTop) - mTotalDragDistance;

                //最大展示边界
                float slingshotDist = mTotalDragDistance;

                //弹力阻尼强度百分比（一开始没有，然后过边界后逐步加强）(0-2)
                float tensionSlingshotPercent = Math.max(0,
                        Math.min(extraOS, slingshotDist * 2) / slingshotDist);

                Log.e("","tensionSlingshotPercent1 : "+tensionSlingshotPercent);

                // x(1-x)  0<x< 1/2 所以tensionPercent增长逐渐缓慢 （0-0.5）
                float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                        (tensionSlingshotPercent / 4), 2)) * 2f;

                Log.e("","tensionPercent1 : "+tensionPercent);

                //可以超过边界的长度，最多边界的 1/4
                float extraMove = (slingshotDist) * tensionPercent / 2;

                //最后targetView所移动的距离
                int targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);

//                mBaseRefreshView.setPercent(mCurrentDragPercent, true);

                setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
                Log.e("","mCurrentOffsetTop : "+mCurrentOffsetTop);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                //当前纵坐标
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                //判定状态值
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                mIsChildMoving = false;

                if (overScrollTop > mTotalDragDistance) {
                    mRefreshing = true;
                } else {
                    mRefreshing = false;
                    animateOffsetToStartPosition();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private void moveToStart(float interpolatedTime) {
        int targetTop = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
        int offset = targetTop - mTarget.getTop();

        mCurrentDragPercent = targetPercent;
        //mBaseRefreshView.setPercent(mCurrentDragPercent, true);
        setTargetOffsetTop(offset, false);
    }

    private Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //mBaseRefreshView.stop();
            mCurrentOffsetTop = mTarget.getTop();
        }
    };

    private void animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = Math
                .abs((long) (MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent));

        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToStartPosition);
    }

    public void setContentView(ContentView contentView){
        mContentView = contentView;
        mTarget = mContentView.getSurfaceView();
        mRefreshView = mContentView.getInsideView();

    }

    private boolean canParentViewScrollUp() {

        View view = this;

        if (android.os.Build.VERSION.SDK_INT < 14) {
            return view.getScrollY() > 0;
        } else {

            //判断是否可以滚动
            return ViewCompat.canScrollVertically(view, -1);
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    //设置两个view的上下偏移距离
    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
        mTarget.offsetTopAndBottom(offset);
        mRefreshView.offsetTopAndBottom(offset);
        mCurrentOffsetTop = mTarget.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    public int convertDpToPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    /**
     * 指定所要控制触摸的view
     */
    private void ensureTarget() {
        if (mTarget != null) {
            return;
        }
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            mTarget = child;
        }
    }

}
