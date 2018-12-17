package com.android.dcxiaolou.hrichedittest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.dcxiaolou.hrichedittest.runtimepermissions.PermissionsManager;
import com.android.dcxiaolou.hrichedittest.runtimepermissions.PermissionsResultAction;
import com.huangdali.base.EditorResultBean;
import com.huangdali.bean.EContent;
import com.huangdali.bean.ItemType;
import com.huangdali.utils.ImageCompereUtils;
import com.huangdali.utils.ImageScaleUtils;
import com.huangdali.view.HRichEditorView;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private static final int REQUEST_CODE_EDIT = 192;

    private String coverUri;

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this, "d74918828662b3d50485e6ef1f8b5e9f");

        Log.e("MainActivity", "onCreate(MainActivity.java:16)");
        requestPermission();
    }

    public void onStart(View view) {
        startActivityForResult(new Intent(this, HRichEditorView.class), REQUEST_CODE_EDIT);
    }

    /**
     * android6.0动态权限申请
     */
    private void requestPermission() {

        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(String permission) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int wittingTime = 0;

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_EDIT) {
            //拿到编辑内容对象
            EditorResultBean resultBean = (EditorResultBean) data.getSerializableExtra("contents");
            Log.d(TAG, "resultBean: " + resultBean);
            //上传图片、视频，并替换上传之后的服务器图片的url
            final List<EContent> contents = resultBean.getContents();
            for (final EContent content : contents) {
                wittingTime += 1000;
                if (ItemType.IMG.equals(content.getType()) || ItemType.VIDEO.equals(content.getType())) {
                    String url = content.getUrl();
                    Log.d(TAG, "url: " + url);
                    if (url != null) {
                        uploadFile(url, content);
                    }
                }
            }
            //拿到编辑内容对应的html body的字符串（已经包括样式啦）
            String articleTitle = data.getStringExtra("articleTitle");
            final String bgUri = data.getStringExtra("bgUri");
            if (articleTitle != null) {
                articleTitle = "<h1 style='text-align: center;'>" + articleTitle + "</h1>";
            }
            if (bgUri != null) {
                wittingTime += 1000;
                uploadFile(bgUri);
            }
            final String header =  articleTitle;
            final int sleepTime = wittingTime; //根据图片数量设置时间
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(sleepTime);
                        String htmlBody = header + coverUri;
                        for (EContent content : contents) {
                            htmlBody += content.getHtml();
                        }
                        Log.d(TAG, "最终编辑的结果：" + htmlBody);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 模拟文件上传，并且返回上传之后该文件所在的路径
     *
     * @param uri
     * @return
     */
    private void uploadFile(String uri, final EContent content) {
        String filePath = ImageCompereUtils.compressImg(ImageScaleUtils.getRealPathFromURI(this, Uri.parse(uri)), 30);//压缩文件
        Log.d(TAG, "filePath: " + filePath);
        //具体的文件上传逻辑

        final BmobFile file = new BmobFile(new File(filePath));
        if (file != null) {
            file.upload(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    String uploadResult;//用于记录文件上传之后的路径（建议使用相对路径，最好不好写死域名，拼接html的时候再加域名，避免域名更改导致更换麻烦的问题）
                    uploadResult = file.getFileUrl();
                    content.setUrl(uploadResult);//反设置图片、视频的url（将URI转换为服务器中存放的地址）
                    Log.d(TAG, "uploadResult: " + uploadResult);
                }
            });
        }

        //模拟上传到服务器的地址
        //uploadResult = "/upload/15519099928/IMG_" + System.currentTimeMillis() + "." + filePath.substring(filePath.lastIndexOf(".") + 1);
        //return uploadResult;
    }

    private void uploadFile(String uri) {
        String filePath = ImageCompereUtils.compressImg(ImageScaleUtils.getRealPathFromURI(this, Uri.parse(uri)), 30);//压缩文件
        Log.d(TAG, "filePath: " + filePath);
        //具体的文件上传逻辑

        final BmobFile file = new BmobFile(new File(filePath));
        if (file != null) {
            file.upload(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    String uploadResult;//用于记录文件上传之后的路径（建议使用相对路径，最好不好写死域名，拼接html的时候再加域名，避免域名更改导致更换麻烦的问题）
                    uploadResult = file.getFileUrl();
                    setCoverUri("<img style='width:100%;display: inline-block;' src=" + uploadResult + " /><br/>");
                    Log.d(TAG, "uploadResult: " + uploadResult);
                }
            });
        }

        //模拟上传到服务器的地址
        //uploadResult = "/upload/15519099928/IMG_" + System.currentTimeMillis() + "." + filePath.substring(filePath.lastIndexOf(".") + 1);
        //return uploadResult;
    }

}
