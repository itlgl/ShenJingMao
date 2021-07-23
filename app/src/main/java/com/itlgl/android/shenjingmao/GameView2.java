package com.itlgl.android.shenjingmao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.itlgl.android.shenjingmao.algorithm.MaoMap;
import com.itlgl.android.shenjingmao.algorithm.Point;

import java.util.List;

/**
 * @author guanliang on 2021/7/19
 */
public class GameView2 extends View {
    private AnimationDrawable mStayDrawable;
    private AnimationDrawable mSurroundDrawable;
    /**
     * 猫的状态，false-普通状态stay true-包围状态surround
     */
    private boolean mStayOrSurround = false;
    private int mDotCount;
    private int mDotPadding;
    private Paint mCircleNormalPaint;
    private Paint mCircleWallPaint;
    private int[][] mMap = new int[MaoMap.MAX][MaoMap.MAX];
    private Point mCatPosition = new Point();
    private boolean mDrawCat = false;
    private GameView.DotClickListener mDotClickListener;
    // 按下时对应地图dot的x/y
    private Point mDotPointWhenTouchDown = new Point();

    public GameView2(Context context) {
        this(context, null);
    }

    public GameView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initAttrs(context, attrs);
        initView();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GameView2);
        mDotCount = ta.getInt(R.styleable.GameView2_dotCount, MaoMap.MAX);
        if(mDotCount <= 0) {
            mDotCount = MaoMap.MAX;
        }
        mDotPadding = ta.getDimensionPixelSize(R.styleable.GameView2_dotPadding, 5);
        ta.recycle();
    }

    private void initView() {

        mStayDrawable = (AnimationDrawable) ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.anim_stay, null);
        mSurroundDrawable = (AnimationDrawable) ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.anim_surround, null);
        // 初始化一下drawable的bounds，默认是{0, 0, 0, 0}
        mStayDrawable.setBounds(0, 0, mStayDrawable.getIntrinsicWidth(), mStayDrawable.getIntrinsicHeight());
        mSurroundDrawable.setBounds(0, 0, mSurroundDrawable.getIntrinsicWidth(), mSurroundDrawable.getIntrinsicHeight());
        mStayDrawable.setCallback(this);
        mSurroundDrawable.setCallback(this);

        //mStayDrawable.start();

        mCircleNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleNormalPaint.setColor(0xFFB5B5B5);
        mCircleNormalPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCircleWallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleWallPaint.setColor(0xFFFF845E);
        mCircleWallPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCatPosition.set(MaoMap.MAX / 2, MaoMap.MAX / 2);
    }

    public void setDotClickListener(GameView.DotClickListener listener) {
        this.mDotClickListener = listener;
    }

    public void setCatState(boolean surrounded) {
        if(surrounded) {
            setCatSurrounded();
        } else {
            setCatStayed();
        }
    }

    public void setCatSurrounded() {
        mStayOrSurround = true;
        mStayDrawable.stop();
        mSurroundDrawable.start();
        invalidate();
    }

    public void setCatStayed() {
        mStayOrSurround = false;
        mStayDrawable.start();
        mSurroundDrawable.stop();
        invalidate();
    }

    public void setCatPosition(int x, int y) {
        mCatPosition.set(x, y);
        invalidate();
    }

    public void setDotsSelected(List<Point> dotPoints) {
        for (int i = 0; i < MaoMap.MAX; i++) {
            for (int j = 0; j < MaoMap.MAX; j++) {
                mMap[i][j] = MaoMap.POINT_EMPTY;
            }
        }
        if(dotPoints != null && dotPoints.size() > 0) {
            for (Point point : dotPoints) {
                if(point.x < 0 || point.x >= MaoMap.MAX || point.y < 0 || point.y >= MaoMap.MAX) {
                    continue;
                }
                mMap[point.x][point.y] = MaoMap.POINT_WALL;
            }
        }
        invalidate();
    }

    public void setDotSelected(int x, int y) {
        mMap[x][y] = MaoMap.POINT_WALL;
        invalidate();
    }

    public void setCatVisible(boolean visible) {
        mDrawCat = visible;
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        super.verifyDrawable(who);
        // super里面判断了是背景或前景图才init，改为true
        return true;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        //super.invalidateDrawable(drawable);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        // 宽度和高度的比例 MAX = 9
        // 宽度 = 9.5 * dotWidth + 8 * dotPadding
        // 则 dotWidth = (width - 8 * dotPadding) / 9.5
        // dotHeight = dotWidth
        // 高度 = dotHeight * 9 + 猫露头的高度
        // 按猫站在dot的下1/4位置算，则 猫露头的高度 = 猫高度 - dotHeight * 3/4
        // 半个dot+count个dot+(count-1)个padding
        int dotWidth = (int) ((width - mDotPadding * (mDotCount - 1)) / (mDotCount + 0.5f));

        Drawable catDrawable = mStayOrSurround ? mSurroundDrawable : mStayDrawable;
        int catHeight = catDrawable.getBounds().bottom - catDrawable.getBounds().top;
        int catImgOutcropHeight = (int) (catHeight - dotWidth * 3f / 4f);
        int height = dotWidth * mDotCount + catImgOutcropHeight;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 根据dot大小适配一下猫图片的大小
        // dot-45 cat-64 64/45=1.42
        mStayDrawable.setBounds(0, 0, (int) (dotWidth * 1.42f), (int) (dotWidth * 1.42f * mStayDrawable.getIntrinsicHeight() / mStayDrawable.getIntrinsicWidth()));
        mSurroundDrawable.setBounds(0, 0, (int) (dotWidth * 1.42f), (int) (dotWidth * 1.42f * mSurroundDrawable.getIntrinsicHeight() / mSurroundDrawable.getIntrinsicWidth()));
    }

    private int getDotWidth() {
        int width = getWidth();
        return (int) ((width - mDotPadding * (mDotCount - 1)) / (mDotCount + 0.5f));
    }

    private int getCatImgOutcropHeight() {
        int dotWidth = getDotWidth();

        Drawable catDrawable = mStayOrSurround ? mSurroundDrawable : mStayDrawable;
        int catHeight = catDrawable.getBounds().bottom - catDrawable.getBounds().top;
        return (int) (catHeight - dotWidth * 3f / 4f);
    }

    private Point getDotPointByTouchPosition(float ex, float ey) {
        return new Point();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // 获取对应dot位置
        int catImgOutcropHeight = getCatImgOutcropHeight();
        int dotWidth = getDotWidth();
        int dotRow = (int) ((y - catImgOutcropHeight) / dotWidth);
        int cxStart = dotRow % 2 == 0 ? 0 : dotWidth / 2;
        //System.out.println((x - cxStart) / (dotWidth + mDotPadding));
        int dotCol = (int) ((x - cxStart) / (dotWidth + mDotPadding));
        int dotYOffset = (int) (( x - cxStart) % (dotWidth + mDotPadding));
        if(dotYOffset > dotWidth) {
            // 点在了空白位置
            dotCol = -1;
        }
        if(y < catImgOutcropHeight) {
            // 点在了上方空白区域
            dotCol = -1;
        }
        //System.out.println("dotRow=" + dotRow + ",dotCol=" + dotCol);

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDotPointWhenTouchDown.set(dotRow, dotCol);
                break;
            case MotionEvent.ACTION_MOVE:
                if(!(mDotPointWhenTouchDown.x == dotRow && mDotPointWhenTouchDown.y == dotCol)) {
                    // 设置一个非法值
                    mDotPointWhenTouchDown.set(-1, -1);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!(mDotPointWhenTouchDown.x == dotRow && mDotPointWhenTouchDown.y == dotCol)) {
                    // 设置一个非法值
                    mDotPointWhenTouchDown.set(-1, -1);
                }
                if(mDotPointWhenTouchDown.x < 0 || mDotPointWhenTouchDown.y < 0) {
                    // 如果之前滑动出了当前dot的范围，那么认为当前点击事件无效
                    break;
                }
                if(mDotPointWhenTouchDown.x >= MaoMap.MAX || mDotPointWhenTouchDown.y >= MaoMap.MAX) {
                    // 如果算出来的x/y超出了数组限制，不做处理
                    break;
                }
                if(mMap[mDotPointWhenTouchDown.x][mDotPointWhenTouchDown.y] != MaoMap.POINT_EMPTY) {
                    // 如果已经选中，那么不响应
                    break;
                }
                // 刷新map，将点置为墙
                mMap[mDotPointWhenTouchDown.x][mDotPointWhenTouchDown.y] = MaoMap.POINT_WALL;
                invalidate();
                if(mDotClickListener != null) {
                    mDotClickListener.onDotClick(mDotPointWhenTouchDown.x, mDotPointWhenTouchDown.y);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawRect(0, 0, getWidth(), getHeight(), mCircleNormalPaint);
        int dotWidth = getDotWidth();
        int catImgOutcropHeight = getCatImgOutcropHeight();
        int dotRadius = dotWidth / 2;
//        System.out.println("width=" + getWidth() + ",height=" + getHeight());
//        System.out.println("dotWidth=" + dotWidth);
//        System.out.println("catImgOutcropHeight=" + dotWidth);
//        System.out.println("dotRadius=" + dotRadius);

        for (int row = 0; row < MaoMap.MAX; row++) {
            int cxStart = row % 2 == 0 ? dotRadius : dotWidth;
            int cy = catImgOutcropHeight + dotRadius + dotWidth * row;
            for (int col = 0; col < MaoMap.MAX; col++) {
                int cx = cxStart + (dotWidth + mDotPadding) * col;
                canvas.drawCircle(cx, cy, dotRadius,
                        mMap[row][col] == MaoMap.POINT_EMPTY ? mCircleNormalPaint : mCircleWallPaint);
            }
        }

        // 猫的位置
        if(mDrawCat) {
            AnimationDrawable catDrawable = mStayOrSurround ? mSurroundDrawable : mStayDrawable;
            Rect bounds = catDrawable.getBounds();
            int catWidth = bounds.right - bounds.left;
            int catHeight = bounds.bottom - bounds.top;
            int catFootMidX = (mCatPosition.x % 2 == 0 ? dotRadius : dotWidth) + mCatPosition.y * (dotWidth + mDotPadding);
            int catFootY = catHeight + dotWidth * mCatPosition.x;
            int catLeftTopX = catFootMidX - catWidth / 2;
            int catLeftTopY = catFootY - catHeight;
//        System.out.println("catWidth=" + catWidth);
//        System.out.println("catHeight=" + catHeight);
//        System.out.println("catLeftTopX=" + catLeftTopX);
//        System.out.println("catLeftTopY=" + catLeftTopY);
            bounds.offsetTo(catLeftTopX, catLeftTopY);
            // 直接用drawable的draw方法，图片第一次会在左上角闪一下，然后再到当前位置
            Drawable current = catDrawable.getCurrent();
            //catDrawable.draw(canvas);
            if(current instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) current).getBitmap();
                canvas.drawBitmap(bitmap, null, bounds, null);
            } else {
                catDrawable.draw(canvas);
            }
        }

//        System.out.println("gameView2 onDraw...");
    }
}
