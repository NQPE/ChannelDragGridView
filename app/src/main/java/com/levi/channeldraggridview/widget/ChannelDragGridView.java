package com.levi.channeldraggridview.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by levi on 2017/11/21.
 * 频道拖动的gridview
 */
public class ChannelDragGridView extends GridView {
    public static final String TAG = "TAG_ChannelDragGridView";
    /**
     * item处于可滑动状态 但是未滑动
     */
    public static final int STATE_DRAG_ENABLE = 1000;
    /**
     * item不可滑动
     */
    public static final int STATE_DRAG_DISABLE =1001;
    /**
     * item处于正在滑动状态
     */
    public static final int STATE_DRAG_ING = 1002;


    @IntDef({STATE_DRAG_ENABLE, STATE_DRAG_DISABLE, STATE_DRAG_ING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemViewState {

    }


    public ChannelDragGridView(Context context) {
        this(context,null);
    }

    public ChannelDragGridView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ChannelDragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //为了解决gradview放在scrollview中的嵌套滑动问题
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }


    public abstract static class DragGridItemAdapter<T> implements ListAdapter{
        protected List data = new ArrayList();
        public void setData(List data) {
            this.data = data;
        }
        public List getData(){
            return data;
        }
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public CharSequence[] getAutofillOptions() {
            return new CharSequence[0];
        }
    }
}
