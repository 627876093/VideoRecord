package com.shuyu.videorecord;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/1 0001.
 */
public class UploadVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private ImageView text_video;
    private RelativeLayout LinearLayout_video;
    private static MediaPlayer player;
    private SurfaceView surface;
    private SurfaceHolder surfaceHolder;
    public static Button play;
    public Button pause;
    public Button btn;
    private static Uri uri;
    public static InputStream inputStream;
    int width, height;//屏幕宽高
    private static String videoPath =FileUtils.getAppPath();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);


        text_video = (ImageView) findViewById(R.id.text_video);
        LinearLayout_video = (RelativeLayout) findViewById(R.id.LinearLayout_video);
        play = (Button) findViewById(R.id.button1);
//        pause = (Button) findViewById(R.id.button2);
        surface = (SurfaceView) findViewById(R.id.surface);
        btn = (Button) findViewById(R.id.btn);

        surface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                        play.setVisibility(View.VISIBLE);
//                        pause.setVisibility(View.GONE);
                    } else {
                        player.start();
                        play.setVisibility(View.GONE);
//                        pause.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    player.release();
                }
                inputStream = null;
                getVideoCapture();
            }
        });
        surfaceHolder = surface.getHolder();  //SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this);    //因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
        surfaceHolder.setFixedSize(CommonUtils.SIZE_1, CommonUtils.SIZE_2);   //显示的分辨率,不设置为视频默认
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //Surface类型
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
//                this.getWindowManager();
        height = wm.getDefaultDisplay().getHeight();
        width = wm.getDefaultDisplay().getWidth();
//        LinearLayout_video.getLayoutParams().height=height-200;

        surface.getLayoutParams().width = width - (width / 10);
        surface.getLayoutParams().height = height - (height / 6);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.setVisibility(View.GONE);
//                pause.setVisibility(View.VISIBLE);
                player.start();
            }
        });
//        pause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                play.setVisibility(View.VISIBLE);
//                pause.setVisibility(View.GONE);
//                player.pause();
//            }
//        });

        text_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setVisibility(View.VISIBLE);
                if (player != null) {
                    player.release();
                }
                inputStream = null;
                getVideoCapture();
            }
        });

        btn.setVisibility(View.VISIBLE);
        if (player != null) {
            player.release();
        }
        inputStream = null;
        getVideoCapture();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                uri = null;
                if (data != null) {
                    uri = data.getData();
                    videoPath = getRealFilePath(this, uri);
//                    text_video.setVisibility(View.VISIBLE);
//                    text_video.setVisibility(View.GONE);
//                    play.setVisibility(View.VISIBLE);
//                    LinearLayout_video.setVisibility(View.VISIBLE);
//                    try {
//                        inputStream = getContentResolver().openInputStream(uri);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }

                    Intent intent = new Intent(this, PlayActivity.class);
                    intent.putExtra(PlayActivity.DATA, videoPath);
                    startActivityForResult(intent, 2222);
                    finish();
                }
            }
        }
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public static InputStream getInputStream() {
        return inputStream;
    }

    public static Uri getUri() {
        return uri;
    }

    public static String getPath() {
        return videoPath;
    }

    public static void playerPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                play.setVisibility(View.VISIBLE);
            }
//                player.release();
        }
    }


    public static void release() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            //Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(surfaceHolder);
        //设置显示视频显示在SurfaceView上
        try {
            player.setDataSource(this, uri);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play.setVisibility(View.VISIBLE);
//                pause.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void getVideoCapture() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //质量 0最小，1最大 默认中
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //限制时长
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        //限制大小1024*1024L为1M
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024*1024L);
//        //根据文件地址创建文件
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        //设置拍摄的视频存放路径
//        File file=new File(videoPath);
//        if (file.exists()){
//            file.delete();
//        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//        开启摄像机
        startActivityForResult(intent, 1);
    }

}
