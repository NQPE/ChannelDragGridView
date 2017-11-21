package com.levi.channeldraggridview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.levi.channeldraggridview.R;
import com.levi.channeldraggridview.widget.ChannelDragGridView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by levi on 2017/11/21.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "TAG_MainActivity";
    ChannelDragGridView draggridview, other_draggridview;
    TextView tv_change, tv_add;
    List<Item> data = new ArrayList<>();
    List<Item> dataOther = new ArrayList<>();
    ChannelDragGridView.DragGridItemAdapter myAdapter,otherAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        loadData();
    }


    private void init() {
        draggridview = (ChannelDragGridView) findViewById(R.id.draggridview);
        other_draggridview = (ChannelDragGridView) findViewById(R.id.other_draggridview);
        tv_add = (TextView) findViewById(R.id.tv_add);
        tv_change = (TextView) findViewById(R.id.tv_change);
        tv_add.setOnClickListener(this);
        tv_change.setOnClickListener(this);
        myAdapter=new MyAdapter();
        otherAdapter=new OtherAdapter();
        draggridview.setNumColumns(3);
        other_draggridview.setNumColumns(3);
    }

    private void loadData() {
        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.title = "频道" + i;
            data.add(item);
        }
        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.title = "Other频道" + i;
            dataOther.add(item);
        }
        myAdapter.setData(data);
        otherAdapter.setData(dataOther);

        draggridview.setAdapter(myAdapter);
        other_draggridview.setAdapter(otherAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_change:
                Log.i(TAG, "onClick: tv_change");
                break;
        }
    }

    public static class Item implements Serializable {
        public String title;
    }

    public static class MyAdapter extends ChannelDragGridView.DragGridItemAdapter {


        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        //在外面先定义，ViewHolder静态类
        static class ViewHolder {
            public ImageView iv_del;
            public TextView tv_item;
        }

        //然后重写getView
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draggridview_test, null);
                holder.iv_del = (ImageView) convertView.findViewById(R.id.iv_del);
                holder.tv_item = (TextView) convertView.findViewById(R.id.tv_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_item.setText(((Item)getItem(position)).title);

            return convertView;
        }




    }
    public static class OtherAdapter extends ChannelDragGridView.DragGridItemAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        //在外面先定义，ViewHolder静态类
        static class ViewHolder {
            public ImageView iv_del;
            public TextView tv_item;
        }

        //然后重写getView
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draggridview_test, null);
                holder.iv_del = (ImageView) convertView.findViewById(R.id.iv_del);
                holder.tv_item = (TextView) convertView.findViewById(R.id.tv_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_item.setText(((Item)getItem(position)).title);

            return convertView;
        }


    }
}
