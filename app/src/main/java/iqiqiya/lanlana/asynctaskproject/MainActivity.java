package iqiqiya.lanlana.asynctaskproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载是很耗时的操作 所以使用asynctask
 * 1. 网络上请求数据：申请网络权限，读写大容量存储(SD卡)的存储权限
 * 2. 布局我们的layout
 * 3. 下载之前  UI处理
 * 4. 下载中  数据处理
 * 5. 下载后  UI处理
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ProgressBar mProgressBar;
    private Button mDownloadButton;
    private TextView mResultTextView;
    public static final int INIT_PROGRESS = 0;
    public static final String APK_URL = "http://api.77sec.cn/LanParse/v115_LanParse_updata.apk";
    public static final String FILE_NAME = "iqiqiya.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initView();

        // 设置点击监听
        setListener();

        // 初始化UI数据
        setData();

        // 调用封装好的测试
        DownloadHelper.download(APK_URL, "", new DownloadHelper.OnDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int code, File file) {

            }

            @Override
            public void onFail(int code, File file, String message) {

            }

            @Override
            public void onProgress(int progress) {

            }
        });




    }

    /**
     * 初始化视图
     */
    private void initView() {
        mProgressBar = findViewById(R.id.progressBar);
        mDownloadButton = findViewById(R.id.button);
        mResultTextView = findViewById(R.id.text_view);
    }

    private void setListener() {
        mDownloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO: 2019/9/26 下载任务

                DownloadAsyncTask asyncTask = new DownloadAsyncTask();
                asyncTask.execute(APK_URL);
            }
        });
    }

    private void setData() {
        mResultTextView.setText(R.string.download_text);
        mDownloadButton.setText(R.string.click_download);
        mProgressBar.setProgress(INIT_PROGRESS);

    }

    /**
     * String 入参
     * Integer 进度
     * Boolean 返回值
     */
    public class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {

        String mFilePath;
        /**
         * 在异步任务之前，在主线程中
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 可操作UI  可以类比淘米之前做的准备工作
            mDownloadButton.setText(R.string.downloading);
            mResultTextView.setText(R.string.downloading);
            mProgressBar.setProgress(INIT_PROGRESS);// 进度初始化为0
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
            if (params != null && params.length > 0) {
                String apkUrl = params[0];
                try {
                    // 构造URL
                    URL url = new URL(apkUrl);
                    // 构造链接，并打开
                    URLConnection urlConnection = url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();

                    // 获取了下载内容的总长度
                    int contentLength = urlConnection.getContentLength();

                    // 下载地址准备  SD卡下面的iqiqiya.apk
                    mFilePath = Environment.getExternalStorageDirectory()
                            + File.separator + FILE_NAME;

                    // 对下载地址进行处理
                    File apkFile = new File(mFilePath);
                    if (apkFile.exists()) {
                        boolean result = apkFile.delete();
                        if (!result) {
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
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // 也是在主线程中,执行结果 处理
            mDownloadButton.setText(result ? getString(R.string.download_finish) : getString(R.string.download_finish));
            mResultTextView.setText(result ? getString(R.string.download_finish) + mFilePath : getString(R.string.download_finish));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // 收到进度，然后处理： 也是在UI线程中
            if (values != null && values.length > 0) {
                mProgressBar.setProgress(values[0]);
            }
        }
    }
}
