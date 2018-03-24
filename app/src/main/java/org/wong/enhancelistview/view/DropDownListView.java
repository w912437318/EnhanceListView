package org.wong.enhancelistview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.wong.enhancelistview.R;

import java.text.SimpleDateFormat;

/**
 * Created by wong on 2018/3/22.
 * 继承ListView，并增加其功能
 */

public class DropDownListView extends ListView {

    //ListView头布局相关属性
    private int paddingTop;
    private int headerView_height;
    private float offset;
    private View headerView;
    private float firstY;
    //控件对象
    private TextView tv_title;
    private TextView tv_time;
    private ProgressBar progressBar;
    private ImageView imageView;
    //下拉状态描述
    private final int PULL_REFRESH = 0;
    private final int RELEASE_REFRESH = 1;
    private final int REFRESHING = 2;
    private int currentRefreshState = PULL_REFRESH;
    //图片旋转动画
    private RotateAnimation rotateDownAnimation;
    private RotateAnimation rotateUpAnimation;
    //回调接口
    private OnRefreshListener onRefreshListener = null;

    public DropDownListView(Context context) {
        super(context);
        init();
    }

    public DropDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DropDownListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DropDownListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * 初始化，找到控件，设置进度条不可见
     * 调用初始化头布局方法
     */
    private void init() {
        setListViewHeaderView();
        tv_title = headerView.findViewById(R.id.header_tv_title);
        tv_time = headerView.findViewById(R.id.header_tv_time);
        progressBar = headerView.findViewById(R.id.header_pb_load);
        progressBar.setVisibility(GONE);
        imageView = headerView.findViewById(R.id.header_img_down);
        initAnimation();
    }

    //触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                headerView.setPadding(0, -headerView_height, 0, 0);
                resetHeaderView();
                firstY = event.getY();
                break;

            //手指移动
            case MotionEvent.ACTION_MOVE:
                //判断是否正在刷新数据，如果正在刷新则不对滑动作出响应
                if (currentRefreshState == REFRESHING) {
                    return super.onTouchEvent(event);
                }

                offset = event.getY() - firstY;
                if (offset > 0 && getFirstVisiblePosition() == 0) {
                    paddingTop = (int) (-headerView_height + offset);
                    headerView.setPadding(0, paddingTop, 0, 0);

                    if (paddingTop >= 0 && currentRefreshState != RELEASE_REFRESH) {
                        //下拉距离足够，进入释放刷新模式
                        currentRefreshState = RELEASE_REFRESH;
                        updateHeader();
                    } else if (paddingTop < 0 && currentRefreshState != PULL_REFRESH) {
                        //下拉距离不够
                        currentRefreshState = PULL_REFRESH;
                        updateHeader();
                    }
                    return true;
                }
                break;

            //手指抬起
            case MotionEvent.ACTION_UP:
                if (currentRefreshState == PULL_REFRESH) {
                    //不刷新，回收头布局
                    updateHeader();
                    headerView.setPadding(0, -headerView_height, 0, 0);
                } else if (currentRefreshState == RELEASE_REFRESH) {
                    //准备刷新状态
                    currentRefreshState = REFRESHING;
                    updateHeader();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 更新头布局，更改头布局状态
     */
    private void updateHeader() {
        switch (currentRefreshState) {
            case PULL_REFRESH:
                //回收头布局
                resetHeaderView();
                break;
            case RELEASE_REFRESH:
                //更新头布局为准备刷新模式
                imageView.startAnimation(rotateUpAnimation);
                tv_title.setText("释放刷新");
                break;
            case REFRESHING:
                //更新数据
                refreshDate();
                break;
            default:
                break;
        }
    }

    /**
     * 动画初始化
     */
    private void initAnimation() {
        //相对于自身旋转-180度
        rotateUpAnimation = new RotateAnimation(
                0f, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        //动画时长
        rotateUpAnimation.setDuration(200);
        rotateUpAnimation.setFillAfter(true);

        //将图标从-180度的位置旋转到0度，回归原位
        rotateDownAnimation = new RotateAnimation(
                -180f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateDownAnimation.setDuration(200);
        rotateDownAnimation.setFillAfter(true);
    }

    /**
     * 设置头布局
     */
    private void setListViewHeaderView() {
        headerView = View.inflate(getContext(), R.layout.listview_header, null);
        //提前调用View的measure方法进行控件尺寸的测量
        headerView.measure(0, 0);
        headerView_height = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerView_height, 0, 0);
        addHeaderView(headerView);
    }

    private void refreshDate() {
        headerView.setPadding(0, 0, 0, 0);
        imageView.clearAnimation();
        imageView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        tv_title.setText("正在刷新");
        onRefreshListener.onRefresh();
    }

    /**
     * 重置头布局文件状态：
     * 清除动画，隐藏进度条，将头布局重置
     */
    private void resetHeaderView() {
        tv_title.setText("下拉刷新");
        imageView.clearAnimation();
        imageView.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        currentRefreshState = PULL_REFRESH;
    }

    /**
     * 设置刷新数据监听，当用户刷新数据是会调用onRefresh放啊
     * 自行定义数据更新代码
     *
     * @param onRefreshListener onRefreshListener对象
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void refreshComplete() {
        headerView.setPadding(0, -headerView_height, 0, 0);
        tv_time.setText("最后刷新时间：" + getCurrentTime());
        resetHeaderView();
    }

    /**
     * OnRefreshListener接口，用户可以创建接口对象传入监听方法中
     * 实现本控件的数据刷新监听
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    private String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(System.currentTimeMillis());
    }
}
