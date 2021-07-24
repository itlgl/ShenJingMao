package com.itlgl.android.shenjingmao;

import com.itlgl.android.shenjingmao.algorithm.Constants;
import com.itlgl.android.shenjingmao.algorithm.MaoMap;
import com.itlgl.android.shenjingmao.algorithm.NextStep;
import com.itlgl.android.shenjingmao.algorithm.Point;

import java.util.List;

/**
 * @author guanliang on 2021/7/6
 */
public class GamePresenter implements GameContract.Presenter {
    private final GameContract.View mView;
    private int mTotalStep = 0;
    private boolean hasBeSurrounded = false;
    private int mMax = Constants.MAX_DEFAULT;
    private MaoMap mMaoMap;

    public GamePresenter(GameContract.View view) {
        mView = view;
    }

    @Override
    public void startGame(int max) {
        if(max <= 0) {
            max = Constants.MAX_DEFAULT;
        }
        mMax = max;
        mMaoMap = new MaoMap(mMax);

        mTotalStep = 0;
        hasBeSurrounded = false;
        mMaoMap.randomMap();
        Point maoPoint = mMaoMap.getCatPoint();
        if(maoPoint == null) {
            System.out.println("没有找到猫的位置！");
            return;
        }
        List<Point> dotWalls = mMaoMap.getWallPoints();
        mView.handleGameStart(dotWalls, maoPoint);
    }

    @Override
    public void userInput(int row, int col) {
        mMaoMap.addMapWall(row, col);
        mTotalStep++;

        // 如果已经在边界
        Point catPoint = mMaoMap.getCatPoint();
        if(MaoMap.isBorderPoint(catPoint.x, catPoint.y, mMax)) {
            mView.handleGameFail();
            return;
        }

        long start = System.currentTimeMillis();
        NextStep nextStep = mMaoMap.getNextStep();
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));

        if(nextStep.noway || nextStep.nextPoint == null) {
            mView.handleGamePass(mTotalStep);
            return;
        }
        if(nextStep.hasBeSurrounded && !hasBeSurrounded) {
            hasBeSurrounded = true;
            mView.handleCatSurrounded();
        }
        mMaoMap.setCatPosition(nextStep.nextPoint.x, nextStep.nextPoint.y);
        mView.handleCatNextStep(nextStep.nextPoint.x, nextStep.nextPoint.y);
    }
}
