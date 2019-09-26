package iqiqiya.lanlana.asynctaskproject;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Author: iqiqiya
 * Date: 2019/9/26
 * Time: 21:04
 * Blog: blog.77sec.cn
 * Github: github.com/iqiqiya
 *
 * 1. download方法 url localPath listener
 * 2. listener: start, success fail progress.
 * 3. 用Asynctask封装好的
 */
public class DownloadHelper {

    public static void download(String url, String localPath, OnDownloadListener listener) {

        DownloadAsyncTask task = new DownloadAsyncTask(url,localPath,listener);
        task.execute();
    }

    /**
     * String 入参
     * Integer 进度
     * Boolean 返回值
     */
    public static class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {

        String mUrl;
        String mFilePath;
        OnDownloadListener mListener;

        public DownloadAsyncTask(String url, String filePath, OnDownloadListener listener) {
            mUrl = url;
            mFilePath = filePath;
            mListener = listener;
        }

        /**
         * 在异步任务之前，在主线程中
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 可操作UI  可以类比淘米之前做的准备工作
            if (mListener != null) {
                mListener.onStart();
            }
        }

        /**
         * 在另外一个线程中处理事件
         *
         * @param params 入参  煮米
         * @return 结果
         */
        // String... strings可变长度的参数
        // doInBackground(str1,str2...str8)
        @Override
        protected Boolean doInBackground(String... params) {

                String apkUrl = mUrl;
                try {
                    // 构造URL
                    URL url = new URL(apkUrl);
                    // 构造链接，并打开
                    URLConnection urlConnection = url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();

                    // 获取了下载内容的总长度
                    int contentLength = urlConnection.getContentLength();

                    // 下载地址准备  SD卡下面的iqiqiya.apk

                    // 对下载地址进行处理
                    File apkFile = new File(mFilePath);
                    if (apkFile.exists()) {
                        boolean result = apkFile.delete();
                        if (!result) {
                            if (mListener != null) {
                                mListener.onFail(-1,apkFile,"文件删除失败");
                            }
                            return false;
                        }
                    }

                    // 已下载的大小
                    int downloadSize = 0;

                    // 新建byte数组
                    byte[] bytes = new byte[1024];

                    int length;

                    // 创建一个输入管道
                    OutputStream outputStream = new FileOutputStream(mFilePath);

                    while ((length = inputStream.read(bytes)) != -1) {
                        // 从0写到length  文件管道
                        outputStream.write(bytes, 0, length);
                        // 每次大小增加length  累加
                        downloadSize += length;
                        // 将进度发布
                        publishProgress(downloadSize * 100 / contentLength);
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        mListener.onFail(-2,new File(mFilePath),e.getMessage());
                    }
                    return false;
                }
                if (mListener != null) {
                    mListener.onSuccess(0,new File(mFilePath));
                }
                return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // 也是在主线程中,执行结果 处理
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess(0,new File(mFilePath));
                } else {
                    mListener.onFail(-1,new File(mFilePath),"下载失败");
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // 收到进度，然后处理： 也是在UI线程中
            if (values != null && values.length > 0) {
                if (mListener != null) {
                    mListener.onProgress(values[0]);
                }
            }

        }
    }

    public interface OnDownloadListener {
        void onStart();

        void onSuccess(int code, File file);

        void onFail(int code, File file, String message);

        void onProgress(int progress);

        abstract class SimpleDownloadListener implements OnDownloadListener{
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {

            }
        }
    }
}
