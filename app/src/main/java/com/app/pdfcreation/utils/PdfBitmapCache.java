package com.app.pdfcreation.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Cache all PDF image with index
 */
public class PdfBitmapCache {

    private static LruCache<Integer, Bitmap> memoryCache = null;

    public static void initBitmapCache(Context context) {
        if (memoryCache == null) {
            final int memClass = ((ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            final int cacheSize = 1024 * 1024 * memClass;
            memoryCache = new LruCache<Integer, Bitmap>(cacheSize) {

                @Override
                protected int sizeOf(Integer key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
    }

    public static void clearMemory() {
        if (memoryCache != null) {
            memoryCache.evictAll();
        }
    }

    public static void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        memoryCache.put(key, bitmap);
    }

    public static Bitmap getBitmapFromMemCache(Integer key) {
        return memoryCache.get(key);
    }
}