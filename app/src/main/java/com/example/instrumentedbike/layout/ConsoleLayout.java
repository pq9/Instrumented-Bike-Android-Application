package com.example.instrumentedbike.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.instrumentedbike.R;



public class ConsoleLayout extends FrameLayout {
    private TextView consoleTitle;
    private Button consoleClear;
    private TextView consoleText;
    private NestedScrollView scrollView;


    public ConsoleLayout(Context context) {
        super(context);
    }

    public ConsoleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConsoleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConsoleLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.layout_console, this, true);
        consoleTitle = (TextView) findViewById(R.id.console_title);
        consoleClear = (Button) findViewById(R.id.console_clear);
        consoleText = (TextView) findViewById(R.id.console_text);
        scrollView = findViewById(R.id.scrollView);
        consoleClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearConsole();
            }
        });
    }

    public void clearConsole() {
        consoleText.setText("");
    }

    private final Handler handler = new Handler();

    public void addLog(String msg) {
        consoleText.append(msg + "\n");
        handler.post(new Runnable() {

            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
