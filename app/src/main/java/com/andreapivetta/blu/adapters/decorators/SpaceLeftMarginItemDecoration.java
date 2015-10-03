package com.andreapivetta.blu.adapters.decorators;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceLeftMarginItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpaceLeftMarginItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = space;

        if (parent.getChildAdapterPosition(view) == 0)
            outRect.left = space;
    }
}
