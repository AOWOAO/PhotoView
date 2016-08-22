package me.itangqi.module.photoview;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.relex.photodraweeview.OnViewTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

public class SinglePhotoViewActivity extends AppCompatActivity {

    private PhotoDraweeView mPhotoDraweeView;
    private static final float MAXIMUM_SCALE = 5.0f; // 最大缩放比
    private static final int REQUEST_CODE = 0; // 请求码
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE; // 所需的权限
    private static String mPhotoURL;
    private static String mFolderName;
    private BroadcastReceiver receiver;
    private static final String ACTION_DOWNLOAD_COMPLETE = "0x123";

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

        IntentFilter filter = new IntentFilter(ACTION_DOWNLOAD_COMPLETE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(SinglePhotoViewActivity.this, R.string.photo_view_download_success, Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(receiver, filter);
    }

    private void startDownloadImage() {
        if ((ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED)) {
            PermissionsActivity.startActivityForResult(this, REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
        } else {
            downloadImage(this, mFolderName, mPhotoURL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时相应处理
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            // TODO
        } else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            downloadImage(this, mFolderName, mPhotoURL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImage(this, mFolderName, mPhotoURL);
        } else {
            Toast.makeText(getApplicationContext(), R.string.photo_view_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private static void downloadImage(final Context context, String folderName, String photoURL) {
        File appDir = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        final File file = new File(appDir, fileName);
        // To get image using Fresco
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(photoURL))
                .setProgressiveRenderingEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, context);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {

            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 下载完成更新
                Intent completedIntent = new Intent(ACTION_DOWNLOAD_COMPLETE);
                context.sendBroadcast(completedIntent);
                // 通知图库更新
                Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
                context.sendBroadcast(scannerIntent);
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                // No cleanup required here.
            }

        }, CallerThreadExecutor.getInstance());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}



