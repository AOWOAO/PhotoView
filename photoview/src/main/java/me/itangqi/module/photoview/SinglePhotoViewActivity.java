package me.itangqi.module.photoview;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;

import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.OnViewTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

public class SinglePhotoViewActivity extends AppCompatActivity {

    private PhotoDraweeView mPhotoDraweeView;
    private static final float MAXIMUM_SCALE = 5.0f; // 最大缩放比
    private static final int REQUEST_CODE = 0; // 请求码
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE; // 所需的权限
    private static String mPhotoURL;
    private static String mFolderName;
    private long mDownloadReference;
    private BroadcastReceiver receiver;

    public static void startSinglePhotoView(Activity activity, String photoURL, String folderName) {
        mPhotoURL = photoURL;
        mFolderName = folderName;
        Intent intent = new Intent(activity, SinglePhotoViewActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo_view);

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
                startDownloadImage();
            }
        });

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (mDownloadReference == reference) {
                    Toast.makeText(SinglePhotoViewActivity.this, R.string.photo_view_download_success, Toast.LENGTH_LONG).show();
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private void startDownloadImage() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时相应处理
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            // TODO
        } else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            downloadImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImage();
        } else {
            Toast.makeText(getApplicationContext(), R.string.photo_view_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadImage() {
        File appDir = new File(Environment.getExternalStorageDirectory(), mFolderName);
        if (!appDir.exists()) {
            if (appDir.mkdir()) {
                downloadImageUseDownloadManager();
            } else {
                Toast.makeText(getApplicationContext(), R.string.photo_view_download_mkdir_failure, Toast.LENGTH_SHORT).show();
            }
        } else {
            downloadImageUseDownloadManager();
        }
    }

    private void downloadImageUseDownloadManager() {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(mPhotoURL);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(mFolderName, System.currentTimeMillis() + ".jpg");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        mDownloadReference = downloadManager.enqueue(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}



