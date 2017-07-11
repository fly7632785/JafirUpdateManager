package com.jafir.updatemanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by jafir on 15/6/27.
 * <p/>
 * 更新管理器
 * <p/>
 * 首先是去一个地址 下载一个xml配置文件
 * xml 文件里面有  安装包的 版本  版本名  APK地址
 * 获取xml 之后比对 如果有新的 版本则去下载
 * 然后安装
 */
public class UpdateManager {

    private String URL = "http://jafir-my-love.oss-cn-shanghai.aliyuncs.com/update_info.xml";
    private String mDownLoadPath = Environment.getExternalStorageDirectory() + "/download";
    /**
     * 用的另一个开源库的progressbar
     */
    private DownloadProgressBar mDownloadProgressBar;
    /**
     * 原生的progressBar
     */
    private ProgressBar mNativeProgressBar;
    private int mDrawingColor;
    private int mProgressColor;
    private int mCircleBackgroundColor;
    private int mProgressBackgroundColor;

    public enum ProgressStyle {
        Native, Cool
    }

    /*  选择progress样式 */
    private ProgressStyle progressStyle = ProgressStyle.Cool;
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 下载xml*/
    private static final int DOWNLOAD_XML = 3;
    /* 下载XML结束*/
    private static final int FINISH_DOWNLOAD_XML = 4;
    /* 下载失败*/
    private static final int FAIL_TO_LOAD_XML = 5;
    /* 保存解析的XML信息 */
//    HashMap<String, String> mHashMap;
    private UpdateInfo info;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private volatile boolean cancelUpdate = false;

    private Context mContext;
    /* 下载提示dialog */
    private Dialog mDownloadDialog;
    /* 根据是否是 后台操作 和 手动操作  提示用户网络请求界面 */
    private boolean isShowDialog = false;
    /* 进度条dialog */
    private ProgressDialog dialog;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setmDownLoadPath(String mDownLoadPath) {
        this.mDownLoadPath = mDownLoadPath;
    }

    public void setShowDialog(boolean showDialog) {
        isShowDialog = showDialog;
    }

    /**
     * 设置progressBar的样式
     *
     * @param progressStyle
     */
    public void setProgressStyle(ProgressStyle progressStyle) {
        this.progressStyle = progressStyle;
    }

    /**
     * @param drawingColor
     * @param progressColor
     * @param circleBackgroundColor
     * @param progressBackgroundColor
     */
    public void setCoolProgressStyle(int drawingColor, int progressColor, int circleBackgroundColor, int progressBackgroundColor) {
        mDrawingColor = drawingColor;
        mProgressColor = progressColor;
        mCircleBackgroundColor = circleBackgroundColor;
        mProgressBackgroundColor = progressBackgroundColor;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_XML:

                    break;
                case FAIL_TO_LOAD_XML:
                    Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_SHORT).show();
                    if (isShowDialog) {
                        dialog.dismiss();
                    }
                    break;
                case FINISH_DOWNLOAD_XML:
                    checkUpdate();
                    if (isShowDialog) {
                        dialog.dismiss();
                    }
                    break;
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    if (progressStyle.equals(ProgressStyle.Cool)) {
                        mDownloadProgressBar.setProgress(progress);
                    } else {
                        mNativeProgressBar.setProgress(progress);
                    }
                    break;
                case DOWNLOAD_FINISH:
                    if (progressStyle.equals(ProgressStyle.Cool)) {
                        mDownloadProgressBar.setSuccessResultState();
                    }
                    mDownloadDialog.dismiss();
                    installApk();
                    break;
            }
        }
    };

    /**
     * 开始从服务器获得xml
     */
    public void start() {
        if (isShowDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("正在检查更新...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        new DownloadXMLThread().start();
    }

    /**
     * 从网上下载xml配置文件
     */
    private class DownloadXMLThread extends Thread {
        @Override
        public void run() {
            // 把xml放到网络上，然后获取文件信息
            //InputStream inStream = ParseXmlService.class.getClassLoader().getResourceAsStream("version.xml");
            // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
            try {
                String path = URL;
                java.net.URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5 * 1000);
                conn.setRequestMethod("GET");
                InputStream inStream = conn.getInputStream();
                info = Parser.getUpdataInfo(inStream);
                mHandler.sendEmptyMessage(FINISH_DOWNLOAD_XML);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAIL_TO_LOAD_XML);
            }
        }
    }

    /**
     * 检测软件更新
     */
    private void checkUpdate() {
        if (isUpdate()) {
            // 显示提示对话框
            showNoticeDialog();
        } else if (isShowDialog) {
            //如果是静默检查更新则不显示
            Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 检查软件是否有更新版本
     *
     * @return 是否有新版本
     */
    private boolean isUpdate() {
        // 获取当前软件版本
        int versionCode = getAppVersionCode(mContext);

        if (null != info) {
            int serviceCode = Integer.valueOf(info.getVersion());
            // 版本判断
            if (serviceCode > versionCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前应用程序的版本号
     */
    private int getAppVersionCode(Context context) {
        int version = 0;
        try {
            version = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("the application not found");
        }
        return version;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_update_info);
        builder.setMessage(info.getDescription());
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        // 构造软件下载对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        builder.setCancelable(false);
        // 给下载对话框增加进度条
        View progressLayout = null;
        final LayoutInflater inflater = LayoutInflater.from(mContext);

        if (progressStyle.equals(ProgressStyle.Cool)) {
            progressLayout = inflater.inflate(R.layout.softupdate_progress, null);
            mDownloadProgressBar = (DownloadProgressBar) progressLayout.findViewById(R.id.download_view);
            setProgressStyle();
            mDownloadProgressBar.playManualProgressAnimation();
        } else {
            progressLayout = inflater.inflate(R.layout.native_progress, null);
            mNativeProgressBar = (ProgressBar) progressLayout.findViewById(R.id.download_view);
        }
        builder.setView(progressLayout);
        // 取消更新
        builder.setNegativeButton(R.string.soft_update_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
                if (progressStyle.equals(ProgressStyle.Cool)) {
                    mDownloadProgressBar.abortDownload();
                }
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    private void setProgressStyle() {
        if (mDrawingColor != 0) {
            mDownloadProgressBar.setmDrawingColor(mDrawingColor);
        }
        if (mProgressColor != 0) {
            mDownloadProgressBar.setmProgressColor(mProgressColor);
        }
        if (mCircleBackgroundColor != 0) {
            mDownloadProgressBar.setmCircleBackgroundColor(mCircleBackgroundColor);
        }
        if (mProgressBackgroundColor != 0) {
            mDownloadProgressBar.setmProgressBackgroundColor(mProgressBackgroundColor);
        }
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    /**
     * 下载文件线程
     */
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    java.net.URL url = new URL(info.getUrl());
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    conn.setConnectTimeout(5000);
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mDownLoadPath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mDownLoadPath, info.getVersionName() + ".apk");
                    /**
                     *  判断是否存在已经下载好的（这里只是通过检查是否大小相等）
                     */
                    if (length == apkFile.length()) {
                        progress = 99;
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                        is.close();
                        return;
                    }
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (Exception e) {
                if (progressStyle.equals(ProgressStyle.Cool)) {
                    mDownloadProgressBar.setErrorResultState();
                }
                e.printStackTrace();

            }
        }
    }

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mDownLoadPath, info.getVersionName() + ".apk");
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".file.provider", apkfile);
        } else {
            uri = Uri.fromFile(apkfile);
        }
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

    /**
     * 解析xml
     */
    static class Parser {
        /**
         * 解析从服务器获取来的xml更新文件
         *
         * @param inStream
         * @return
         * @throws Exception
         */
        public UpdateInfo parseXmlByDom(InputStream inStream) throws Exception {
            UpdateInfo info = new UpdateInfo();//实体
            // 实例化一个文档构建器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 通过文档构建器工厂获取一个文档构建器
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 通过文档通过文档构建器构建一个文档实例
            Document document = builder.parse(inStream);
            //获取XML文件根节点
            Element root = document.getDocumentElement();
            //获得所有子节点
            NodeList childNodes = root.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                //遍历子节点
                Node childNode = childNodes.item(j);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    //版本号
                    if ("version".equals(childElement.getNodeName())) {
                        info.setVersion(childElement.getFirstChild().getNodeValue());
                    }
                    //软件名称
                    else if (("versionName".equals(childElement.getNodeName()))) {
                        info.setVersionName(childElement.getFirstChild().getNodeValue());
                    }
                    //下载地址
                    else if (("url".equals(childElement.getNodeName()))) {
                        info.setUrl(childElement.getFirstChild().getNodeValue());
                    }
                    //下载地址
                    else if (("description".equals(childElement.getNodeName()))) {
                        info.setDescription(childElement.getFirstChild().getNodeValue());
                    }
                }
            }
            return info;
        }

        /*
         * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
         */
        public static UpdateInfo getUpdataInfo(InputStream is) throws Exception {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, "utf-8");//设置解析的数据源
            int type = parser.getEventType();
            UpdateInfo info = new UpdateInfo();//实体
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        if ("version".equals(parser.getName())) {
                            info.setVersion(parser.nextText()); //获取版本号
                        } else if ("url".equals(parser.getName())) {
                            info.setUrl(parser.nextText()); //获取要升级的APK文件
                        } else if ("versionName".equals(parser.getName())) {
                            info.setVersionName(parser.nextText()); //获取版本名字
                        } else if ("description".equals(parser.getName())) {
                            info.setDescription(parser.nextText());
                        }
                        break;
                }
                type = parser.next();
            }
            return info;
        }

    }
}
