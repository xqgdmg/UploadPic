package com.example.test.okhttpuploadcompress;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 你的文件路径 getFilesDir() 返回/data/data/youPackageName/files的File对象。记得拖进去
        File file = new File(getFilesDir(),"a.jgp");

        // 你的上传地址
        // http://192.168.1.100:8080/meinv/show?type=siwa
        // http://192.168.1.100:8080/UploadPic/PhotoTest
        uploadFileInThreadByOkHttp(this,"http://192.168.1.100:8080/UploadPic/PhotoTest",file);// 压缩图片并上传
    }

    /**
     * 压缩图片并上传
     */
    private  void uploadFileInThreadByOkHttp(final Activity context, final String uploadUrl, final File tempFile) {

        // 原图路径
        final String picPath = tempFile.getPath();

        // 压缩图路径
        String targetPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"compressPic.jpg";

        //调用压缩图片的方法，返回压缩后 小图片路径
        final String compressedPath = compressImage(picPath, targetPath, 30);

        // 构建压缩后的图片
        final File compressedFile = new File(compressedPath);

        // 压缩成功则压缩上传，不成功普通上传
        if (compressedFile.exists()) {
            Log.e(TAG, "图片压缩上传");
            uploadFileByOkHTTP(context, uploadUrl, compressedFile);
        }else{//直接上传
            uploadFileByOkHTTP(context, uploadUrl, tempFile);
        }

//        // 直接上传原图，上面全注释，老是提示文件不存在，尼玛
//        uploadFileByOkHTTP(context, uploadUrl, tempFile);
    }

    /**
     * add
     */
    private void uploadFileByOkHTTP(Activity context, String uploadUrl, File file) {

//        MediaType mediaType = MediaType.parse("application/octet-stream");// add
        MediaType mediaType = MediaType.parse("image/jpg"); // 区别
//        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        uploadFile(uploadUrl, file, mediaType, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"Failure == " + e.getMessage());//
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG,"onResponse == " + response.toString());
            }
        });
    }

    /**
     * 压缩图片的关键代码，返回小图片的路径
     */
    public static String compressImage(String filePath, String targetPath, int quality)  {
        Bitmap smallBitmap = getSmallBitmap(filePath);//获取一定尺寸的图片

        int degree = readPictureDegree(filePath);//获取相片拍摄角度
        if(degree!=0){//旋转照片角度，防止头像横着显示
            smallBitmap=rotateBitmap(smallBitmap,degree);
        }

        // 压缩后输出的文件
        File outputFile = new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();// 不存在创建
            }else{
                outputFile.delete();// 存在删除
            }

            FileOutputStream out = new FileOutputStream(outputFile);// 输出的文件转换成 输出流
            smallBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);// 流转 Bitmap
        }catch (Exception e){
            e.printStackTrace();
        }
        return outputFile.getPath();// 返回小图片的路径
    }

    /**
     * 根据路径获得图片信息并按比例压缩，返回bitmap
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
//        BitmapFactory.decodeFile(filePath, options);
//
//        // 计算缩放比
//        options.inSampleSize = calculateInSampleSize(options, 480, 800);
//
//        // 完整解析图片返回bitmap
//        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }


    /**
     * 获取照片 int 角度
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);// ExifInterface 获取额外的信息
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,// 朝向
                    ExifInterface.ORIENTATION_NORMAL);// 默认值不旋转 0 度

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转照片
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap,int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 计算缩放比
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // 实际宽高大于想要的宽高
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // 取 小的压缩比
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * OkHttp 上传文件
     * @param url  接口地址
     * @param file 上传的文件
     * @param mediaType  资源mediaType类型:比如 MediaType.parse("image/png");
     * @param responseCallback 回调方法,在子线程,更新UI要post到主线程
     * @return
     */
    public boolean uploadFile(String url, File file, MediaType mediaType, Callback responseCallback) {

        // 文件不存在 或者 上传网址是空的 直接返回 上传失败
        if (!file.exists() || TextUtils.isEmpty(url)){
            Log.e(TAG,"file.exists()|| TextUtils.isEmpty(url)");//
            return false;
        }

        // 用于 addFormDataPart
        RequestBody fileBody = RequestBody.create(mediaType, file);// 这个 mediaType 是什么意思？？

        //addFormDataPart视项目自身的情况而定
        //builder.addFormDataPart("description","2.jpg");
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.parse("multipart/form-data"));
        builder.addFormDataPart("upload", file.getName(), fileBody);// upload , file 这里的名字是服务器规定的

        //构建请求体
        RequestBody requestBody2 = builder.build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody2)
                .build();

        // 配置 OkHttpClient
        final okhttp3.OkHttpClient.Builder clientBuiler = new OkHttpClient.Builder();
        OkHttpClient okHttpClient  = clientBuiler
                //设置超时
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        // 请求加入客户端（Client） newCall enqueue
        okHttpClient.newCall(request).enqueue(responseCallback);

//        enqueue(request,responseCallback);
        return true;
    }

    private void uploadMultiFile() {

        // 上传地址
        final String url = "upload url";

        // 上传的文件
        File file = new File("fileDir", "test.jpg");

        // 请求体1 RequestBody == fileBody
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

        // 请求体2 RequestBody
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)// 直接设置为表单类型
                .addFormDataPart("image", "test.jpg", fileBody)// addFormDataPart视项目自身的情况而定，可以添加多个上传的文件。
                .build();

        // 请求 Request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)// 请求体 加入 请求
                .build();

        // 配置 OkHttpClient
        final okhttp3.OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient  = builder
                //设置超时
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        // 请求加入客户端（Client） newCall enqueue
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "uploadMultiFile() e=" + e);
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "uploadMultiFile() response=" + response.body().string());
            }
        });
    }
}
