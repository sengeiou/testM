package com.lemo.emojcenter.utils.download;

import com.lemo.emojcenter.FaceConfigInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class FaceDownloadManager {
    private static final AtomicReference<FaceDownloadManager> INSTANCE = new AtomicReference<>();
    private HashMap<String, Call> downCalls;//用来存放各个下载的请求
    private OkHttpClient mClient;//OKHttpClient;

    private FaceDownloadManager() {
        downCalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    //获得一个单例类
    public static FaceDownloadManager getInstance() {
        for (; ; ) {
            FaceDownloadManager current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new FaceDownloadManager();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    /**
     * 开始下载
     *
     * @param url              下载请求的网址
     * @param downLoadObserver 用来回调的接口
     */
    public void download(String url, FaceDownLoadObserver downLoadObserver, String fileName) {
        Observable.just(url)
        .filter(s -> !downCalls.containsKey(s))//call的map已经有了,就证明正在下载,则这次不下载
        .flatMap(s -> Observable.just(createDownInfo(s, fileName)))
        .map(this::getRealFileName)//检测本地文件夹,生成新的文件名
        .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))//下载
        .observeOn(AndroidSchedulers.mainThread())//在主线程回调
        .subscribeOn(Schedulers.io())//在子线程执行
        .subscribe(downLoadObserver);//添加观察者
    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private FaceDownloadInfo createDownInfo(String url, String fileName) {
        FaceDownloadInfo downloadInfo = new FaceDownloadInfo(url);
        long contentLength = getContentLength(url);//获得文件大小
        downloadInfo.setTotal(contentLength);
        //        String fileName = url.substring(url.lastIndexOf("/"));
        downloadInfo.setFileName(fileName);
        return downloadInfo;
    }

    private FaceDownloadInfo getRealFileName(FaceDownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0, contentLength = downloadInfo.getTotal();
        String dir= FaceConfigInfo.INSTANCE.getDirEmojRoot();
        File file = new File(dir, fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        //之前下载过,需要重新来一个文件
        int i = 1;
        while (downloadLength >= contentLength) {
            int dotIndex = fileName.lastIndexOf(".");
            String fileNameOther;
            if (dotIndex == -1) {
                fileNameOther = fileName + "(" + i + ")";
            } else {
                fileNameOther = fileName.substring(0, dotIndex)
                + "(" + i + ")" + fileName.substring(dotIndex);
            }
            File newFile = new File(dir, fileNameOther);
            file = newFile;
            downloadLength = newFile.length();
            i++;
        }
        //设置改变过的文件名/大小
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
        .url(downloadUrl)
        .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? FaceDownloadInfo.Companion.getTOTAL_ERROR() : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FaceDownloadInfo.Companion.getTOTAL_ERROR();
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<FaceDownloadInfo> {
        private FaceDownloadInfo downloadInfo;

        public DownloadSubscribe(FaceDownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<FaceDownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getProgress();//已经下载好的长度
            long contentLength = downloadInfo.getTotal();//文件的总长度
            //初始进度信息
            e.onNext(downloadInfo);
            Request request = new Request.Builder()
            //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
            .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
            .url(url)
            .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            File fileDown = new File(FaceConfigInfo.INSTANCE.getDirEmojZipRoot(), downloadInfo.getFileName()+".down");
            File file = new File(FaceConfigInfo.INSTANCE.getDirEmojZipRoot(), downloadInfo.getFileName());
            if(fileDown.exists()){
                fileDown.delete();
            }
            if(file.exists()){
                file.delete();
            }
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(fileDown, true);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();
                fileDown.renameTo(file);
                downCalls.remove(url);
            } finally {
                //关闭IO流
                FaceIOUtil.closeAll(is, fileOutputStream);

            }
            downloadInfo.setFile(file);
            e.onComplete();//完成
        }
    }


}
