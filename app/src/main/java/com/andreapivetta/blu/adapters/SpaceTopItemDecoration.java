package com.andreapivetta.blu.adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class SpaceTopItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpaceTopItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = space;

        if (parent.getChildAdapterPosition(view) == 0)
            outRect.top = space;
    }
}
