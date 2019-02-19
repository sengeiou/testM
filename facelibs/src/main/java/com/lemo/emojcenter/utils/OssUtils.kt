package com.lemo.emojcenter.utils

/**
 * Description:下载
 * Author:wxw
 * Date:2018/2/1.
 */
class OssUtils/* private static OSS mOss;

    private OssUtils() {
    }

    public OssUtils init(Context context) {
        String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
        // 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考访问控制章节
        // 也可查看sample 中 sts 使用方式了解更多(https://github.com/aliyun/aliyun-oss-android-sdk/tree/master/app/src/main/java/com/alibaba/sdk/android/oss/app)
        OSSCredentialProvider credentialProvider =
            new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>",
                "<StsToken.SecurityToken>");
        //该配置类如果不设置，会有默认配置，具体可看该类
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        mOss = new OSSClient(context, endpoint, credentialProvider);
        return this;
    }

    public static void download(String bucketName, String objectKey, String filename,
                                String filesDir,
                                ProgressBar progressBar) {
        GetObjectRequest get = new GetObjectRequest(bucketName, objectKey);
        //设置下载进度回调
        get.setProgressListener((request, currentSize, totalSize) -> {

                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize,
                    false);
                if (null == progressBar) {
                    return;
                }
                progressBar.setMax((int) totalSize);
                progressBar.setProgress((int) currentSize);
            }

        );
        OSSAsyncTask task =
            mOss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
                @Override
                public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                    // 请求成功
                    InputStream inputStream = result.getObjectContent();
                    long contentLength = result.getMetadata().getContentLength();
                    File file = new File(filesDir, filename);
                    FileOutputStream fileOutputStream = null;

                    byte[] buffer = new byte[2048];
                    int len;
                    try {
                        fileOutputStream = new FileOutputStream(file, true);
                        while ((len = inputStream.read(buffer)) != -1) {
                            // 处理下载的数据
                            fileOutputStream.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(GetObjectRequest request, ClientException clientExcepion,
                                      ServiceException serviceException) {
                    // 请求异常
                    if (clientExcepion != null) {
                        // 本地异常如网络异常等
                        clientExcepion.printStackTrace();
                    }
                    if (serviceException != null) {
                        // 服务异常
                        Log.e("ErrorCode", serviceException.getErrorCode());
                        Log.e("RequestId", serviceException.getRequestId());
                        Log.e("HostId", serviceException.getHostId());
                        Log.e("RawMessage", serviceException.getRawMessage());
                    }
                }
            });
    }

    public static void getOssPermission(String objectKey, String filename, String filesDir,
                                        ProgressBar progressBar) {
        OkHttpUtils.get()
            .url(IConstants.GETUPLOADPERMISSION)
            .addHeader(IConstants.APP_KEY,IConstants.APP_KEY_VALUE)
            .build()
            .execute(new GsonReturnCallback<OssDataBean>(){
                @Override
                public void onError(Call call, Exception e, int id) {
                    super.onError(call, e, id);
                }

                @Override
                public void onResponse(OssDataBean response, int id) {
                    super.onResponse(response, id);
                    if (response == null) {
                        return;
                    }
                    //objectKey--表情包名称 测试暂用face/app/test.zip
//                    download(response.getBuckName(),objectKey,filename,filesDir,progressBar);
                    download(response.getBuckName(),"face/app/test.zip",filename,filesDir,progressBar);

                }
            });
    }*/
