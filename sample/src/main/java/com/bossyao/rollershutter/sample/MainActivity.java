package com.bossyao.rollershutter.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.bossyao.rollershutter.library.ContentView;
import com.bossyao.rollershutter.library.RollerShuttersView;


public class MainActivity extends ActionBarActivity {

    private ContentView mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        RollerShuttersView scrollView = (RollerShuttersView) findViewById(R.id.rollerShuttersView);

        mContentView = (ContentView) findViewById(R.id.ContentView);
        scrollView.setContentView(mContentView);
    }


}
