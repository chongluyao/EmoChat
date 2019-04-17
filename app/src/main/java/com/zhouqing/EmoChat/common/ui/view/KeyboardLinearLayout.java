package com.zhouqing.EmoChat.common.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class KeyboardLinearLayout extends LinearLayout {
    private SoftKeyboardStatusChangeListener softKeyboardStatusChangeListener;

    public void setSoftKeyboardStatusChangeListener(SoftKeyboardStatusChangeListener softKeyboardStatusChangeListener) {
        this.softKeyboardStatusChangeListener = softKeyboardStatusChangeListener;
    }

    public KeyboardLinearLayout(Context context) {
        super(context);
    }

    public KeyboardLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedHeight) {
            Log.e("keyboard", "guess keyboard is shown");
            if(softKeyboardStatusChangeListener != null){
                softKeyboardStatusChangeListener.onKeyboardOpen();
            }
        } else {
            Log.e("keyboard", "guess keyboard has been hidden");
            if(softKeyboardStatusChangeListener != null){
                softKeyboardStatusChangeListener.onKeyboardClose();
            }
        }

    }
    public interface SoftKeyboardStatusChangeListener {
        void onKeyboardOpen();

        void onKeyboardClose();
    }
}
