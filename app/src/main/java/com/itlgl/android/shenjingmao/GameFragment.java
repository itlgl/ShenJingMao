package com.itlgl.android.shenjingmao;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.itlgl.android.shenjingmao.algorithm.Constants;
import com.itlgl.android.shenjingmao.algorithm.Point;
import com.itlgl.android.shenjingmao.databinding.FragmentGameBinding;

import java.util.List;

/**
 * @author guanliang on 2021/7/6
 */
public class GameFragment extends Fragment implements GameContract.View {

    private FragmentGameBinding mBinding;
    private GameContract.Presenter mPresenter;
    private int mMax = Constants.MAX_DEFAULT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new GamePresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentGameBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @SuppressLint("NonConstantResourceId")
    private void initView() {
        // 当弹框显示时，拦截gameview点击事件
        View.OnClickListener layoutClickListener = v -> {
        };
        mBinding.layoutInner.layoutStart.setOnClickListener(layoutClickListener);
        mBinding.layoutInner.layoutSuccess.setOnClickListener(layoutClickListener);
        mBinding.layoutInner.layoutFail.setOnClickListener(layoutClickListener);

        mBinding.layoutInner.ivStart.setOnClickListener(v -> {
            hideAllDialogView();
            mPresenter.startGame(mMax);
        });

        // success view
        mBinding.layoutInner.viewSuccessShare.setOnClickListener(v -> {
            Toast.makeText(getContext(), R.string.game_share_tip, Toast.LENGTH_SHORT).show();
        });
        mBinding.layoutInner.viewSuccessReplay.setOnClickListener(v -> {
            hideAllDialogView();
            mPresenter.startGame(mMax);
        });

        // fail view
        mBinding.layoutInner.viewFailShare.setOnClickListener(v -> {
            Toast.makeText(getContext(), R.string.game_share_tip, Toast.LENGTH_SHORT).show();
        });
        mBinding.layoutInner.viewFailReplay.setOnClickListener(v -> {
            hideAllDialogView();
            mPresenter.startGame(mMax);
        });

        mBinding.layoutInner.gameView.setDotClickListener((x, y) -> {
            long start = System.currentTimeMillis();
            mPresenter.userInput(x, y);
            long end = System.currentTimeMillis();
            System.out.println("userInput cost " + (end - start));
//            mBinding.layoutInner.gameView.setCatPosition(x, y);
        });

        mBinding.layoutInner.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_normal:
                    mMax = Constants.MAX_NORMAL;
                    hideAllDialogView();
                    mBinding.layoutInner.gameView.setDotCount(mMax);
                    mPresenter.startGame(mMax);
                    break;
                case R.id.menu_small:
                    mMax = Constants.MAX_SMALL;
                    hideAllDialogView();
                    mBinding.layoutInner.gameView.setDotCount(mMax);
                    mPresenter.startGame(mMax);
                    break;
                case R.id.menu_middle:
                    mMax = Constants.MAX_MIDDLE;
                    hideAllDialogView();
                    mBinding.layoutInner.gameView.setDotCount(mMax);
                    mPresenter.startGame(mMax);
                    break;
                case R.id.menu_hard:
                    mMax = Constants.MAX_HARD;
                    hideAllDialogView();
                    mBinding.layoutInner.gameView.setDotCount(mMax);
                    mPresenter.startGame(mMax);
                    break;
            }


            return true;
        });
    }

    private void hideAllDialogView() {
        mBinding.layoutInner.layoutStart.setVisibility(View.GONE);
        mBinding.layoutInner.layoutSuccess.setVisibility(View.GONE);
        mBinding.layoutInner.layoutFail.setVisibility(View.GONE);
    }

    private void showStartView() {
        mBinding.layoutInner.layoutStart.setVisibility(View.VISIBLE);
        mBinding.layoutInner.layoutSuccess.setVisibility(View.GONE);
        mBinding.layoutInner.layoutFail.setVisibility(View.GONE);
    }

    private void showSuccessView(int totalStep) {
        mBinding.layoutInner.layoutStart.setVisibility(View.GONE);
        mBinding.layoutInner.layoutSuccess.setVisibility(View.VISIBLE);
        mBinding.layoutInner.layoutFail.setVisibility(View.GONE);
    }

    private void showFailView() {
        mBinding.layoutInner.layoutStart.setVisibility(View.GONE);
        mBinding.layoutInner.layoutSuccess.setVisibility(View.GONE);
        mBinding.layoutInner.layoutFail.setVisibility(View.VISIBLE);
    }

    @Override
    public void handleGameStart(List<Point> dotWalls, Point crazyCat) {
        mBinding.layoutInner.gameView.setDotsSelected(dotWalls);
        if (crazyCat == null) {
            crazyCat = new Point(4, 4);
        }
        mBinding.layoutInner.gameView.setCatPosition(crazyCat.x, crazyCat.y);
        mBinding.layoutInner.gameView.setCatState(false);
        mBinding.layoutInner.gameView.setCatVisible(true);
    }

    @Override
    public void handleGameFail() {
        showFailView();
    }

    @Override
    public void handleGamePass(int totalStep) {
        String[] game_success_title_1 = getResources().getStringArray(R.array.game_success_title_1);
        String[] game_success_title_2 = getResources().getStringArray(R.array.game_success_title_2);

        String text1 = String.valueOf(totalStep);
        String text2 = String.valueOf((int) (100 * totalStep + Math.random() * totalStep * 5));
        String text3 = (100 - (int) (totalStep * Math.random())) + "%";
        String text4 = totalStep < game_success_title_1.length ?
                game_success_title_1[totalStep] :
                game_success_title_2[(int) (Math.random() * game_success_title_2.length)];

        mBinding.layoutInner.tvSuccess1.setText(getResources().getString(R.string.game_success_text_1, text1));
        mBinding.layoutInner.tvSuccess2.setText(getResources().getString(R.string.game_success_text_2, text2));
        mBinding.layoutInner.tvSuccess3.setText(getResources().getString(R.string.game_success_text_3, text3));
        mBinding.layoutInner.tvSuccess4.setText(getResources().getString(R.string.game_success_text_4, text4));
        showSuccessView(totalStep);
    }

    @Override
    public void handleCatSurrounded() {
        mBinding.layoutInner.gameView.setCatState(true);
    }

    @Override
    public void handleCatNextStep(int x, int y) {
        mBinding.layoutInner.gameView.setCatPosition(x, y);
    }
}
