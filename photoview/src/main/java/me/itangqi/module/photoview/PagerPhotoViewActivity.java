package me.itangqi.module.photoview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import me.relex.photodraweeview.PhotoDraweeView;

public class PagerPhotoViewActivity extends AppCompatActivity {
    private ArrayList<String> mPhotoList;
    private String mFolderName;
    private String mPhotoURL;
    private int mPosition;
    private static final float MAXIMUM_SCALE = 5.0f; // 最大缩放比
    private static final int REQUEST_CODE = 0; // 请求码
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE; // 所需的权限

    public static void startPagerPhotoView(Context context, ArrayList<String> arrayList, int position, String folderName) {
        Intent intent = new Intent(context, PagerPhotoViewActivity.class);
        intent.putStringArrayListExtra("arrayList", arrayList);
        intent.putExtra("folderName", folderName);
        intent.putExtra("position", position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_photo_view);
        mPhotoList = getIntent().getStringArrayListExtra("arrayList");
        if (mPhotoList == null || mPhotoList.isEmpty()) {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
            return;
        }
        mFolderName = getIntent().getStringExtra("folderName");
        mPosition = getIntent().getIntExtra("position", 0);
        // 初始化小圆点
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        final MultiTouchViewPager viewPager = (MultiTouchViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new DraweePagerAdapter());
        viewPager.setCurrentItem(mPosition);
        indicator.setViewPager(viewPager);
        // 初始化下载图标
        ImageView mImageViewSave = (ImageView) findViewById(R.id.iv_save);
        mImageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoURL = mPhotoList.get(viewPager.getCurrentItem());
                // 默认都是 .jpg
                startDownloadImage(mPhotoURL, mFolderName);
            }
        });
    }

    private class DraweePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPhotoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            final PhotoDraweeView photoDraweeView = new PhotoDraweeView(viewGroup.getContext());
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(Uri.parse(mPhotoList.get(position)));
            controller.setOldController(photoDraweeView.getController());
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            photoDraweeView.setController(controller.build());
            photoDraweeView.setMaximumScale(MAXIMUM_SCALE);
            try {
                viewGroup.addView(photoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return photoDraweeView;
        }
    }

    private void startDownloadImage(String photoURL, String folderName) {
        if ((ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED)) {
            PermissionsActivity.startActivityForResult(this, REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
        } else {
            downloadImage(photoURL, folderName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时相应处理
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            // TODO
        } else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            downloadImage(mPhotoURL, mFolderName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImage(mPhotoURL, mFolderName);
        } else {
            Toast.makeText(getApplicationContext(), R.string.photo_view_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadImage(String photoURL, String folderName) {
        Intent intent = new Intent(PagerPhotoViewActivity.this, DownloadService.class);
        intent.putExtra("file_url", photoURL);
        intent.putExtra("file_name", System.currentTimeMillis() + ".jpg");
        intent.putExtra("folder_name", folderName);
        startService(intent);
    }

}
