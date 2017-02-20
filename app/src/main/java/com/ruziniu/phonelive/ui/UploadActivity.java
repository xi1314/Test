package com.ruziniu.phonelive.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.AppManager;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.listener.CompressListener;
import com.ruziniu.phonelive.ui.listener.InitListener;
import com.ruziniu.phonelive.utils.FileUtil;
import com.ruziniu.phonelive.utils.ImageUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.TLog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLDecoder;
import java.util.Locale;

import okhttp3.Call;


/**
 * Created by admin on 2016/10/12.
 */
public class UploadActivity extends ToolBarBaseActivity {

    private EditText mFileName;
    private Button mUpload;
    private String choose;
    private ImageView mIvChoose;
    private TextView mTvChoose;
    private UserBean user;
    private String objectname1;
    private LinearLayout mLlchoose;
    private LinearLayout mSuoLveTu;
    private String path1;
    private String objectname;
    private ImageView mIvShiPinSuoLveTu;
    private ImageView mIvChooseShiPin;
    private TextView mTvUpload;
    private String path;
    private File file1;
    private File file;
    private String title;
    private FFmpeg ffmpeg;
    private String time;
    private int maxSize;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_upload;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_private_chat_back:
                finish();
                break;
            case R.id.btn_upload:
                upload();
                break;
            case R.id.ll_choose:
                chooseVideoPhone();
                break;
            case R.id.ll_suolvetu:
                startImagePick();
                break;
        }
    }

    private void chooseVideoPhone() {
        if (choose.equals("1")) {
            //跳到图库
            Intent intent = new Intent(Intent.ACTION_PICK);
            //选择的格式为视频,图库中就只显示视频（如果图片上传的话可以改为image，图库就只显示图片）
            intent.setType("video/*");
            // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
            startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
        } else {
            startImagePick();
        }
    }

    /**
     * 选择图片裁剪
     */
    private void startImagePick() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // 从相册返回的数据
            if (data != null) {
                if (requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD) {
                    Uri uri1 = Uri.parse(URLDecoder.decode(data.getDataString()));
                    path1 = getRealFilePath(this, uri1);
                    String b = path1.substring(path1.lastIndexOf(File.separator) + 1, path1.length());
                    objectname1 = b;
                    //图片缩略图
                    Bitmap bitmap = BitmapFactory.decodeFile(path1);
                    mIvShiPinSuoLveTu.setImageBitmap(bitmap);
                } else {
                    // 得到视频的全路径
                    Uri uri = Uri.parse(URLDecoder.decode(data.getDataString()));
                    path = getRealFilePath(UploadActivity.this, uri);

                    String b = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
                    objectname = b;
                    //视频缩略图
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                    mIvChoose.setImageBitmap(bitmap);
                    mIvChooseShiPin.setVisibility(View.VISIBLE);
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /* 下面是4.4后通过Uri获取路径以及文件名一种方法，比如得到的路径 /storage/emulated/0/video/20160422.3gp，
                            通过索引最后一个/就可以在String中截取了*/
    public String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return "";
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
        String p = data;
        return p;
    }

    private void upload() {

        if (path1 == null) {
            hideWaitDialog();
            Toast.makeText(this, mTvUpload.getText(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (choose.equals("1")) {
            if (path == null) {
                hideWaitDialog();
                Toast.makeText(this, "请选择上传视频", Toast.LENGTH_SHORT).show();
                return;
            }

            if(FileUtil.getFileSize(path) > maxSize){
                hideWaitDialog();
                Toast.makeText(this, String.format(Locale.CHINA,"超过%dM视频您无权限上传！",maxSize/1024/1024), Toast.LENGTH_SHORT).show();
                return;

            }
        }



        title = mFileName.getText().toString();
        if (title.equals("") || title.isEmpty()) {
            hideWaitDialog();
            Toast.makeText(this, "请输入标题名", Toast.LENGTH_SHORT).show();
            return;
        }
        time = user.getId() + "_" + String.valueOf(SystemClock.currentThreadTimeMillis());

        file1 = new File(path1);
        file = null;
        if (choose.equals("1")) {
            file = new File(path);
            /*String cmd = "-y -i " + file.getPath() + " -vcodec h264 -acodec mp2 " + file.getParent() + "/out.mp4";

            String[] command = cmd.split(" ");

            execFFmpegBinary(command);*/

            uploadFile();
        } else {
            showWaitDialogNoCancel("正在上传...");
            PhoneLiveApi.uploadimg(user.getId(), title, objectname1, user.getToken(), file1, new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {
                    hideWaitDialog();
                }

                @Override
                public void onResponse(String response) {
                    hideWaitDialog();
                    TLog.log(response);
                    boolean res = ApiUtils.checkIsSuccess2(response);
                    if (res) {
                        Toast.makeText(UploadActivity.this, "上传图片成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void inProgress(float progress) {
                    super.inProgress(progress);
                    //进度条
                }
            });
        }
    }

    private void execFFmpegBinary(final String[] command) {

        showWaitDialogNoCancel("正在压缩视频...");

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    //mProgress.setText("FAILED with output : "+s);
                    hideWaitDialog();
                    uploadFile();
                }

                @Override
                public void onSuccess(String s) {
                    //mProgress.setText("FAILED with output : "+s);
                    hideWaitDialog();

                    File newVideo = new File(file.getParent() + "/out.mp4");

                    if (file.exists()) {

                        file = newVideo;
                    }
                    uploadFile();

                }

                @Override
                public void onProgress(String s) {
                    //mProgress.setText("FAILED with output : "+s);

                }

                @Override
                public void onStart() {
                    //mProgress.setText("Started command : ffmpeg " + command);

                }

                @Override
                public void onFinish() {

                    //mProgress.setText("Finished command : ffmpeg" + command);

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void uploadFile() {

        showWaitDialogNoCancel("正在上传...");
        PhoneLiveApi.uploadvideo(time, user.getId(), title, objectname, objectname1, user.getToken(), file, file1, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                PhoneLiveApi.checkuploadvideo(user.getId(), time, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideWaitDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        hideWaitDialog();
                        try {
                            JSONObject resJson = new JSONObject(response);
                            if (Integer.parseInt(resJson.getString("ret")) == 200) {
                                JSONObject dataJson = resJson.getJSONObject("data");
                                String code = dataJson.getString("code");
                                if (code.equals("700")) {
                                    AppManager.getAppManager().finishAllActivity();
                                    Intent intent = new Intent(AppContext.getInstance(), LiveLoginSelectActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    AppContext.getInstance().startActivity(intent);
                                } else if (!code.equals("0")) {
                                    Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(UploadActivity.this, "上传视频成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(String response) {
                hideWaitDialog();
                boolean res = ApiUtils.checkIsSuccess2(response);
                if (res) {
                    Toast.makeText(UploadActivity.this, "上传视频成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }

    @Override
    public void initView() {

        mFileName = (EditText) findViewById(R.id.et_search_input);
        mUpload = (Button) findViewById(R.id.btn_upload);
        mIvChoose = (ImageView) findViewById(R.id.iv_choose);
        mTvChoose = (TextView) findViewById(R.id.tv_choose);
        mTvUpload = (TextView) findViewById(R.id.tv_shangchuantu);
        mLlchoose = (LinearLayout) findViewById(R.id.ll_choose);
        mSuoLveTu = (LinearLayout) findViewById(R.id.ll_suolvetu);
        mIvShiPinSuoLveTu = (ImageView) findViewById(R.id.iv_shipin_suolvetu);
        mIvChooseShiPin = (ImageView) findViewById(R.id.iv_choose_shipin);
        mLlchoose.setOnClickListener(this);
        mTvChoose.setOnClickListener(this);
        mUpload.setOnClickListener(this);
        mSuoLveTu.setOnClickListener(this);
        ffmpeg = FFmpeg.getInstance(getApplicationContext());
        loadFFMpegBinary();
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {

                }

                @Override
                public void onSuccess() {


                }
            });
        } catch (FFmpegNotSupportedException e) {
        }
    }

    @Override
    public void initData() {
        choose = getIntent().getStringExtra("choose");
        user = AppContext.getInstance().getLoginUser();
        if (choose.equals("1")) {
            mTvUpload.setText("请选择缩略图");
            mLlchoose.setVisibility(View.VISIBLE);
        } else {
            mTvUpload.setText("请选择上传图片");
            mLlchoose.setVisibility(View.GONE);
        }


        PhoneLiveApi.getVideoUploadMaxSize(new StringCallback(){

            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                if(res != null){
                    maxSize = StringUtils.toInt(res);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("uploadvideo");
        OkHttpUtils.getInstance().cancelTag("uploadimg");
    }
}
