package org.wong.enhancelistview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.wong.enhancelistview.view.DropDownListView;
import org.wong.enhancelistview.view.PullUpListView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<String> listItems;
    private MyListAdapter myListAdapter;
    private PullUpListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    /**
     * 初始化Activity
     */
    private void init() {
        listView = findViewById(R.id.lv_content);
        listItems = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            listItems.add("这是第" + (i + 1) + "条数据");
        }
        myListAdapter = new MyListAdapter();
        listView.setAdapter(myListAdapter);

        listView.setOnRefreshDataListener(new PullUpListView.OnRefreshDataListener() {
            @Override
            public void onRefresh() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        listItems.add("加载更新的数据");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listView.onRefreshComplete();
                                myListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }.start();
            }
        });
    }

    /**
     * 自定义的BaseAdapter.
     */
    class MyListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(parent.getContext());
            textView.setText(listItems.get(position));
            textView.setTextSize(24);
            return textView;
        }
    }
}
