package com.levi.channeldraggridview.activity;

import android.animation.Animator;
import android.graphics.Point;
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
        draggridview.setNumColumns(3);
        draggridview.setDragEnable(false);
        draggridview.setAdapter(myAdapter);

        otherAdapter=new OtherAdapter(other_draggridview);
        other_draggridview.setNumColumns(3);
        other_draggridview.setDragEnable(false);
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

    public class MyAdapter extends ChannelDragGridView.DragGridItemAdapter {

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
        public View onBindView(final ChannelDragGridView channelDragGridView, final boolean dragEnable, View itemView, final Object itemData, int state) {
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
                        Point point=other_draggridview.getLastPlusOnePoint();
                        getChannelDragGridView().removeItemAnim(finalItemView, point.x, point.y, new ChannelDragGridView.OnAnimatorListener() {
                            @Override
                            public void onAnimatorEndListener(Animator animation) {
                                other_draggridview.addItem(itemData);
                            }
                        });
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

    public class OtherAdapter extends ChannelDragGridView.DragGridItemAdapter {

        public OtherAdapter(ChannelDragGridView gridview) {
            super(gridview);
        }

        @Override
        public void onStartSelectItem(ChannelDragGridView channelDragGridView, int index, View itemDragView) {

        }

        @Override
        public void onEndSelectItem(ChannelDragGridView channelDragGridView, int index, View itemDragView) {

        }

        @Override
        public View onBindView(final ChannelDragGridView channelDragGridView, final boolean dragEnable, View itemView, final Object itemData, int state) {
            if (itemView==null){
                itemView = LayoutInflater.from(channelDragGridView.getContext()).inflate(R.layout.item_draggridview_test, null);
            }
            Log.i(TAG, "OtherAdapter onBindView: "+((Item)itemData).title);
            ImageView iv_del = (ImageView) itemView.findViewById(R.id.iv_del);
            final TextView tv_item = (TextView) itemView.findViewById(R.id.tv_item);
            iv_del.setVisibility(dragEnable?View.VISIBLE:View.INVISIBLE);
            tv_item.setText(((Item)itemData).title);
            final View finalItemView = itemView;
            tv_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Point point=draggridview.getLastPlusOnePoint();
                    getChannelDragGridView().removeItemAnim(finalItemView, point.x, point.y, new ChannelDragGridView.OnAnimatorListener() {
                        @Override
                        public void onAnimatorEndListener(Animator animation) {
                            draggridview.addItem(itemData);
                        }
                    });
                    Toast.makeText(v.getContext(),tv_item.getText(),Toast.LENGTH_SHORT).show();
                }
            });
            return itemView;
        }

    }
}
