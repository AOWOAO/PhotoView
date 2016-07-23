package me.itangqi.photoview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;

import me.itangqi.module.photoview.PagerPhotoViewActivity;
import me.itangqi.module.photoview.SinglePhotoViewActivity;

public class MainActivity extends AppCompatActivity {
    private Button mBtnViewSingle;
    private Button mBtnViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnViewSingle = (Button)findViewById(R.id.btn_single);
        mBtnViewPager = (Button)findViewById(R.id.btn_pager);
        mBtnViewSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String photoViewURL = "http://ww2.sinaimg.cn/large/610dc034jw1f5z2eko5xnj20f00miq5s.jpg";
                SinglePhotoViewActivity.startPhotoViewSingle(MainActivity.this, photoViewURL);
            }
        });
        mBtnViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> photoViewURLArryList = new ArrayList(Arrays.asList("http://ww4.sinaimg.cn/large/610dc034jw1f5ufyzg2ajj20ck0kuq5e.jpg",
                        "http://ww4.sinaimg.cn/large/610dc034jw1f5xwnxj2vmj20dw0dwjsc.jpg",
                        "http://ww3.sinaimg.cn/large/610dc034jw1f5t889dhpoj20f00mi414.jpg",
                        "http://ww2.sinaimg.cn/large/610dc034jw1f5s5382uokj20fk0ncmyt.jpg"));
                PagerPhotoViewActivity.startPhotoViewPager(MainActivity.this, photoViewURLArryList);
            }
        });
    }
}
