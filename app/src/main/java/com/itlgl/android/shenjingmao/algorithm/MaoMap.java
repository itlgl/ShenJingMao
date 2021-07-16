package com.itlgl.android.shenjingmao.algorithm;

import java.util.*;

public class MaoMap {

    public static final int POINT_CAT = -2;
    public static final int POINT_EMPTY = 0;
    public static final int POINT_WALL = -1;
    public static final int POINT_MARK = 1;

    public static int MAX = 9;

    private int[][] mMap = new int[MAX][MAX];
    private Point mCatPoint = new Point();

    public void randomMap() {
        // 清空
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                mMap[i][j] = POINT_EMPTY;
            }
        }
        mCatPoint.set(MAX / 2, MAX / 2);
        // random
        int count = (int) (Math.random() * 5) + 10;
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * MAX);
            int y = (int) (Math.random() * MAX);
            if (x == mCatPoint.x && y == mCatPoint.y) {
                continue;
            }
            mMap[x][y] = POINT_WALL;
        }
    }

    public void setMap(int[][] map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null!");
        }
        if (map.length != MAX) {
            throw new IllegalArgumentException("map.length should be " + MAX);
        }
        for (int i = 0; i < map.length; i++) {
            if (map[i] == null || map[i].length != MAX) {
                throw new IllegalArgumentException("map[" + i + "].length should be " + MAX);
            }
        }
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                mMap[i][j] = map[i][j];
            }
        }
    }

    public void addMapWall(int x, int y) {
        mMap[x][y] = POINT_WALL;
    }

    public void setCatPosition(int x, int y) {
        mCatPoint.set(x, y);
    }

    public int[][] getMap() {
        return mMap;
    }

    /**
     * 获取下一步的走法
     */
    public NextStep getNextStep() {
        NextStep nextStep = new NextStep();

        Point maoPoint = getCatPoint();
        if (maoPoint == null) {
            throw new IllegalStateException("地图中没有猫的位置!");
        }

        Point[] points = neighborDotsArray(maoPoint.x, maoPoint.y);

        // 是否已经无路可走
        boolean neighborAllWall = true;
        for (int i = 0; i < points.length; i++) {
            int px = points[i].x;
            int py = points[i].y;
            if (isValidPoint(px, py) && mMap[px][py] == POINT_EMPTY) {
                neighborAllWall = false;
                break;
            }
        }
        if (neighborAllWall) {
            System.out.println("无路可走");
            nextStep.noway = true;
            nextStep.hasBeSurrounded = true;
            return nextStep;
        }

        // 寻找周围点走出去的最小步数
        int[] minStepArr = new int[6];
        for (int i = 0; i < points.length; i++) {
            int px = points[i].x;
            int py = points[i].y;
            if (isValidPoint(px, py) && mMap[px][py] == POINT_EMPTY) {
                int[][] copyMap = copyMap(mMap);
                copyMap[maoPoint.x][maoPoint.y] = POINT_CAT;
                minStepArr[i] = minStepToBorder(px, py, copyMap);
            } else {
                minStepArr[i] = Integer.MAX_VALUE;
            }
        }

        int minStep = Integer.MAX_VALUE;
        for (int i = 0; i < minStepArr.length; i++) {
            if (minStep > minStepArr[i]) {
                minStep = minStepArr[i];
            }
        }
        System.out.println("走出去最小步数=" + minStep);
        if (minStep == Integer.MAX_VALUE) {
            System.out.println("被包围了");
            nextStep.hasBeSurrounded = true;
        }
        List<Point> nextPointList = new ArrayList<>();
        for (int i = 0; i < minStepArr.length; i++) {
            if (isValidPoint(points[i]) && mMap[points[i].x][points[i].y] == POINT_EMPTY && minStep == minStepArr[i]) {
                nextPointList.add(points[i]);
                System.out.println("下一步待选点=" + points[i].toString());
            }
        }
        int index = (int) (Math.random() * nextPointList.size());
        Point nextPoint = nextPointList.get(index);
        System.out.println("下一步=" + nextPoint);
        nextStep.nextPoint = nextPoint;
        return nextStep;
    }

    /**
     * 寻找当前点到边界最近的位置
     *
     * @param x   坐标点x
     * @param y   坐标点y
     * @param map 搜索的map
     * @return
     */
    private int minStepToBorder(int x, int y, int[][] map) {
        if (!isValidPoint(x, y)) {
            System.out.println("当前点不在地图范围内，无法找到最短路径");
            return Integer.MAX_VALUE;
        }
        if (map[x][y] == POINT_EMPTY) {
            map[x][y] = POINT_MARK;// 当前点做标记
        }
        // 在边界了
        if (isBorderPoint(x, y)) {
            return 0;
        }
        int minStep = 0;
        List<Point> pointToBeSearched = new ArrayList<>();
        List<Point> pointNextSearched = new ArrayList<>();
        pointToBeSearched.add(new Point(x, y));
        while (pointToBeSearched.size() > 0) {
            for (int i = 0, len = pointToBeSearched.size(); i < len; i++) {
                Point p = pointToBeSearched.get(i);

                if (isBorderPoint(p.x, p.y)) {
                    return minStep;
                }

                // 找到周围点
                Point[] points = neighborDotsArray(p.x, p.y);
                for (int j = 0; j < points.length; j++) {
                    int px = points[j].x;
                    int py = points[j].y;

                    if (isValidPoint(px, py) && map[px][py] == POINT_EMPTY) {
                        pointNextSearched.add(points[j]);
                        // 先把周围点都置为搜索过
                        map[px][py] = POINT_MARK;
                    }
                }
            }
            pointToBeSearched.clear();
            pointToBeSearched.addAll(pointNextSearched);
            pointNextSearched.clear();

            minStep++;
        }
        // 如果遍历完没有找到，返回最大值
        return Integer.MAX_VALUE;
    }

    private int[][] copyMap(int[][] map) {
        int[][] copyMap = new int[map.length][map.length];
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                copyMap[i][j] = map[i][j];
            }
        }
        return copyMap;
    }

    public Point getCatPoint() {
        return mCatPoint;
    }

    public List<Point> getWallPoints() {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                if (mMap[i][j] == POINT_WALL) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
    }

    public void printMap() {
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                System.out.print(mMap[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static List<Point> neighborDotsList(Point point) {
        return neighborDotsList(point.x, point.y);
    }

    public static List<Point> neighborDotsList(int x, int y) {
        Point[] neighborDots = neighborDotsArray(x, y);

        List<Point> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (isValidPoint(neighborDots[i])) {
                result.add(neighborDots[i]);
            }
        }
        return result;
    }

    public static Point[] neighborDotsArray(Point point) {
        return neighborDotsArray(point.x, point.y);
    }

    public static Point[] neighborDotsArray(int x, int y) {
        Point[] neighborDots = new Point[6];
        if (x % 2 == 0) {
            // 偶数行邻节点 上[x - 1, y - 1] [x - 1, y] 下[x+1, y - 1] [x+1, y]
            neighborDots[0] = new Point(x - 1, y - 1);
            neighborDots[1] = new Point(x - 1, y);
            neighborDots[2] = new Point(x + 1, y - 1);
            neighborDots[3] = new Point(x + 1, y);
            neighborDots[4] = new Point(x, y - 1);
            neighborDots[5] = new Point(x, y + 1);
        } else {
            // 奇数行邻节点 上[x - 1, y] [x - 1, y + 1] 下[x+1, y] [x+1, y + 1]
            neighborDots[0] = new Point(x - 1, y);
            neighborDots[1] = new Point(x - 1, y + 1);
            neighborDots[2] = new Point(x + 1, y);
            neighborDots[3] = new Point(x + 1, y + 1);
            neighborDots[4] = new Point(x, y - 1);
            neighborDots[5] = new Point(x, y + 1);
        }
        return neighborDots;
    }

    public static boolean isValidPoint(int x, int y) {
        return x >= 0 && x < MaoMap.MAX && y >= 0 && y < MaoMap.MAX;
    }

    public static boolean isValidPoint(Point point) {
        if (point == null) return false;
        return isValidPoint(point.x, point.y);
    }

    public static boolean isBorderPoint(Point point) {
        return isBorderPoint(point.x, point.y);
    }

    public static boolean isBorderPoint(int x, int y) {
        return x == 0 || y == 0 || x == MaoMap.MAX - 1 || y == MaoMap.MAX - 1;
    }

    public static void main(String[] args) {
        MaoMap maoMap = new MaoMap();
        maoMap.randomMap();
        maoMap.printMap();
        maoMap.getNextStep();
    }
}
