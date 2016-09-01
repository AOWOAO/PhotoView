package me.itangqi.module.photoview;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Troy on 2015/10/2.
 * 用来实现下载功能，根据Pref的设置，是否每次在MainActivity启动时调用
 */
public class DownloadService extends IntentService {

    private String mURLStr = null;
    private String mFileName = null;
    private String mFilePath = null;
    private int hasDown;
    private int size;
    private boolean isError = false;

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;
    public static final int ID = 0x123;

    public DownloadService() {
        super("Updater");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mNotificationBuilder = new Notification.Builder(this)
                .setContentTitle("正在下载")
                .setProgress(100, 0, false)
                .setLargeIcon(bitmap)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[]{500})
                .setOngoing(true);
        mURLStr = intent.getStringExtra("file_url");
        mFileName = intent.getStringExtra("file_name");
        mFilePath = intent.getStringExtra("file_path");
        download();
    }

    private void download() {
        final File file;
        InputStream is = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            URL url = new URL(mURLStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                size = connection.getContentLength();
                is = connection.getInputStream();
                bis = new BufferedInputStream(is);
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Huaban" + File.separator + mFilePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file = new File(dir, mFileName);
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                byte[] buffer = new byte[1024];
                int hasRead;
                showStatus();
                while ((hasRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, hasRead);
                    hasDown += hasRead;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (mFilePath.equals("Pins")) {
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
                            Toast.makeText(DownloadService.this, "成功保存到 Huaban/" + mFilePath + " 文件夹", Toast.LENGTH_SHORT).show();
                        } else if (mFilePath.equals("Apps")){
                            installApk(Uri.fromFile(file));
                        }
                        Looper.loop();
                    }
                }).start();
            } else {
            }
        } catch (IOException e) {
            isError = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(DownloadService.this, "保存失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }).start();
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showStatus() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int status = (int) (((float) hasDown / size) * 100);
                if (!isError) {
                    mNotificationManager.notify(ID, mNotificationBuilder.setProgress(100, status, false).build());
                } else {
                    mNotificationManager.cancel(ID);
                    cancel();
                }
                if (status >= 100) {
                    mNotificationManager.cancel(ID);
                    cancel();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 100);
    }

    private void installApk(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.stopSelf();
    }

}
