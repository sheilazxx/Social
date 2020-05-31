package com.mhy.socialcommon;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author mahongyin 2020-05-30 12:29 @CopyRight mhy.work@qq.com
 * description .
 */
public class ShareUtil {
    Activity mActivity;

    public ShareUtil(Activity act) {
        this.mActivity = act;
    }

    public void shareText(String content) {
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, content);
        mActivity.startActivity(Intent.createChooser(textIntent, "分享"));
    }

    /**
     * @param resImg 本地图片
     */
    public void shareImg(int resImg) {
        String path = getResourcesUri(resImg);
        Intent imageIntent = new Intent(Intent.ACTION_SEND);
        imageIntent.setType("image/*");
        imageIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        mActivity.startActivity(Intent.createChooser(imageIntent, "分享"));
    }

    public void shareImg(String path) {
//        String path = (saveImage(resImg));
        Intent imageIntent = new Intent(Intent.ACTION_SEND);
        imageIntent.setType("image/*");
        imageIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        mActivity.startActivity(Intent.createChooser(imageIntent, "分享"));
    }

    public void shareMultPath(ArrayList<String> imagePaths) {

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (String imagePath : imagePaths) {
            Uri uri = Uri.parse(imagePath);
            imageUris.add(uri);
        }

        Intent mulIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        mulIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        mulIntent.setType("image/*");
        mActivity.startActivity(Intent.createChooser(mulIntent, "分享"));
    }

    public void shareMultImg(ArrayList<Integer> imageRes) {

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (Integer imageRe : imageRes) {
            Uri uri1 = Uri.parse(getResourcesUri(imageRe));
            imageUris.add(uri1);
        }
//
//        Uri uri1 = Uri.parse(saveImage(R.drawable.dog));
//        Uri uri2 = Uri.parse(saveImage(R.drawable.shu_1));
//        imageUris.add(uri1);
//        imageUris.add(uri2);
        Intent mulIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        mulIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        mulIntent.setType("image/*");
        mActivity.startActivity(Intent.createChooser(mulIntent, "分享"));
    }

    public void shareWx(String msg) {
        Intent wechatIntent = new Intent(Intent.ACTION_SEND);
        wechatIntent.setPackage("com.tencent.mm");
        wechatIntent.setType("text/plain");
        wechatIntent.putExtra(Intent.EXTRA_TEXT, msg);
        mActivity.startActivity(wechatIntent);
    }
    public void shareWb(String msg) {
        Intent weiboIntent = new Intent(Intent.ACTION_SEND);
        weiboIntent.setPackage("com.sina.weibo");
        weiboIntent.setType("text/plain");
        weiboIntent.putExtra(Intent.EXTRA_TEXT, msg);
        mActivity.startActivity(weiboIntent);
    }

    public void shareQq(String msg) {
        Intent qqIntent = new Intent(Intent.ACTION_SEND);
        qqIntent.setPackage("com.tencent.mobileqq");
        qqIntent.setType("text/plain");
        qqIntent.putExtra(Intent.EXTRA_TEXT, msg);
        mActivity.startActivity(qqIntent);
    }

    // 调用系统方法分享文件
    public void shareFile(String filePath) {
        File file = new File(filePath);
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            Uri uri = null;
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // "项目包名.fileprovider"即是在清单文件中配置的authorities
                uri = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".com.fileprovider", file);
                // 给目标应用一个临时授权
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mActivity.startActivity(Intent.createChooser(share, "分享文件"));
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    private String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }

    private String getResourcesUri(@DrawableRes int id) {
        Resources resources = mActivity.getResources();
        String uriPath = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(id) + "/" +
                resources.getResourceTypeName(id) + "/" +
                resources.getResourceEntryName(id);
        return uriPath;
    }

    public void sendEmail(String title, String content, String emails) {
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
        Uri uri = Uri.parse("mailto:" + emails);

        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_EMAIL, emails);//结果将匹配不到任何应用
        intent.putExtra(Intent.EXTRA_CC, emails); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, content); // 正文
        mActivity.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
    }

    /**
     * res目录下面的一张图片保存到本地
     *
     * @param resId 图片的id
     */
    private String saveImage(int resId) {
        //在本地创建一个文件
        File file = new File(mActivity.getFilesDir().getAbsolutePath() + "/image/Image" + ".png");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeResource(mActivity.getResources(), resId);

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

}
