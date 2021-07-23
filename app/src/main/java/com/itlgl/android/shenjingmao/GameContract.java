package com.itlgl.android.shenjingmao;

import com.itlgl.android.shenjingmao.algorithm.Point;

import java.util.List;

/**
 * @author guanliang on 2021/7/6
 */
public interface GameContract {
    interface View {
        void handleGameStart(List<Point> dotWalls, Point crazyCat);

        void handleGameFail();

        void handleGamePass(int totalStep);

        void handleCatSurrounded();

        void handleCatNextStep(int x, int y);
    }

    interface Presenter {
        void startGame(int max);

        void userInput(int row, int col);
    }
}
