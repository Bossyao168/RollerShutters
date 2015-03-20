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

    private static final int DRAG_MAX_DISTANCE = 80;

    private static final int INVALID_POINTER = -1;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private static final float DRAG_RATE = .5f;

    public static final int MAX_OFFSET_ANIMATION_DURATION = 500;

    private ContentView mContentView;

    //触发下拉操作的view,一般都是头部以下
    private View mTarget;

//    private View mBaseRefreshView;

    private View mHideView;

    private MotionEvent mCurrentMotionDownEvent;

    private boolean mHasMadeDown;

    private Interpolator mDecelerateInterpolator;

    private boolean mIsBeingDragged;

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

    private int mTo;

    private float mFromDragPercent;

    private boolean mAnimationEndNotify;

    private enum State {Hide, Show}

    private State mCurrentState = State.Hide;


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

        Log.e("", "canParentViewScrollUp : " + canParentViewScrollUp());

//        if (!isEnabled() || canParentViewScrollUp()) {
//
//            return false;
//        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mCurrentMotionDownEvent = MotionEvent.obtain(ev);
                mHasMadeDown = true;

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
                Log.e("", "onInterceptTouchEvent ACTION_MOVE");
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;

                //如果滑动过大，就将事件给父view
                if (Math.abs(yDiff) > mTouchSlop && !mIsBeingDragged && !mIsChildMoving) {

                    //可以滑动
                    if (canParentViewScrollUp()){
                        mIsBeingDragged = false;
                        mIsChildMoving = true;
                    }else {
                        if(yDiff >0){
                            mIsBeingDragged = true;
                            mIsChildMoving = false;
                        }else {
                            mIsBeingDragged = false;
                            mIsChildMoving = true;
                        }
                    }

                    Log.e("","阿西吧！！！！");

                    return true;

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

        Log.e("", "mIsBeingDragged : " + mIsBeingDragged);

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        Log.e("","好的阿西吧");
        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mCurrentMotionDownEvent = MotionEvent.obtain(ev);
                mHasMadeDown = true;

                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                return super.onTouchEvent(ev);
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                if (mIsChildMoving) {

                    if(mHasMadeDown) {
                        onTouchEvent(mCurrentMotionDownEvent);
                        mHasMadeDown = false;
                    }

                    Log.e("", "childmoving");
                    return super.onTouchEvent(ev);
                }

                //当前所在地
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                //距离原点差值
                final float yDiff = y - mInitialMotionY;

                if (!mIsBeingDragged && !mIsChildMoving) {
                    if (Math.abs(yDiff) > mTouchSlop) {

                        //一开始上滑
                        if (yDiff < 0) {

                            mIsChildMoving = true;
                            return super.onTouchEvent(ev);
                        } else {

                            //如果是下拉到下方，就继续操作scrollview
                            if (canParentViewScrollUp()) {
                                mIsChildMoving = true;
                                return super.onTouchEvent(ev);

                                //不然就判定为在顶部，要下拉
                            } else {
                                mIsBeingDragged = true;

                                //然后不返回，继续后面代码的操作
                            }
                        }

                    } else {
                        return true;
                    }

                }

                //真正移动的值，这里为0.5即一半
                float scrollTop = yDiff * DRAG_RATE;

                //如果是view出现了之后，那么滑动值需要加上边界值
                if (State.Show == mCurrentState) {
                    scrollTop += mTotalDragDistance;
                }

                // 下拉程度百分比
                mCurrentDragPercent = scrollTop / mTotalDragDistance;
                if (mCurrentDragPercent < 0) {
                    return false;
                }

                //被限制的下拉百分比 百分比不能超过1
                float boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                Log.e("", "boundedDragPercent : " + boundedDragPercent);

                //与最大展示边界的相对距离
                float extraOS = Math.abs(scrollTop) - mTotalDragDistance;

                //最大展示边界
                float slingshotDist = mTotalDragDistance;

                //弹力阻尼强度百分比（一开始没有，然后过边界后逐步加强）(0-2)
                float tensionSlingshotPercent = Math.max(0,
                        Math.min(extraOS, slingshotDist * 2) / slingshotDist);

                Log.e("", "tensionSlingshotPercent1 : " + tensionSlingshotPercent);

                // x(1-x)  0<x< 1/2 所以tensionPercent增长逐渐缓慢 （0-0.5）
                float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                        (tensionSlingshotPercent / 4), 2)) * 2f;

                Log.e("", "tensionPercent1 : " + tensionPercent);

                //可以超过边界的长度，最多边界的 1/4
                float extraMove = (slingshotDist) * tensionPercent / 2;

                //最后targetView所移动的距离
                int targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);

//                mBaseRefreshView.setPercent(mCurrentDragPercent, true);

                setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
                Log.e("", "mCurrentOffsetTop : " + mCurrentOffsetTop);
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
                float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                if (State.Show == mCurrentState) {
                    overScrollTop += mTotalDragDistance;

                    if (mIsChildMoving) {
                        mIsBeingDragged = false;
                        mIsChildMoving = false;
                        return false;
                    }
                }

                //hide的时候，拉取超过了view
                if (overScrollTop > mTotalDragDistance && State.Hide == mCurrentState && !mIsChildMoving) {
                    mCurrentState = State.Show;
                    animateOffsetToSomePosition(mTotalDragDistance);
                } else {//拉取没超过
                    mCurrentState = State.Hide;
                    animateOffsetToSomePosition(0);
                }
                mActivePointerId = INVALID_POINTER;

                mIsBeingDragged = false;
                mIsChildMoving = false;
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();

        mTarget.layout(left, top + mCurrentOffsetTop, left + width - right,
                top + height - bottom + mCurrentOffsetTop);
        mHideView.layout(left, top, left + width - right, top + height - bottom);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private void moveToStart(float interpolatedTime) {
        int currentTarget = mFrom + (int) ((mTo - mFrom) * interpolatedTime);

        float toPercent = mTo / mTotalDragDistance;
        float targetPercent = mFromDragPercent
                + (toPercent - mFromDragPercent) * (interpolatedTime);

        int offset = currentTarget - mTarget.getTop();

        mCurrentDragPercent = targetPercent;

        Log.e("", "mTo : " + mTo + " mFrom : " + mFrom + " currentTarget : " + currentTarget
                + " offset : " + offset + " mTarget.getTop() : " + mTarget.getTop());

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

        }
    };

    private void animateOffsetToSomePosition(int endPosition) {

        mFrom = mCurrentOffsetTop;
        mTo = endPosition;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = Math
                .abs((long) (Math.min(MAX_OFFSET_ANIMATION_DURATION,
                        MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent)));

        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        mHideView.clearAnimation();
        mHideView.startAnimation(mAnimateToStartPosition);

    }



    public void setContentView(ContentView contentView) {
        mContentView = contentView;
        mTarget = mContentView.getSurfaceView();
        mHideView = mContentView.getInsideView();

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
        Log.e("", "mTarget.offsetTopAndBottom : " + offset);

        // mHideView.offsetTopAndBottom(offset);
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
