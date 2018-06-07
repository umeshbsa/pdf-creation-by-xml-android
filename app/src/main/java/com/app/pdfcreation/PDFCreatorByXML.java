package com.app.pdfcreation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PDFCreatorByXML {


    private RelativeLayout mAppLogoRL;
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
    private int SECTOR = 7; // default value

    /**
     * Total number of page in per file
     */
    private int NUMBER_OF_PAGE;

    /**
     * PDFModel data with list
     */
    private List<PDFModel> pdfModels;

    /**
     * Adapter for row item
     */
    private PdfCreateAdapter pdfRootAdapter;

    /**
     * This is parent view
     * In this view we have contain App logo, 'MyEarnings' and first row item from PdfCreateAdapter inflate
     */
    private View parentView;
    private RecyclerView parentRV;

    /**
     * This is child view
     * In this view we have to create all items row
     */
    private View childView;
    private RecyclerView childRV;

    /**
     * This is indicate to progressbar
     * This is static because we have to define all object(PDFCreatorByXML class) within single variable
     */
    public static int TOTAL_PROGRESS_BAR;

    /**
     * This is indicate to number of pdf files.
     * This all files is merge with one single pdf file
     */
    private int PDF_INDEX;

    /**
     * This is store of all pdf file path.
     * This is static because we have to define all object(PDFCreatorByXML class) within single variable
     */
    public static List<String> filePath = new ArrayList<>();
    private MainActivity activity;

    /**
     * This is final merge pdf file path
     */
    private String finalPdfFile;

    /**
     * This is create for every pdf file path and end of it will be merge to single pdf file path
     */
    private String pathForEveryPdfFile;

    private PDFCreatorByXML() {
    }

    /**
     * @param activity
     * @param pdfModels       list of pdfmodel for every file
     * @param TOTAl_LIST_SIZE this is total pdf list size during current sub list of pdf model
     * @param PDF_INDEX       This is define to number of pdf files
     */
    public PDFCreatorByXML(MainActivity activity, List<PDFModel> pdfModels, int TOTAl_LIST_SIZE, int PDF_INDEX) {
        this.activity = activity;
        this.pdfModels = pdfModels;
        this.PDF_INDEX = PDF_INDEX;
        getWH();
        createForEveryPDFFilePath();
        int sizeInPixel = activity.getResources().getDimensionPixelSize(R.dimen.dp_90) +
                activity.getResources().getDimensionPixelSize(R.dimen.dp_30);

        /**
         * Inflate parent view which contain app logo, 'MyEarnings and item row define by SECTOR'
         */
        parentView = LayoutInflater.from(activity).inflate(R.layout.pdf_creation_parent_view, null, false);
        mAppLogoRL = (RelativeLayout) parentView.findViewById(R.id.rl_logo);

        /**
         * Visible only first pdf file
         */
        if (PDF_INDEX == 1) {
            mAppLogoRL.setVisibility(View.VISIBLE);
        } else {
            mAppLogoRL.setVisibility(View.GONE);
        }

        /**
         * This is inflate of item row
         */
        childView = LayoutInflater.from(activity).inflate(R.layout.pdf_creation_child_view, null);

        /**
         * Number of item in per page
         */
        SECTOR = deviceHeight / sizeInPixel;
        TOTAL_PROGRESS_BAR = TOTAl_LIST_SIZE / SECTOR;

        if (TOTAl_LIST_SIZE > 0) {
            TOTAL_PROGRESS_BAR = 1;
        }

        parentRV = (RecyclerView) parentView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        parentRV.setLayoutManager(mLayoutManager);


        childRV = (RecyclerView) childView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(activity);
        childRV.setLayoutManager(mLayoutManager1);

        pdfRootAdapter = new PdfCreateAdapter();

        document = new PdfDocument();
        pdfModelListSize = pdfModels.size();

    }

    private IPdfCallback callback;

    /**
     * Call from MyPaymentListActivity
     *
     * @param iPdfCallback
     */
    public void createPDF(IPdfCallback iPdfCallback) {
        this.callback = iPdfCallback;

        /**
         * If pdf list size <= sector
         */
        if (pdfModelListSize <= SECTOR) {
            NUMBER_OF_PAGE = 1;
            bitmapOfView = findParentViewBitmap(pdfModels);
            PdfBitmapCache.addBitmapToMemoryCache(NUMBER_OF_PAGE, bitmapOfView);
            createPdf();
        } else {

            /**
             * If number of pdf list size>sector
             * First Identify to NUMBER_OF_PAGE
             */
            NUMBER_OF_PAGE = pdfModelListSize / SECTOR;
            if (pdfModelListSize % SECTOR != 0) {
                NUMBER_OF_PAGE++;
            }

            /**
             * Create pdf final map
             * This is used to distribute list model of per page
             */
            Map<Integer, List<PDFModel>> listMap = createFinalData();
            for (int PAGE_INDEX = 1; PAGE_INDEX <= NUMBER_OF_PAGE; PAGE_INDEX++) {
                List<PDFModel> list = listMap.get(PAGE_INDEX);
                if (PAGE_INDEX == 1) {
                    bitmapOfView = findParentViewBitmap(list);
                } else {
                    bitmapOfView = findChildViewBitmap(list);
                }
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
            List<PDFModel> list = pdfModels.subList(START, END);
            START = END;
            END = SECTOR + END;
            map.put(INDEX, list);
            INDEX++;
        }
        return map;
    }

    /**
     * Adapter set all data by model then create bitmap of this view
     *
     * @param list pdf list - define by per NUMBER_OF_PAGE
     * @return
     */
    private Bitmap findParentViewBitmap(final List<PDFModel> list) {
        pdfRootAdapter.setListData(list);
        parentRV.setAdapter(pdfRootAdapter);
        return getViewBitmap(parentView);
    }

    /**
     * Adapter set all data by model then create bitmap of this view
     *
     * @param list pdf list - define by per NUMBER_OF_PAGE
     * @return
     */
    private Bitmap findChildViewBitmap(final List<PDFModel> list) {
        pdfRootAdapter.setListData(list);
        childRV.setAdapter(pdfRootAdapter);
        return getViewBitmap(childView);
    }


    /**
     * @param view this is pass from parent or child view and create bitmap with current loaded data
     * @return
     */
    private Bitmap getViewBitmap(View view) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(deviceHeight, View.MeasureSpec.EXACTLY);
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap b = Bitmap.createBitmap(deviceWidth, deviceHeight, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        view.draw(c);
        return getResizedBitmap(b, (measuredWidth * 80) / 100, (measuredHeight * 80) / 100);
    }

    public Bitmap getResizedBitmap(Bitmap image, int width, int height) {

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            height = (int) (width / bitmapRatio);
        } else {
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
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

                    if (PDF_INDEX == 1) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null)
                                    callback.onStart();
                            }
                        });
                    }
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
                            int progress = (progressCount++ * 100) / TOTAL_PROGRESS_BAR;
                            if (progress > 90) {
                                progress = 90;
                            }
                            callback.onProgress(progress);
                        }
                    });
                    if (PAGE_INDEX == NUMBER_OF_PAGE) {
                        document.close();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onExecute();
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

                if (PDF_INDEX == 1) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onProgress(100);
                            }
                        }
                    });

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
                                callback.onProgress(100);
                            }
                        }
                    });

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
        String foldername = "pdf_creation_by_xml";
        File folder = new File(Environment.getExternalStorageDirectory() + "/" + foldername);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (PDF_INDEX == 1) {
            pathForEveryPdfFile = folder + File.separator + "PDF_Creation.pdf";
        } else {
            pathForEveryPdfFile = folder + File.separator + "PDF_Creation_" + PDF_INDEX + ".pdf";
        }
        filePath.add(pathForEveryPdfFile);
    }

    private String createFinalPdfFilePath() {
        String bankName = "pdf_creation_by_xml";
        File folder = new File(Environment.getExternalStorageDirectory() + "/" + bankName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        finalPdfFile = folder + File.separator + "PDF_Creation.pdf";
        return finalPdfFile;
    }

    private void getWH() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
    }


    public interface IPdfCallback {
        void onStart();

        void onProgress(int progress);

        void onExecute();

        void onComplete(String filePath);

        void onError(Exception e);
    }

}
