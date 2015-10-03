package com.andreapivetta.blu.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;


public class EditTextCursorWatcher extends EditText {

    private CursorWatcher cursorWatcher;
    private int lastStartCursorPosition = 1;

    public EditTextCursorWatcher(Context context) {
        super(context);
    }

    public EditTextCursorWatcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextCursorWatcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (cursorWatcher != null) {
            if (lastStartCursorPosition < (selStart - 1) || lastStartCursorPosition > (selStart + 1))
                cursorWatcher.onCursorPositionChanged(selStart, selEnd);
            lastStartCursorPosition = selStart;
        }
    }

    public void addCursorWatcher(CursorWatcher cursorWatcher) {
        this.cursorWatcher = cursorWatcher;
    }

    public interface CursorWatcher {
        void onCursorPositionChanged(int currentStartPosition, int currentEndPosition);
    }
}
