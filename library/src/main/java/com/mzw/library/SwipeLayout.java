package com.mzw.library;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Created by M on 2015/10/23.
 */
public class SwipeLayout extends FrameLayout {

    private static final String TAG = SwipeLayout.class.getSimpleName();

    public static final int STATUS_OPEN = 0;
    public static final int STATUS_CLOSE = 1;

    private ViewDragHelper mViewDragHelper;

    private View mDragView;
    private View mHideView;

    private int mWidth;
    private int mHeight;

    private int mDragDistance;

    private int mState;

    private int mDragSlop;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new Callback());
        mDragSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(getContext()));
        setState(STATUS_CLOSE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mDragView.layout(getPaddingLeft(), getPaddingTop(), mWidth - getPaddingRight(), mHeight - getPaddingBottom());
        mHideView.layout(mWidth - getPaddingRight(), getPaddingTop(), mWidth - getPaddingRight() + mDragDistance, mHeight - getPaddingBottom());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            mDragView = getChildAt(0);
            mHideView = getChildAt(1);
        } catch (Exception e) {
            throw new NullPointerException("The ViewGroup must have two child views!");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        mDragDistance = mHideView.getMeasuredWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    class Callback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left > getPaddingLeft()) {
                return getPaddingLeft();
            }
            if (left < getPaddingLeft() - mDragDistance) {
                return getPaddingLeft() - mDragDistance;
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return getPaddingTop();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragDistance;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (getPaddingLeft() - mDragView.getLeft() < mDragSlop) {
                mViewDragHelper.smoothSlideViewTo(getHideView(), mWidth - getPaddingRight(), getPaddingTop());
                mViewDragHelper.smoothSlideViewTo(getDragView(), getPaddingLeft(), getPaddingTop());
            } else {
                mViewDragHelper.smoothSlideViewTo(getHideView(), mWidth - getPaddingRight() - mDragDistance, getPaddingTop());
                mViewDragHelper.smoothSlideViewTo(getDragView(), getPaddingLeft() - mDragDistance, getPaddingTop());
            }
            ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mHideView.layout(mHideView.getLeft() + dx, mHideView.getTop(), mHideView.getRight() + dx, mHideView.getBottom());
            ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
        }
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public View getDragView() {
        if (getChildCount() == 0) return null;
        return getChildAt(0);
    }

    public View getHideView() {
        if (getChildCount() == 1) return null;
        return getChildAt(1);
    }
}
