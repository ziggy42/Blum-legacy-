package com.andreapivetta.blu.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.utilities.Common;


public class BorderedLinearLayout extends LinearLayout {

    private Paint strokePaint;
    private Rect r = new Rect();
    private RectF outline = new RectF();
    private float radius = Common.dpToPx(getContext(), 3);

    public BorderedLinearLayout(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public BorderedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        init(attrs);
    }

    public BorderedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.BorderedLinearLayout, 0, 0);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(a.getColor(R.styleable.BorderedLinearLayout_borderColor,
                getResources().getColor(R.color.blueThemeColorPrimary)));
        strokePaint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(r);
        outline.set(1, 1, r.right - 1, r.bottom - 1);
        canvas.drawRoundRect(outline, radius,  radius, strokePaint);
    }
}
