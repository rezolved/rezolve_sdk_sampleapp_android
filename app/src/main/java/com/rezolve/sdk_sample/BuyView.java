package com.rezolve.sdk_sample;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class BuyView implements View.OnTouchListener {

    private final float SLIDER_DISABLED_ALPHA = 0.7f;
    private final float SLIDER_ENABLED_ALPHA = 1.0f;

    public interface SlideToBuyListener {
        void onSlideToBuySuccess();
    }

    private LinearLayout rootView;
    private SlideToBuyListener listener;
    private View head;
    private boolean wasSlideToBuyAnimated = false;

    private float dX = 0;

    public BuyView(View parentView, SlideToBuyListener listener) {
        this.listener = listener;
        this.rootView = parentView.findViewById(R.id.buy_view);
        this.head = parentView.findViewById(R.id.slide_to_buy_head);
    }

    public void setEnabled(boolean value) {
        if (value) {
            head.setOnTouchListener(this);
            rootView.setAlpha(SLIDER_ENABLED_ALPHA);
            head.setAlpha(SLIDER_ENABLED_ALPHA);
        } else {
            head.setOnTouchListener(null);
            rootView.setAlpha(SLIDER_DISABLED_ALPHA);
            head.setAlpha(SLIDER_DISABLED_ALPHA);
        }
    }

    public void setVisible(boolean value) {
        rootView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int parentWidth = ((View)v.getParent()).getWidth();
        int i = v.getId();
        if (i == R.id.slide_to_buy_head) {
            if (action == MotionEvent.ACTION_DOWN) {
                dX = v.getX() - event.getRawX();
                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {

                float newX = event.getRawX() + dX;

                if (newX > 0 && newX + v.getWidth() < parentWidth) {
                    v.animate().x(newX).setDuration(0).start();
                } else {
                    if (newX <= 0) {
                        v.animate().x(0).setDuration(0).start();
                    } else {
                        v.animate().x(parentWidth - v.getWidth()).setDuration(0).start();
                    }
                }
                return true;
            } else if (action == MotionEvent.ACTION_UP) {
                boolean success = false;
                if (v.getX() >= parentWidth - v.getWidth() - 5) {
                    if (listener != null) {
                        success = true;
                        listener.onSlideToBuySuccess();
                    }
                    v.animate().x(0).setDuration(200).start();
                }

                if (!success) {
                    v.animate().x(0).setDuration(200).start();
                }
                return false;
            } else if (action == MotionEvent.ACTION_CANCEL) {
                if (head != null && head.getX() > 0) {
                    head.animate().x(0).setDuration(200).start();
                }
            }

        }
        return false;
    }
}
