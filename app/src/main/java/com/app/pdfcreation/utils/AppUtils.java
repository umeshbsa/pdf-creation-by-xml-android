package com.app.pdfcreation.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.pdfcreation.adapter.PdfCreateAdapter;
import com.app.pdfcreation.model.PDFModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AppUtils {

    /**
     * Adapter set all data by model then create bitmap of this view
     *
     * @param currentPDFModels pdf list - define by per NUMBER_OF_PAGE
     * @return
     */
    public static Bitmap findViewBitmap(final List<PDFModel> currentPDFModels, int deviceWidth, int deviceHeight, PdfCreateAdapter pdfRootAdapter, RecyclerView mPDFCreationRV, View mPDFCreationView) {
        pdfRootAdapter.setListData(currentPDFModels);
        mPDFCreationRV.setAdapter(pdfRootAdapter);
        return getViewBitmap(mPDFCreationView, deviceWidth, deviceHeight);
    }

    /**
     * @param view this is pass from parent or child view and create bitmap with current loaded data
     * @return
     */
    private static Bitmap getViewBitmap(View view, int deviceWidth, int deviceHeight) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(deviceHeight, View.MeasureSpec.EXACTLY);
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(deviceWidth, deviceHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.draw(c);
        return getResizedBitmap(b, (measuredWidth * 80) / 100, (measuredHeight * 80) / 100);
    }

    private static Bitmap getResizedBitmap(Bitmap image, int width, int height) {

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            height = (int) (width / bitmapRatio);
        } else {
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static String createPDFPath() {
        String foldername = "pdf_creation_by_xml";
        File folder = new File(Environment.getExternalStorageDirectory() + "/" + foldername);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");

        String date = simpleDateFormat.format(Calendar.getInstance().getTime());

        return folder + File.separator + "pdf_" + date + ".pdf";
    }

}
