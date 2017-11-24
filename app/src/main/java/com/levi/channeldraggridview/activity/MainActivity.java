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
import android.widget.Toast;

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
        myAdapter=new MyAdapter(draggridview);
        otherAdapter=new OtherAdapter(other_draggridview);
        draggridview.setNumColumns(3);
        other_draggridview.setNumColumns(3);
        draggridview.setDragEnable(false);
        draggridview.setAdapter(myAdapter);
        other_draggridview.setAdapter(otherAdapter);
    }

    private void loadData() {
        for (int i = 0; i < 15; i++) {
            Item item = new Item();
            item.title = "频道" + i;
            data.add(item);
        }
//        for (int i = 0; i < 20; i++) {
//            Item item = new Item();
//            item.title = "Other频道" + i;
//            dataOther.add(item);
//        }
        myAdapter.setData(data);
        otherAdapter.setData(dataOther);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_change:
                Log.i(TAG, "onClick: tv_change");
                draggridview.setDragEnable(!draggridview.getDragEnable());
                break;
            case R.id.tv_add:
                Log.i(TAG, "onClick: tv_add");
                Item item=new Item();
                item.title="频道"+myAdapter.getData().size();
                draggridview.addItem(item);
                break;
        }
    }

    public static class Item implements Serializable {
        public String title;
    }

    public static class MyAdapter extends ChannelDragGridView.DragGridItemAdapter {

        public MyAdapter(ChannelDragGridView gridview) {
            super(gridview);
        }

        @Override
        public void onStartSelectItem(ChannelDragGridView channelDragGridView, int index, View itemDragView) {

        }

        @Override
        public void onEndSelectItem(ChannelDragGridView channelDragGridView, int index, View itemDragView) {

        }

        @Override
        public void onDragEnableListener(boolean dragEnable) {

        }

        @Override
        public View onBindView(final ChannelDragGridView channelDragGridView, final boolean dragEnable,View itemView, Object itemData, int state) {
            if (itemView==null){
                itemView = LayoutInflater.from(channelDragGridView.getContext()).inflate(R.layout.item_draggridview_test, null);
            }
            Log.i(TAG, "onBindView: "+((Item)itemData).title);
            ImageView iv_del = (ImageView) itemView.findViewById(R.id.iv_del);
            final TextView tv_item = (TextView) itemView.findViewById(R.id.tv_item);
            iv_del.setVisibility(dragEnable?View.VISIBLE:View.INVISIBLE);
            tv_item.setText(((Item)itemData).title);
            final View finalItemView = itemView;
            tv_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dragEnable){
                        getChannelDragGridView().removeItemAnim(finalItemView,500,1000);
                        return;
                    }
                    Toast.makeText(v.getContext(),tv_item.getText(),Toast.LENGTH_SHORT).show();
                }
            });
            tv_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!dragEnable)channelDragGridView.setDragEnable(true);
                    return true;
                }
            });
            return itemView;
        }

    }
    public static class OtherAdapter extends ChannelDragGridView.DragGridItemAdapter {

        public OtherAdapter(ChannelDragGridView gridview) {
            super(gridview);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public void onStartSelectItem(ChannelDragGridView channelDragGridView, int index, View itemDragView) {

        }

        @Override
        public void onEndSelectItem(ChannelDragGridView channelDragGridView, int index, View itemDragView) {

        }

        @Override
        public void onDragEnableListener(boolean dragEnable) {

        }

        @Override
        public View onBindView(ChannelDragGridView channelDragGridView, boolean dragEnable,View itemView, Object itemData, int state) {
            return null;
        }

        //在外面先定义，ViewHolder静态类
        static class ViewHolder {
            public ImageView iv_del;
            public TextView tv_item;
        }

        //然后重写getView
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.i(TAG, "OtherAdapter getView position=="+position);
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
