package com.yinghuanhang.pdf.parser;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Cao-Human on 2016/11/28
 */
public class FileUtils {

    public static final String DIRECTORY_CACHE = "/MuPDF_Demo/";
    private static String mRootDir = null;

    /**
     * @param name 名称
     * @param type 文件类型
     * @return 构建缓存文件
     */
    public static String onBuildingCache(String direct, String name, String type) {
        if (type == null) {
            return getCacheDirectory() + name;
        }
        return getCacheDirectory(direct) + name + "." + type;
    }

    public static String getCacheDirectory(String directory) {
        if (mRootDir == null) {
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            String root = dcim.getAbsolutePath() + DIRECTORY_CACHE;
            File file = new File(root);
            if (!file.exists() && !file.mkdirs()) {
                return "";
            }
            mRootDir = root;
        }
        if (TextUtils.isEmpty(directory)) {
            return mRootDir;
        }
        String path = mRootDir + directory + "/";
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            return "";
        }
        return path;
    }

    public static String getCacheDirectory() {
        return getCacheDirectory(null);
    }

    /**
     * 保存位图到指定路径
     */
    public static boolean onSaveBitmapTo(Bitmap bitmap, String path) {
        if (bitmap == null) {
            return false;
        }
        FileOutputStream out = null;
        try {
            File file = new File(path);
            if (!file.exists() || file.delete()) {
                out = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            close(out);
        }
    }

    /**
     * 关闭流对象
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // Nothing need to do
        }
    }
}