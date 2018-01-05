package com.jia.xunfeidemo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Description: 做笔记TextView
 * Created by jia on 2017/11/9.
 * 人之所以能，是相信能
 */
public class NoteTextView extends TextView {

    private static final String TAG = "NoteTextView";

    private HashMap<String, Drawable> imgs = new HashMap<>();

    private NetWorkImageGetter mNetWorkImageGetter = new NetWorkImageGetter();

    private int img_num = 0;

    private String text;


    private Rect mRect;
    private Paint mPaint;
    private int mColor = 0xFFFFA200;
    private float density;
    private float mStrokeWidth;

    // 笔记白点
    private Paint mPointPaint;

    // 开始各结束位置索引,startIndex必须大于等于endIndex
    private int startIndex = 300;
    private int endIndex = 520;

    private int off; //字符串的偏移值

    float x_start, x_stop, x_diff;
    int baseline;

    private float notePointX, notePointY;

    private int scrollY=0;

    private List<TextIndex> indexs = new ArrayList<>();
    private List<TextIndex> drawIndexs = new ArrayList<>();

    private Handler handler = new Handler();

    public NoteTextView(Context context) {
        super(context);
        init();
    }

    public NoteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        //获取屏幕密度
        density = getResources().getDisplayMetrics().density;

        mStrokeWidth = density;

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);

        mPointPaint = new Paint();
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointPaint.setColor(Color.WHITE);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStrokeWidth(2.2f);


    }

    public void fromHtml(String text) {

        this.text = text;

        setText(Html.fromHtml(this.text, mNetWorkImageGetter, new Html.TagHandler() {
            @Override
            public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {
                if (s.equals("img")) {
                    img_num++;
                }
            }
        }));

        // 没有图片直接加载
        if (img_num == 0) {
            setText();
        }


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //得到TextView显示有多少行
        int count = getLineCount();

        //得到TextView的布局
        final Layout layout = getLayout();

        // 初始化索引序列
        indexs.clear();
        for (int i = 0; i < count; i++) {
            TextIndex index = new TextIndex(i, layout.getLineStart(i), layout.getLineEnd(i));
            indexs.add(index);
        }

        boolean hasStart = false;

        for (int i = 0; i < indexs.size(); i++) {
            // 先确定开始位置
            if (startIndex >= indexs.get(i).start && startIndex <= indexs.get(i).end) {

                // 在确定结束位置
                if (endIndex >= indexs.get(i).start && endIndex <= indexs.get(i).end) {

                    drawIndexs.add(new TextIndex(i, startIndex, endIndex));
                    break;

                } else {

                    // 结束位置不再此行的话,先记下起始位置,结束位置为本行最后一位
                    drawIndexs.add(new TextIndex(i, startIndex, indexs.get(i).end));
                    hasStart = true;
                    continue;
                }


            } else {

                if (endIndex >= indexs.get(i).start && endIndex <= indexs.get(i).end) {

                    drawIndexs.add(new TextIndex(i, indexs.get(i).start, endIndex));
                    hasStart = false;
                    break;

                    // 否则此行全画
                } else {
                    if (hasStart) {
                        drawIndexs.add(new TextIndex(i, indexs.get(i).start, indexs.get(i).end));
                    }


                }

            }

        }


        for (int i = 0; i < drawIndexs.size(); i++) {

            // getLineBounds得到这一行的外包矩形,
            // 这个字符的顶部Y坐标就是rect的top 底部Y坐标就是rect的bottom
            baseline = getLineBounds(drawIndexs.get(i).line, mRect);

            //要得到这个字符的左边X坐标 用layout.getPrimaryHorizontal
            //得到字符的右边X坐标用layout.getSecondaryHorizontal
            x_start = layout.getPrimaryHorizontal(drawIndexs.get(i).start);
            x_diff = layout.getPrimaryHorizontal(drawIndexs.get(i).start + 1) - x_start;
            x_stop = layout.getPrimaryHorizontal(drawIndexs.get(i).end - 1) + x_diff;
            canvas.drawLine(x_start, baseline + mStrokeWidth + 8, x_stop, baseline + mStrokeWidth + 8, mPaint);


            /**
             * 在最后位置绘制椭圆和三个白点
             * 注意这里的所有值都不能给死，否则无法适配
             */
            if (i == drawIndexs.size() - 1) {
                canvas.drawCircle(x_stop + mStrokeWidth * 4, baseline + mStrokeWidth + 8, mStrokeWidth * 4, mPaint);
                notePointX = x_stop + mStrokeWidth * 4;
                notePointY = baseline + mStrokeWidth + 8;
                Log.e(TAG, "onDraw: x=" + (x_stop + mStrokeWidth * 4) + "y=" + (baseline + mStrokeWidth + 8));
                float[] pts = {x_stop + mStrokeWidth * 2, baseline + mStrokeWidth + 8, x_stop + mStrokeWidth * 4, baseline + mStrokeWidth + 8, x_stop + mStrokeWidth * 6, baseline + mStrokeWidth + 8};
                canvas.drawPoints(pts, mPointPaint);
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Layout layout = getLayout();
        int line = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent: " + event.getX() + "   " + event.getY());
                Log.e(TAG, "onTouchEvent: " + event.getX() + "   " + getScrollY() );

                if (Math.abs(event.getX() -notePointX)<=30 && Math.abs(event.getY() -notePointY)<=30) {
                    JsPopupWindow popWindow = new JsPopupWindow.Builder()
                            .setContentViewId(R.layout.dialog_popupwindow) // 设置布局
                            .setContext(getContext()) // 设置上下文
                            .setOutSideCancle(true) // 点击外部消失
                            .setHeight(LinearLayout.LayoutParams.WRAP_CONTENT) // 设置高度
                            .setWidth(LinearLayout.LayoutParams.WRAP_CONTENT) // 设置宽度
                            .setAnimation(R.style.anim_pop) // 设置动画
                            .build() // 构建
                            .showAtLocation(this, Gravity.TOP | Gravity.LEFT, (int) notePointX, (int) notePointY-scrollY);

                    TextView tv_pop = (TextView) popWindow.getItemView(R.id.tv_pop);
                    tv_pop.setText("我爱北京天安门，天安门上太阳升");
                }


                line = layout.getLineForVertical(getScrollY() + (int) event.getY());
                off = layout.getOffsetForHorizontal(line, (int) event.getX());
                if (Math.abs(off - endIndex - 10) <= 15) {

//                    JsPopupWindow popWindow = new JsPopupWindow.Builder()
//                            .setContentViewId(R.layout.dialog_popupwindow) // 设置布局
//                            .setContext(getContext()) // 设置上下文
//                            .setOutSideCancle(true) // 点击外部消失
//                            .setHeight(LinearLayout.LayoutParams.WRAP_CONTENT) // 设置高度
//                            .setWidth(LinearLayout.LayoutParams.WRAP_CONTENT) // 设置宽度
//                            .setAnimation(R.style.anim_pop) // 设置动画
//                            .build() // 构建
//                            .showAtLocation(this, Gravity.TOP | Gravity.LEFT, (int) notePointX, (int) notePointY);
//
//                    TextView tv_pop = (TextView) popWindow.getItemView(R.id.tv_pop);
//                    tv_pop.setText("我爱北京天安门，天安门上太阳升");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                line = layout.getLineForVertical(getScrollY() + (int) event.getY());
                int curOff = layout.getOffsetForHorizontal(line, (int) event.getX());

                break;
        }
        return true;
    }


    public void setMScrollY(int scrollY) {
        this.scrollY = scrollY;
        Log.e(TAG, "setMScrollY: "+this.scrollY );
    }

    class TextIndex {
        int line;
        int start;
        int end;

        public TextIndex(int line, int start, int end) {
            this.line = line;
            this.start = start;
            this.end = end;
        }
    }


    class NetWorkImageGetter implements Html.ImageGetter {

        @Override
        public Drawable getDrawable(final String source) {

            if (imgs.containsKey(source)) {
                imgs.get(source).setBounds(0, 0, imgs.get(source).getIntrinsicWidth() * 2,
                        imgs.get(source).getIntrinsicHeight() * 2);
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        imgs.put(source, new BitmapDrawable(getbitmap(source)));

                        if (imgs.size() == img_num) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setText();
                                }
                            });
                        }
                    }
                }).start();
            }

            return imgs.get(source);
        }

    }

    private void setText() {

        setText(Html.fromHtml(text, mNetWorkImageGetter, null));

    }


    /**
     * 根据一个网络连接(String)获取bitmap图像
     *
     * @param imageUri
     * @return
     */
    public static Bitmap getbitmap(String imageUri) {

        // 显示网络上的图片
        Bitmap bitmap = null;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            bitmap = null;
        } catch (IOException e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }

    @Override
    protected boolean getDefaultEditable() {//禁止EditText被编辑
        return false;
    }


    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return super.getDefaultMovementMethod();
    }

}
