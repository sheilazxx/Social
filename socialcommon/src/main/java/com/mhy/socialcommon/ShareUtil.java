package com.mhy.socialcommon;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mahongyin 2020-05-30 12:29 @CopyRight mhy.work@qq.com
 * description .
 */
public class ShareUtil {
    Context mActivity;
    private static ShareUtil shareUtil;

    public static ShareUtil getInstance(Context context) {
        if (shareUtil == null) {
            shareUtil = new ShareUtil(context);
        }
        return shareUtil;
    }

    private ShareUtil(Context act) {
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
        // 必须明确使用mailto前缀来修饰邮件地址
        Uri uri = Uri.parse("mailto:" + emails);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);//不带附件
//        intent.putExtra(Intent.EXTRA_EMAIL, emails);//接收者
//        intent.putExtra(Intent.EXTRA_CC, emails); // 抄送人
//        intent.putExtra(Intent.EXTRA_BCC, emails);//密送
        intent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, content); // 正文
        mActivity.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
    }

    /**
     * 带附件
     *
     * @param title
     * @param content
     * @param emails
     * @param filePath
     */
    public void sendEmailAccessory(String title, String content, String[] emails, String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
//        intent.putExtra(Intent.EXTRA_CC, emails);
//        intent.putExtra(Intent.EXTRA_BCC, emails);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);

        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath/*"file:///mnt/sdcard/a.jpg"*/));
        intent.setType("*/*");//intent.setType("text/html");.html类型
        intent.setType("message/rfc882");
        Intent.createChooser(intent, "请选择邮件类应用");
        mActivity.startActivity(intent);
        // 必须明确使用mailto前缀来修饰邮件地址,
//        Uri uri = Uri.parse("mailto:" + emails);
//        Intent intent = new Intent(Intent.ACTION_SEND, uri);//带附件
//        intent.putExtra(Intent.EXTRA_EMAIL, emails);
//        intent.putExtra(Intent.EXTRA_CC, emails); // 抄送人
//        intent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
//        intent.putExtra(Intent.EXTRA_TEXT, content); // 正文
//        mActivity.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
    }

    /**
     * 多附件
     *
     * @param title
     * @param content
     * @param emails
     * @param filePaths
     */
    public void sendEmailMultipleAccessory(String title, String content, String emails, List<String> filePaths) {
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
//        Uri uri = Uri.parse("mailto:" + emails);
//        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE, uri);//带多附件
//        intent.putExtra(Intent.EXTRA_EMAIL, emails);
//        intent.putExtra(Intent.EXTRA_CC, emails); // 抄送人
//        intent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
//        intent.putExtra(Intent.EXTRA_TEXT, content); // 正文
//        mActivity.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
//        intent.putExtra(Intent.EXTRA_CC, emails);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (String filePath : filePaths) {
            imageUris.add(Uri.parse(filePath/*"file:///mnt/sdcard/a.jpg"*/));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        intent.setType("*/*");
        intent.setType("message/rfc882");
        Intent.createChooser(intent, "请选择邮件类应用");
        mActivity.startActivity(intent);
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

    public void openBrowser(String url) {
        Uri myBlogUri = Uri.parse(url);
        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, myBlogUri));
    }

    public void openMap(String j, String w) {
        Uri mapUri = Uri.parse("geo:" + j + "，" + w);
        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
    }

    public void toCall(String phone) {
        Uri telUri = Uri.parse("tel:" + phone);
        mActivity.startActivity(new Intent(Intent.ACTION_DIAL, telUri));
    }

    public void doCall(String phone) {
        Uri callUri = Uri.parse("tel:" + phone);
        mActivity.startActivity(new Intent(Intent.ACTION_CALL, callUri));
    }

    public void unInstanll(String packname) {
        Uri uninstallUri = Uri.fromParts("package", packname, null);
        mActivity.startActivity(new Intent(Intent.ACTION_DELETE, uninstallUri));
    }

    public void instanll(String packname) {
        Uri installUri = Uri.fromParts("package", packname, null);
        mActivity.startActivity(new Intent(Intent.ACTION_PACKAGE_ADDED, installUri));
    }

    public void play(String path) {
        Uri playUri = Uri.parse(path/*"file:///sdcard/download/everything.mp3"*/);
        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, playUri));
    }

    public void toSendSMS(String phone, String msg) {
        Uri smsUri = Uri.parse("tel:" + phone);
        Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
        mActivity.startActivity(intent);
        intent.putExtra("sms_body", "yyyy");
        intent.setType("vnd.android-dir/mms-sms");
    }

    public void doSendSMS(String phone, String msg) {
        Uri smsToUri = Uri.parse("smsto://" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", msg);
        mActivity.startActivity(intent);
    }

    public void toSendColorSMS(String mediaPath, String phone, String msg) {
        Uri mmsUri = Uri.parse(mediaPath/*"content://media/external/images/media/23"*/);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("sms_body", msg);
        intent.putExtra(Intent.EXTRA_STREAM, mmsUri);
        intent.setType("*/*");
        mActivity.startActivity(intent);
    }
 /*利用URL Scheme

   比如在自带浏览器里面输入
   "javascript:window.location.href=’alipays://platformapi/startapp?appId=20000056’;"
   即可直接打开支付宝的二维码付款页面。自己手动添加浏览器书签然后丢到桌面就行了。
   支付宝扫码
   alipayqr://platformapi/startapp?saId=10000007
   支付宝付款
   alipays://platformapi/startapp?appId=20000056
   支付宝红包入口
   alipay://platformapi/startapp?saId=88886666

   下面是微信的
   ”weixin://dl/groupchat“发起群聊
   ”weixin://dl/add“添加朋友
   ”weixin://dl/log“上报日志
   ”weixin://dl/recommendation“新的朋友
   ”weixin://dl/groups“群聊
   ”weixin://dl/tags“标签
   ”weixin://dl/officialaccounts“公众号
   ”weixin://dl/moments“朋友圈
   ”weixin://dl/scan“扫一扫
   ”weixin://dl/shopping“购物
   ”weixin://dl/games“游戏
   ”weixin://dl/profile“个人信息
   ”weixin://dl/setname“名字
   ”weixin://dl/myQRcode“我的二维码
   ”weixin://dl/myaddress“我的地址
   ”weixin://dl/posts“相册
   ”weixin://dl/favorites“收藏
   ”weixin://dl/card“优惠券
   ”weixin://dl/stickers“表情
   ”weixin://dl/settings“设置
   ”weixin://dl/bindqq“QQ 号
   ”weixin://dl/bindmobile“手机号
   ”weixin://dl/bindemail“邮箱地址
   ”weixin://dl/protection“帐号保护
   ”weixin://dl/notifications“新消息通知
   ”weixin://dl/blacklist“通讯录黑名单
   ”weixin://dl/hidemoments“不让他（她）看我的朋友圈
   ”weixin://dl/blockmoments“不看他（她）的朋友圈
   ”weixin://dl/general“通用
   ”weixin://dl/languages“多语言
   ”weixin://dl/textsize“字体大小
   ”weixin://dl/stickersetting“我的表情
   ”weixin://dl/sight“朋友圈小视频
   ”weixin://dl/features“功能
   ”weixin://dl/securityassistant“通讯录同步助手
   ”weixin://dl/broadcastmessage“群发助手
   ”weixin://dl/chathistory“聊天记录迁移
   ”weixin://dl/clear“清理微信存储空间
   ”weixin://dl/help“意见反馈
   ”weixin://dl/about“关于微信
*/
    public void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mActivity.startActivity(intent);
    }
}
