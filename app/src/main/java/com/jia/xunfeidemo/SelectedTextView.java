package com.jia.xunfeidemo;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;


/**
 * Description:
 * Created by jia on 2017/10/28.
 * 人之所以能，是相信能
 */
public class SelectedTextView extends EditText{
    private int off;
    public SelectedTextView(Context context) {
        super(context);
        init();
    }

    public SelectedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SelectedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init(){
        setGravity(Gravity.TOP);
        this.setBackgroundColor(Color.WHITE);
    }
    @Override
    protected boolean getDefaultEditable() {
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int  action = event.getAction();
        Layout layout = getLayout();
        int  line =  0 ;
        switch (action) {
            case  MotionEvent.ACTION_DOWN:
                line = layout.getLineForVertical(getScrollY()+ (int )event.getY());
                off = layout.getOffsetForHorizontal(line, (int )event.getX());
                Selection.setSelection(getEditableText(), off);
                break ;
            case  MotionEvent.ACTION_MOVE:
            case  MotionEvent.ACTION_UP:
                line = layout.getLineForVertical(getScrollY()+(int )event.getY());
                int  curOff = layout.getOffsetForHorizontal(line, ( int )event.getX());
                Selection.setSelection(getEditableText(), off, curOff);
                break ;
        }
        return   true ;
    }


}
