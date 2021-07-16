package com.itlgl.android.shenjingmao;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.itlgl.android.shenjingmao.algorithm.Point;
import com.itlgl.android.shenjingmao.databinding.ViewGameBinding;

import java.util.List;

/**
 * @author guanliang on 2021/7/5
 */
public class GameView extends FrameLayout /*implements GameContract.View*/ {
    public static final int MAX = 9;

    private ViewGameBinding mGameBinding;
    private ImageView[][] mDotViewArr = new ImageView[MAX][MAX];
    private ImageView mCatView;

    private DotClickListener mDotClickListener;

    public GameView(@NonNull Context context) {
        this(context, null);
    }

    public GameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView(context);
    }

    public void setDotClickListener(DotClickListener listener) {
        this.mDotClickListener = listener;
    }

    private void initView(@NonNull Context context) {
        mGameBinding = ViewGameBinding.inflate(LayoutInflater.from(context), this, true);

        addDotViews();
        addCatView();
        initDotClick();
    }

    private void addDotViews() {
        // 先创建dot imageview
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                ImageView iv = new ImageView(getContext());
                iv.setId(ViewCompat.generateViewId());
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iv.setImageResource(R.drawable.selector_dot_bg);
                mDotViewArr[i][j] = iv;
            }
        }

        for (int i = 0; i < MAX; i++) {
            addOneLineViews(i);
        }
    }

    private void addOneLineViews(int row) {
//        <androidx.appcompat.widget.AppCompatImageView
//        android:id="@+id/iv_dot_0"
//        android:layout_width="0dp"
//        android:layout_height="0dp"
//        android:scaleType="fitCenter"
//        android:src="@drawable/selector_dot_bg"
//        app:layout_constraintDimensionRatio="w,1:1"
//        app:layout_constraintEnd_toStartOf="@+id/iv_dot_1"
//        app:layout_constraintHorizontal_chainStyle="spread_inside"
//        app:layout_constraintStart_toStartOf="parent"
//        app:layout_constraintTop_toBottomOf="@+id/iv_title_bg"
//        app:layout_constraintWidth_percent="0.1" />

        // padding
        View paddingView = new View(getContext());
        paddingView.setId(ViewCompat.generateViewId());
        // padding在头还是在尾
        boolean paddingAtStart = row % 2 == 1;
        if(paddingAtStart) {
            ConstraintLayout.LayoutParams paddingLp = new ConstraintLayout.LayoutParams(0, 0);
            paddingLp.dimensionRatio = "w,1:1";
            paddingLp.topToBottom = row == 0 ? R.id.iv_title_bg : mDotViewArr[row - 1][0].getId();
            paddingLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            paddingLp.endToStart = mDotViewArr[row][0].getId();
            paddingLp.horizontalChainStyle = ConstraintLayout.LayoutParams.CHAIN_SPREAD_INSIDE;
            paddingLp.matchConstraintPercentWidth = 0.05f;

            mGameBinding.layoutDots.addView(paddingView, paddingLp);

            for (int j = 0; j < MAX; j++) {
                ConstraintLayout.LayoutParams dotLp = new ConstraintLayout.LayoutParams(0, 0);
                dotLp.dimensionRatio = "w,1:1";
                dotLp.topToTop = paddingView.getId();
                dotLp.startToEnd = j == 0 ? paddingView.getId() : mDotViewArr[row][j - 1].getId();
                if(j == MAX - 1) {
                    dotLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                } else {
                    dotLp.endToStart = mDotViewArr[row][j + 1].getId();
                }
                dotLp.matchConstraintPercentWidth = 0.1f;

                mGameBinding.layoutDots.addView(mDotViewArr[row][j], dotLp);
            }
        } else {
            for (int j = 0; j < MAX; j++) {
                ConstraintLayout.LayoutParams dotLp = new ConstraintLayout.LayoutParams(0, 0);
                dotLp.dimensionRatio = "w,1:1";
                dotLp.matchConstraintPercentWidth = 0.1f;
                if(j == 0) {
                    if(row == 0) {
                        dotLp.bottomToBottom = R.id.iv_placeholder;
                    } else {
                        dotLp.topToBottom = mDotViewArr[row - 1][0].getId();
                    }
                    dotLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    dotLp.endToStart = mDotViewArr[row][j + 1].getId();
                    dotLp.horizontalChainStyle = ConstraintLayout.LayoutParams.CHAIN_SPREAD_INSIDE;
                } else {
                    dotLp.topToTop = mDotViewArr[row][0].getId();
                    dotLp.startToEnd = mDotViewArr[row][j - 1].getId();
                    dotLp.endToStart = j == MAX - 1 ? paddingView.getId() : mDotViewArr[row][j + 1].getId();
                }
                mGameBinding.layoutDots.addView(mDotViewArr[row][j], dotLp);
            }

            ConstraintLayout.LayoutParams paddingLp = new ConstraintLayout.LayoutParams(0, 0);
            paddingLp.topToTop = mDotViewArr[row][0].getId();
            paddingLp.startToEnd = mDotViewArr[row][MAX - 1].getId();
            paddingLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            paddingLp.matchConstraintPercentWidth = 0.05f;

            mGameBinding.layoutDots.addView(paddingView, paddingLp);
        }
    }

    private void addCatView() {
        mCatView = new ImageView(getContext());
        mCatView.setImageResource(R.drawable.anim_stay);
        mCatView.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.startToStart = mDotViewArr[4][4].getId();
        lp.endToEnd = mDotViewArr[4][4].getId();
        lp.bottomToBottom = mDotViewArr[4][4].getId();
        mGameBinding.layoutDots.addView(mCatView, lp);
    }

    private void initDotClick() {
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                final int x = i;
                final int y = j;
                mDotViewArr[i][j].setOnClickListener(v -> {
                    if(mDotViewArr[x][y].isSelected()) {
                        return;
                    }
                    mDotViewArr[x][y].setSelected(true);
                    if(mDotClickListener != null) {
                        mDotClickListener.onDotClick(x, y);
                    }
                });
            }
        }
    }

    public void setCatPosition(int x, int y) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mCatView.getLayoutParams();
        lp.startToStart = mDotViewArr[x][y].getId();
        lp.endToEnd = mDotViewArr[x][y].getId();
        lp.bottomToBottom = mDotViewArr[x][y].getId();
        mCatView.setLayoutParams(lp);
        mCatView.setVisibility(View.VISIBLE);
    }

    public void setCatState(boolean surrounded) {
        Drawable drawableOld = mCatView.getDrawable();
        if(drawableOld instanceof AnimationDrawable) {
            ((AnimationDrawable) drawableOld).stop();
        }
        AnimationDrawable drawable = (AnimationDrawable) ResourcesCompat.getDrawable(getResources(), surrounded ? R.drawable.anim_surround : R.drawable.anim_stay, null);
        mCatView.setImageDrawable(drawable);
        if (drawable != null) {
            drawable.start();
        }
        mCatView.setVisibility(View.VISIBLE);
    }

    public void setDotsSelected(List<Point> dotPoints) {
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                mDotViewArr[i][j].setSelected(false);
            }
        }
        if(dotPoints != null && dotPoints.size() > 0) {
            for (Point point : dotPoints) {
                if(point.x < 0 || point.x >= MAX || point.y < 0 || point.y >= MAX) {
                    continue;
                }
                mDotViewArr[point.x][point.y].setSelected(true);
            }
        }
    }

    public void setDotSelected(int x, int y) {
        mDotViewArr[x][y].setSelected(true);
    }

    public static interface DotClickListener {
        /**
         * 路径点被点击的回调，回调这个方法时，此点已经更新界面，被选中
         * @param x [0-9]
         * @param y [0-9]
         */
        public void onDotClick(int x, int y);
    }
}
