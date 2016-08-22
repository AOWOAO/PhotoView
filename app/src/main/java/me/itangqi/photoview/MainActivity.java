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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mBtnViewSingle = (Button) findViewById(R.id.btn_single);
        Button mBtnViewPager = (Button) findViewById(R.id.btn_pager);
        mBtnViewSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String photoViewURL = "http://img2.yytcdn.com/artist/fan/150812/0/-M-0e9a280baa1d87f73e7d91db57cffa22_0x0.jpg";
                // 跳转 Single view
                SinglePhotoViewActivity.startSinglePhotoView(MainActivity.this, photoViewURL, System.currentTimeMillis() + ".jpg", "PhotoView");
            }
        });
        mBtnViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList photoViewURLArrayList = new ArrayList(Arrays.asList("http://img2.yytcdn.com/artist/fan/150812/0/-M-0e9a280baa1d87f73e7d91db57cffa22_0x0.jpg",
                        "http://himg2.huanqiu.com/attachment2010/2015/0407/10/15/20150407101555843.jpg",
                        "http://img.cxdq.com/d1/img/081115/2008111514278263.jpg",
                        "http://img01.sogoucdn.com/app/a/100540002/856602.jpg"));
                // 跳转 Pager view
                PagerPhotoViewActivity.startPagerPhotoView(MainActivity.this, photoViewURLArrayList, "PhotoView");
            }
        });
    }
}
