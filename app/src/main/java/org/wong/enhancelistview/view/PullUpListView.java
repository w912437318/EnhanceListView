package org.wong.enhancelistview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.wong.enhancelistview.R;

/**
 * Created by wong on 2018/3/24.
 * 上拉加载更多功能的ListView
 */

public class PullUpListView extends ListView implements AbsListView.OnScrollListener {

    // 脚布局相关参数
    private View mFooterView;
    private int mFooterView_height;
    private boolean isRefreshing = false;
    //回调接口对象
    private OnRefreshDataListener onRefreshDataListener = null;

    public PullUpListView(Context context) {
        super(context);
        init();
    }

    public PullUpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullUpListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化ListView
     */
    private void init() {
        // 添加脚布局
        mFooterView = View.inflate(getContext(), R.layout.listview_footer, null);
        addFooterView(mFooterView);
        mFooterView.measure(0, 0);
        mFooterView_height = mFooterView.getMeasuredHeight();
        resetFooterView();
        setOnScrollListener(this);
    }

    /**
     * 重置脚布局为初始化状态
     */
    private void resetFooterView() {
        mFooterView.setPadding(0, -mFooterView_height, 0, 0);
    }

    public void setOnRefreshDataListener(OnRefreshDataListener onRefreshDataListener) {
        this.onRefreshDataListener = onRefreshDataListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (!isRefreshing) {
            if (scrollState == SCROLL_STATE_TOUCH_SCROLL && getLastVisiblePosition() >= getCount() - 1) {
                isRefreshing = true;
                mFooterView.setPadding(0, 0, 0, 0);
                setSelection(getCount() - 1);
                if (onRefreshDataListener != null) {
                    onRefreshDataListener.onRefresh();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public interface OnRefreshDataListener {
        void onRefresh();
    }

    public void onRefreshComplete() {
        resetFooterView();
        isRefreshing = false;
    }
}
