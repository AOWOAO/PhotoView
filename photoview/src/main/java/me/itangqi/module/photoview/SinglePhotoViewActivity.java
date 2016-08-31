package me.itangqi.module.photoview;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import me.relex.photodraweeview.OnViewTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

public class SinglePhotoViewActivity extends AppCompatActivity {

    private PhotoDraweeView mPhotoDraweeView;
    private static final float MAXIMUM_SCALE = 5.0f; // 最大缩放比
    private static final int REQUEST_CODE = 0; // 请求码
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE; // 所需的权限
    private String mPhotoURL;
    private String mFileName;
    private String mFolderName;

    public static void startSinglePhotoView(Activity activity, String photoURL, String fileName, String folderName) {
        Intent intent = new Intent(activity, SinglePhotoViewActivity.class);
        intent.putExtra("photoURL", photoURL);
        intent.putExtra("fileName", fileName);
        intent.putExtra("folderName", folderName);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo_view);
        mPhotoURL = getIntent().getStringExtra("photoURL");
        if (mPhotoURL == null || mPhotoURL.isEmpty()) {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
            return;
        }
        mFileName = getIntent().getStringExtra("fileName");
        mFolderName = getIntent().getStringExtra("folderName");
        mPhotoDraweeView = (PhotoDraweeView) findViewById(R.id.photo_drawee_view);
        mPhotoDraweeView.setMaximumScale(MAXIMUM_SCALE);
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        controller.setUri(Uri.parse(mPhotoURL));
        controller.setOldController(mPhotoDraweeView.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null || mPhotoDraweeView == null) {
                    return;
                }
                mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        mPhotoDraweeView.setController(controller.build());
        mPhotoDraweeView.setOnViewTapListener(new OnViewTapListener() {

            @Override
            public void onViewTap(View view, float x, float y) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        ImageView mImageViewSave = (ImageView) findViewById(R.id.iv_save);
        mImageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDownloadImage(mPhotoURL, mFileName, mFolderName);
            }
        });
    }

    private void startDownloadImage(String photoURL, String fileName, String folderName) {
        if ((ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED)) {
            PermissionsActivity.startActivityForResult(this, REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
        } else {
            downloadImage(photoURL, fileName, folderName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时相应处理
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            // TODO
        } else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            downloadImage(mPhotoURL, mFileName, mFolderName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImage(mPhotoURL, mFileName, mFolderName);
        } else {
            Toast.makeText(getApplicationContext(), R.string.photo_view_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadImage(String photoURL, String fileName, String folderName) {
        Intent intent = new Intent(SinglePhotoViewActivity.this, DownloadService.class);
        intent.putExtra("file_url", photoURL);
        intent.putExtra("file_name", fileName);
        intent.putExtra("folder_name", folderName);
        startService(intent);
    }

}



