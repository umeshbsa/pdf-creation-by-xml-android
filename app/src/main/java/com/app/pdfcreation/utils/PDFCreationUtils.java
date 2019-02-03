package com.app.pdfcreation.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;

import com.app.pdfcreation.R;
import com.app.pdfcreation.adapter.PdfCreateAdapter;
import com.app.pdfcreation.model.PDFModel;
import com.app.pdfcreation.ui.PdfCreationActivity;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class PDFCreationUtils {


    private int deviceHeight;
    private int deviceWidth;

    /**
     * This bitmap is created by view.
     * There is many bitmat which is depend on how many page created from pdfmodel list size.
     * ex. we find 'SECTOR' by calculation of deviceHeight is 7 and list isze is 22 then number of bitmap is 22/7 = 3 + 1(4)
     * means 4 bitmapOfView which contain 7 row of every bitmap.
     */
    private Bitmap bitmapOfView;

    private PdfDocument document;

    private int pdfModelListSize;

    /**
     * This is define to how many create number of row in per page
     */
    private int SECTOR = 8; // default value

    /**
     * Total number of page in per file
     */
    private int NUMBER_OF_PAGE;

    /**
     * PDFModel data with list
     */
    private List<PDFModel> mCurrentPDFModels;

    /**
     * Adapter for row item
     */
    private PdfCreateAdapter pdfRootAdapter;

    /**
     * This is parent view
     * In this view we have contain App logo, 'MyEarnings' and first row item from PdfCreateAdapter inflate
     */
    private View mPDFCreationView;
    private RecyclerView mPDFCreationRV;

    /**
     * This is child view
     * In this view we have to create all items row
     */

    /**
     * This is indicate to progressbar
     * This is static because we have to define all object(PDFCreationUtils class) within single variable
     */
    public static int TOTAL_PROGRESS_BAR;

    /**
     * This is indicate to number of pdf files.
     * This all files is merge with one single pdf file
     */
    private int mCurrentPDFIndex;

    /**
     * This is store of all pdf file path.
     * This is static because we have to define all object(PDFCreationUtils class) within single variable
     */
    public static List<String> filePath = new ArrayList<>();
    private PdfCreationActivity activity;

    /**
     * This is final merge pdf file path
     */
    private String finalPdfFile;

    /**
     * This is create for every pdf file path and end of it will be merge to single pdf file path
     */
    private String pathForEveryPdfFile;

    private PDFCreationUtils() {
    }

    /**
     * @param activity
     * @param currentPdfModels  list of currentPdfModels for every file
     * @param totalPDFModelSize this is total pdf list size during current sub list of pdf model
     * @param currentPDFIndex   This is define to number of pdf files
     */
    public PDFCreationUtils(PdfCreationActivity activity, List<PDFModel> currentPdfModels, int totalPDFModelSize, int currentPDFIndex) {
        this.activity = activity;
        this.mCurrentPDFModels = currentPdfModels;
        this.mCurrentPDFIndex = currentPDFIndex;
        getWH();
        createForEveryPDFFilePath();
        int sizeInPixel = activity.getResources().getDimensionPixelSize(R.dimen.dp_90) +
                activity.getResources().getDimensionPixelSize(R.dimen.dp_30);

        // Inflate parent view which contain app logo, 'MyEarnings and item row define by SECTOR'
        mPDFCreationView = LayoutInflater.from(activity).inflate(R.layout.pdf_creation_view, null, false);


        // Number of item in per page
        SECTOR = deviceHeight / sizeInPixel;
        TOTAL_PROGRESS_BAR = totalPDFModelSize / SECTOR;

        mPDFCreationRV = (RecyclerView) mPDFCreationView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        mPDFCreationRV.setLayoutManager(mLayoutManager);

        pdfRootAdapter = new PdfCreateAdapter();

        document = new PdfDocument();
        pdfModelListSize = currentPdfModels.size();

    }

    private PDFCallback callback;

    /**
     * Call from MainActivity
     *
     * @param callback
     */
    public void createPDF(PDFCallback callback) {
        this.callback = callback;


        // If pdf list size <= sector
        if (pdfModelListSize <= SECTOR) {
            NUMBER_OF_PAGE = 1;

            bitmapOfView = AppUtils.findViewBitmap(mCurrentPDFModels, deviceWidth, deviceHeight, pdfRootAdapter, mPDFCreationRV, mPDFCreationView);
            PdfBitmapCache.addBitmapToMemoryCache(NUMBER_OF_PAGE, bitmapOfView);
            createPdf();
        } else {

            // If number of pdf list size > sector
            // First Identify to NUMBER_OF_PAGE
            NUMBER_OF_PAGE = pdfModelListSize / SECTOR;
            if (pdfModelListSize % SECTOR != 0) {
                NUMBER_OF_PAGE++;
            }


            // Create pdf final map
            // This is used to distribute list model of per page

            Map<Integer, List<PDFModel>> listMap = createFinalData();
            for (int PAGE_INDEX = 1; PAGE_INDEX <= NUMBER_OF_PAGE; PAGE_INDEX++) {
                List<PDFModel> list = listMap.get(PAGE_INDEX);
                bitmapOfView = AppUtils.findViewBitmap(list, deviceWidth, deviceHeight, pdfRootAdapter, mPDFCreationRV, mPDFCreationView);
                PdfBitmapCache.addBitmapToMemoryCache(PAGE_INDEX, bitmapOfView);
            }
            createPdf();
        }
    }

    /**
     * This is used to distribute list model of per page
     * This is create index by START - END ex. 0 -10, 10 - 20, 20 - 22
     *
     * @return Create final map to distribute on per page by pdf list
     */
    private Map<Integer, List<PDFModel>> createFinalData() {
        int START = 0;
        int END = SECTOR;
        Map<Integer, List<PDFModel>> map = new LinkedHashMap<>();
        int INDEX = 1;
        for (int i = 0; i < NUMBER_OF_PAGE; i++) {
            if (pdfModelListSize % SECTOR != 0) {
                if (i == NUMBER_OF_PAGE - 1) {
                    END = START + pdfModelListSize % SECTOR;
                }
            }
            List<PDFModel> list = mCurrentPDFModels.subList(START, END);
            START = END;
            END = SECTOR + END;
            map.put(INDEX, list);
            INDEX++;
        }
        return map;
    }

    /**
     * Create pdf from view bitmap
     * This is call by per pdf file
     */

    private void createPdf() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int PAGE_INDEX = 1; PAGE_INDEX <= NUMBER_OF_PAGE; PAGE_INDEX++) {

                    final Bitmap b = PdfBitmapCache.getBitmapFromMemCache(PAGE_INDEX);
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(b.getWidth(), b.getHeight(), PAGE_INDEX).create();
                    PdfDocument.Page page = document.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#ffffff"));
                    canvas.drawPaint(paint);
                    canvas.drawBitmap(b, 0, 0, null);
                    document.finishPage(page);

                    File filePath = new File(pathForEveryPdfFile);
                    try {
                        document.writeTo(new FileOutputStream(filePath));
                    } catch (IOException e) {
                        if (callback != null) {
                            if (e != null) {
                                callback.onError(e);
                            } else {
                                callback.onError(new Exception("IOException"));
                            }

                        }
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onProgress(progressCount++);
                        }
                    });

                    if (PAGE_INDEX == NUMBER_OF_PAGE) {
                        document.close();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCreateEveryPdfFile();
                            }
                        });
                    }
                }
            }
        }).start();


    }

    public static int progressCount = 1;

    public void downloadAndCombinePDFs() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mCurrentPDFIndex == 1) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onComplete(pathForEveryPdfFile);
                            }
                        }
                    });

                } else {
                    try {

                        PDFMergerUtility ut = new PDFMergerUtility();
                        for (String s : filePath) {
                            ut.addSource(s);
                        }

                        final FileOutputStream fileOutputStream = new FileOutputStream(new File(createFinalPdfFilePath()));
                        try {
                            ut.setDestinationStream(fileOutputStream);
                            ut.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());

                        } finally {
                            fileOutputStream.close();
                        }

                    } catch (Exception e) {

                    }

                    // delete of other pdf file
                    for (String s : filePath) {
                        new File(s).delete();
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onComplete(finalPdfFile);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void createForEveryPDFFilePath() {
        pathForEveryPdfFile = AppUtils.createPDFPath();
        filePath.add(pathForEveryPdfFile);
    }

    private String createFinalPdfFilePath() {
        finalPdfFile = AppUtils.createPDFPath();
        return finalPdfFile;
    }

    private void getWH() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
    }


    public interface PDFCallback {

        void onProgress(int progress);

        void onCreateEveryPdfFile();

        void onComplete(String filePath);

        void onError(Exception e);
    }

}
