package com.levi.channeldraggridview.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * 未知状态码
     */
    public static final int CODE_UNKNOW= -1;


    //最小滑动距离
    int mTouchSlop;


    DragingGridItem mDragingGridItem=new DragingGridItem();
    //整个viewgroup控件是否可拖动
    boolean mDragEnable = false;
    //记录down接触点的坐标
    float mDownX,mDownY;

    //设置前几个item是不能移动的
    int mDisableDragCount=0;

    //一个item的宽度
    int mItemWidth;
    //一个item的高度
    int mItemHeight;

    //拖动item与其他item重叠范围判断
    float mOverlapScale=1/8;

    /**
     * item挤压移动 动画持续时间
     * 默认200
     */
    int mMoveAnimDuration=200;

    /**
     * 包含view和data绑定的item容器
     *
     */
    List<ItemDragViewData> mItemDragViewDatas=new ArrayList<>();

    DragGridItemAdapter mAdapter;

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
        setChildrenDrawingOrderEnabled(true);
//        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setClipChildren(false);
        setClipToPadding(false);
        mTouchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow");
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Log.i(TAG,"addView(View child, int index, LayoutParams params)");
        super.addView(child, index, params);
    }

    @Override
    public void removeViewAt(int index) {
        Log.i(TAG,"removeViewAt(int index)");
        super.removeViewAt(index);
    }

    @Override
    public void removeView(View view) {
        Log.i(TAG,"removeView(View view)");
        super.removeView(view);
    }

    @Override
    public void removeAllViews() {
        Log.i(TAG,"removeAllViews()");
        super.removeAllViews();
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        Log.i(TAG,"requestLayout()");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //为了解决gradview放在scrollview中的嵌套滑动问题
        //这样有个特别大的坏处就是 无法利用gridview的缓存机制了
        //因为这个设置相当于把gridview的所有item内容总高度算出来了
        //导致gridview的所有子item全部绘制出来 而不是正常的采用复用item缓存机制
        //但是鉴于这个是频道类的拖拽gridview 使用场景中 item不会很多 所以暂时采用这个机制
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
        Log.i(TAG, "onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.i(TAG, "onLayout");
        handleOnLayout();
    }

    /**
     * 主要是完成mItemDragViewDatas的信息绑定
     */
    private void handleOnLayout() {
        int childCount=getChildCount();
        if (childCount<=0)return;
        //只需要第一次执行创建操作就行
        if (mItemDragViewDatas.size()>0)return;
        mItemDragViewDatas.clear();
        for (int i=0;i<childCount;i++){

            ItemDragViewData itemDragViewData=new ItemDragViewData();
            itemDragViewData.itemView=getChildAt(i);
            itemDragViewData.itemState=mDisableDragCount>(i+1)?STATE_DRAG_DISABLE:STATE_DRAG_ENABLE;
            if (mAdapter!=null&&mAdapter.getData().size()==childCount){
                itemDragViewData.itemData=mAdapter.getData().get(i);
            }
            if (i==0){
                mItemHeight = itemDragViewData.itemView.getMeasuredHeight();
                mItemWidth = itemDragViewData.itemView.getMeasuredWidth();
                Log.i(TAG, "handleOnLayout: mItemWidth=="+mItemWidth);
                Log.i(TAG, "handleOnLayout: mItemHeight=="+mItemHeight);
            }
            Log.i(TAG, "handleOnLayout: i=="+i+"x=="+itemDragViewData.itemView.getLeft()+"y=="+itemDragViewData.itemView.getTop());
            mItemDragViewDatas.add(itemDragViewData);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return handleDispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return handleOnInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleOnTouchEvent(event);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
//        Log.i(TAG, "getChildDrawingOrder");
        //重写此方法  让选择的itemview 绘制顺序在最后
        // 方便移动时 覆盖到其他itemview的上方
        if (mDragingGridItem.mDragGridItemView == null) {
            return i;
        }
        if (i == indexOfChild(mDragingGridItem.mDragGridItemView)) {
            return childCount - 1;
        } else if (i == childCount - 1) {
            return indexOfChild(mDragingGridItem.mDragGridItemView);
        } else {
            return i;
        }
//        return super.getChildDrawingOrder(childCount, i);
    }

    /**
     * 处理gridview的分发事件
     * @param ev
     * @return
     */
    private boolean handleDispatchTouchEvent(MotionEvent ev) {
//        Log.i(TAG, "handleDispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }



    /**
     * 处理拦截事件
     *
     * @param ev
     * @return
     */
    private boolean handleOnInterceptTouchEvent(MotionEvent ev) {
        Log.i(TAG, "handleOnInterceptTouchEvent");
        if (!mDragEnable)return false;
        //获取action
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "handleOnInterceptTouchEvent ACTION_DOWN");
                final float x = ev.getX();
                final float y = ev.getY();
                findTopChildUnder((int) x, (int) y);
                if (checkDisableDrag(mDragingGridItem.mDragGridItemViewPosition)){
                    mDragingGridItem.mDragGridItemView=null;
                }
                if (mDragingGridItem.mDragGridItemView != null) {
                    if (getParent()!=null){
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    //重写刷新draw的缓存 好重新排序drwaing
                    invalidate();
                }
                mDownX=x;
                mDownY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "handleOnInterceptTouchEvent ACTION_MOVE");
                if (checkTouchSlop(ev.getX(),ev.getY())){
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * 根据x和y坐标和来找到触摸的draggriditem
     *
     * @param x
     * @param y
     * @return
     */
    private void findTopChildUnder(int x, int y) {
        final int childCount = mItemDragViewDatas.size();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = mItemDragViewDatas.get(i).itemView;
            if (x >= child.getLeft() && x < child.getRight() &&
                    y >= child.getTop() && y < child.getBottom()) {
                mDragingGridItem.mDragGridItemViewPosition=i;
                mDragingGridItem.mDragGridItemView =child;
                mDragingGridItem.left=getLeft();
                mDragingGridItem.top=getTop();
                return ;
            }
        }
        mDragingGridItem.mDragGridItemView=null;
        mDragingGridItem.mDragGridItemViewPosition=CODE_UNKNOW;
        return ;
    }

    /**
     * 验证view是否是disable状态
     * @param position
     * @return
     */
    private boolean checkDisableDrag(int position) {
        return mDisableDragCount>=position+1;
    }

    /**
     * 检查是否是超出最大距离
     * @param x
     * @param y
     * @return
     */
    private boolean checkTouchSlop(float x, float y) {
        return (Math.abs(y-mDownY)>mTouchSlop|| Math.abs(x-mDownX)>mTouchSlop);
    }

    /**
     * 处理触摸事件
     *
     * @param ev
     * @return
     */
    private boolean handleOnTouchEvent(MotionEvent ev) {
//        Log.i(TAG, "handleOnTouchEvent");
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                //这里-mItemWidth/2 主要是为了滑动的时候不是以左上角为起点
                //否则拖动的item的移动中心在左上角而不是中心点
                final float x = ev.getX()-mItemWidth/2;
                final float y = ev.getY()-mItemHeight/2;

                if (mDragingGridItem.mDragGridItemView != null) {
                    //拖拽至指定位置
                    dragTo(mDragingGridItem.mDragGridItemView.getLeft(), mDragingGridItem.mDragGridItemView.getTop(), (int) x, (int) y);
                    //处理挤压动画
                    handleMoveAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
//                handleUpAnimation();
                break;
        }

        return true;
    }

    /**
     * 移动view
     *
     * @param left
     * @param top
     * @param dx
     * @param dy
     */
    private void dragTo(int left, int top, int dx, int dy) {
        if (dx != 0) {
            //移动View
            ViewCompat.offsetLeftAndRight(mDragingGridItem.mDragGridItemView, dx - left);
        }
        if (dy != 0) {
            //移动View
            ViewCompat.offsetTopAndBottom(mDragingGridItem.mDragGridItemView, dy - top);
        }

    }

    /**
     * 用handle的消息队列处理机制来player挤压动画
     */
    Handler handlerMoveAnimation=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj instanceof AnimatorSet){
                ((AnimatorSet)msg.obj).start();
            }
        }
    };

    /**
     * 处理挤压的移动动画
     */
    private void handleMoveAnimation() {
        if (mDragingGridItem.mDragGridItemView == null) return;
        int overlapPosition = getOverlapPosition(mDragingGridItem.mDragGridItemView);
        if (overlapPosition==CODE_UNKNOW)return;
        if (overlapPosition==mDragingGridItem.mDragGridItemViewPosition)return;
        Log.i(TAG, "handleMoveAnimation: overlapPosition=="+overlapPosition);
        AnimatorSet animatorSet=createMoveAnimation(overlapPosition,mDragingGridItem.mDragGridItemViewPosition);
        handleMoveData(overlapPosition,mDragingGridItem.mDragGridItemViewPosition);
        Message msg=new Message();
        msg.obj=animatorSet;
        handlerMoveAnimation.sendMessageDelayed(msg,mMoveAnimDuration);
    }

    /**
     * 创建挤压动画
     * @param overlapPosition
     * @param itemPosition
     */
    private AnimatorSet createMoveAnimation(int overlapPosition, int itemPosition) {
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(mMoveAnimDuration);
        ArrayList<Animator> valueAnimators = new ArrayList<>();
        if (overlapPosition>itemPosition){
            for (int i=overlapPosition;i>itemPosition;i--){
                View itemview=mItemDragViewDatas.get(i).itemView;
                if (itemview==null)continue;
                ValueAnimator animator = creatValueAnimator(itemview,i,i-1);
                valueAnimators.add(animator);
            }
        }else {
            for (int i=overlapPosition;i<itemPosition;i++){
                View itemview=mItemDragViewDatas.get(i).itemView;
                if (itemview==null)continue;
                ValueAnimator animator = creatValueAnimator(itemview,i,i+1);
                valueAnimators.add(animator);
            }
        }
        animSet.playTogether(valueAnimators);
        return animSet;
    }

    /**
     * 处理move之后的数据
     *
     * @param overlapPosition
     * @param itemPosition
     */
    private void handleMoveData(int overlapPosition, int itemPosition) {
        mDragingGridItem.mDragGridItemViewPosition=overlapPosition;
        mItemDragViewDatas.add(overlapPosition,mItemDragViewDatas.remove(itemPosition));
    }

    /**
     *
     * @param itemview  需要移动的view
     * @param beforepos 当前item的position
     * @param afterpos  需要移动去的那个position
     * @return
     */
    private ValueAnimator creatValueAnimator(final View itemview, final int beforepos, int afterpos) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setTarget(itemview);
        int after_left=afterpos==mDragingGridItem.mDragGridItemViewPosition?mDragingGridItem.left:mItemDragViewDatas.get(afterpos).itemView.getLeft();
        int after_top=afterpos==mDragingGridItem.mDragGridItemViewPosition?mDragingGridItem.top:mItemDragViewDatas.get(afterpos).itemView.getTop();
        final int l = mItemDragViewDatas.get(beforepos).itemView.getLeft();
        final int offset_l =after_left- mItemDragViewDatas.get(beforepos).itemView.getLeft();
        final int t = mItemDragViewDatas.get(beforepos).itemView.getTop();
        final int offset_t =after_top-mItemDragViewDatas.get(beforepos).itemView.getTop();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int lc = (int) (l + offset_l * (float) animation.getAnimatedValue());
                int tc = (int) (t + offset_t * (float) animation.getAnimatedValue());
                itemview.layout(lc, tc, lc + mItemWidth, tc + mItemHeight);
            }
        });
        return animator;
    }

    /**
     * 采用itemview.layout来移动
     * @param itemview  需要移动的view
     * @param afterX  需要移动去的X
     * @param afterY  需要移动去的Y
     * @return
     */
    private ValueAnimator creatMoveAnimator(final View itemview, final int afterX, final int afterY) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(mMoveAnimDuration);
        animator.setTarget(itemview);
        final int l = itemview.getLeft();
        final int offset_l =afterX- l;
        final int t = itemview.getTop();
        final int offset_t = afterY-t;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int lc = (int) (l + offset_l * (float) animation.getAnimatedValue());
                int tc = (int) (t + offset_t * (float) animation.getAnimatedValue());
                itemview.layout(lc, tc, lc + mItemWidth, tc + mItemHeight);

            }
        });
        return animator;
    }

    /**
     *采用itemview.setTranslationX/Y来移动
     * @param itemview  需要移动的view
     * @param afterX  需要移动去的X
     * @param afterY  需要移动去的Y
     * @return
     */
    private ValueAnimator creatMoveAnimation(final View itemview, final int afterX, int afterY) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(360);
        animator.setTarget(itemview);
        final int l = itemview.getLeft();
        final int offset_l =afterX- l;
        final int t = itemview.getTop();
        final int offset_t = afterY-t;
        Log.i(TAG,"offset_l=="+offset_l);
        Log.i(TAG,"offset_t=="+offset_t);
        Log.i(TAG,"l=="+l);
        Log.i(TAG,"t=="+t);
        Log.i(TAG,"afterX=="+afterX);
        Log.i(TAG,"afterY=="+afterY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction= (float) animation.getAnimatedValue();
                itemview.setTranslationX(evaluateInt(animatedFraction, 0, offset_l));
                itemview.setTranslationY(evaluateInt(animatedFraction, 0, offset_t));
            }
        });
        return animator;
    }

    /**
     * 得到拖动的item移动到哪个position
     *
     * @param mDragGridItemView
     * @return
     */
    private int getOverlapPosition(View mDragGridItemView) {
        int position = CODE_UNKNOW;
        int dragitemcenterx = mDragGridItemView.getLeft() + mItemWidth / 2;
        int dragitemcentery = mDragGridItemView.getTop() + mItemHeight / 2;
        int lastPostion=mItemDragViewDatas.size()-1;
        for (int i=0;i<lastPostion+1;i++){
            if (dragitemcenterx >= mItemDragViewDatas.get(i).itemView.getLeft() + mItemWidth *mOverlapScale && dragitemcenterx < mItemDragViewDatas.get(i).itemView.getLeft()+ mItemWidth * (1-mOverlapScale) &&
                    dragitemcentery >= mItemDragViewDatas.get(i).itemView.getTop() + mItemHeight *mOverlapScale && dragitemcentery < mItemDragViewDatas.get(i).itemView.getTop() + mItemHeight * (1-mOverlapScale)) {
                position = i;
                break;
            }
        }

        if (position==CODE_UNKNOW){
            //超出下边界 也算是 覆盖在了最后一个item上
            if (dragitemcentery>mItemDragViewDatas.get(lastPostion).itemView.getTop()+mItemHeight){
                return lastPostion;
            }else if (dragitemcenterx>mItemDragViewDatas.get(lastPostion).itemView.getLeft()+mItemWidth&&
                    dragitemcentery>mItemDragViewDatas.get(lastPostion).itemView.getTop()){
                //在最后一个item的右边 也算是覆盖
                return lastPostion;
            }
        }

        //判断itemview state是否可拖动
        if (checkDisableDrag(position)){
            return CODE_UNKNOW;
        }

        return position;
    }
    /**=====================================公共方法start==================================*/

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof DragGridItemAdapter)){
            throw new RuntimeException("adapter must be DragGridItemAdapter class type");
        }
        super.setAdapter(adapter);
        this.mAdapter= (DragGridItemAdapter) adapter;
    }


    /**
     * 设置data排序的前N个item固定不能拖动
     *  注意 一定要在adapter setdata之前
     * @return
     */
    public void setDisableDragCount(int count) {
        this.mDisableDragCount=count;
    }

    /**
     * 返回data排序的前N个item固定不能拖动
     * @return
     */
    public int getDisableDragCount() {
        return mDisableDragCount;
    }

    /**
     * 整个viewgroup控件是否可拖动子view
     *
     * @param enable
     */
    public void setDragEnable(boolean enable){
        mDragEnable=enable;
//        handleOnDragEnableListener();
//        handleOnBindItemView();
//        handleDispatchTouchEvent();
    }


    /**
     * 得到是否是可拖动状态
     * @return
     */
    public boolean getDragEnable(){
        return mDragEnable;
    }


    /**=====================================公共方法end==================================*/




    /**=====================================内部类及接口start==================================*/

    public abstract static class DragGridItemAdapter<T> extends BaseAdapter{
        protected List data = new ArrayList();
        public void setData(List data) {
            this.data = data;
        }
        public List getData(){
            return data;
        }


        @Override
        public long getItemId(int i) {
            return i;
        }

    }

    /**
     * 依附于itemview上面的信息
     *
     * @param <T>
     */
    public static class ItemDragViewData<T> implements Serializable {
        //itemview
        public View itemView;

        //itemview状态
        public int itemState;

        //itemview对应的itemdata
        public T itemData;
    }

    /**
     * 正在拖动的DragingGridItem
     *
     */
    public class DragingGridItem implements Serializable {
        //拖动的itemview
        public View mDragGridItemView;
        //拖动的itemview 的position
        public int mDragGridItemViewPosition=CODE_UNKNOW;
        //原始位置的getleft
        public int left;
        //原始位置的gettop
        public int top;
    }

    /**
     * viewgroup控件在可拖动与不可拖动直接切换时触发的listener
     */
    public interface OnDragEnableListener{
        /**
         *
         * @param dragEnable
         */
        void onDragEnableListener(boolean dragEnable);
    }

    /**
     * 选中item的滑动状态的监听
     *
     */
    public interface OnItemDragListener{
        void onStartSelectItem(int dragitemindex);
        void onMoveItem(int dragitemindex,int targetindex);
        void onEndMoveItem(int dragitemindex,int targetindex);
    }
    /**=====================================内部类及接口end==================================*/

    /**==================================工具函数start=====================================*/

    /**
     * 获取屏幕的宽度（单位：px）
     *
     * @return 屏幕宽px
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(dm);// 给白纸设置宽高
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度（单位：px）
     *
     * @return 屏幕高px
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(dm);// 给白纸设置宽高
        return dm.heightPixels;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * Integer 估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static Integer evaluateInt(float fraction, Integer startValue, Integer endValue) {
        int startInt = startValue;
        return (int) (startInt + fraction * (endValue - startInt));
    }

/**=====================================工具函数end==================================*/
}
