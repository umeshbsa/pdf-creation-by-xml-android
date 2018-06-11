package com.app.pdfcreation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {


    private PDFCreatorByXML pdfCreator;
    private boolean IS_MANY_PDF_FILE;
    private int SECTOR = 100;
    private int START;
    private int END = SECTOR;
    private int NO_OF_PDF_FILE = 1;
    private int NO_OF_FILE;
    private int LIST_SIZE;
    private ProgressDialog progressDialog;


    private List<PDFModel> pdfModels;
    private TextView tvPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        tvPath = (TextView) findViewById(R.id.tv_path);

        findViewById(R.id.btn_create_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            generatePdfReport();
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        } else {
            generatePdfReport();
        }
    }


    /**
     * This is manage to all model
     */
    private void generatePdfReport() {

        PDFCreatorByXML.filePath.clear();
        PDFCreatorByXML.progressCount = 1;


        pdfModels = new ArrayList<>();

        /**
         * Create received model
         */

        for (int i = 0; i < 12; i++) {
            PDFModel model = new PDFModel();
            if (!isFirstReceivedItem) {
                model.setReceived(true);
                isFirstReceivedItem = true;
            } else {
                model.setReceived(false);
            }
            model.setPrice("$220");
            model.setName("Umesh " + i);
            model.setRating(i % 3);
            pdfModels.add(model);
        }

        if (pdfModels.size() == 0) {
            Toast.makeText(this, "msg_no_received_pending_data_from_api", Toast.LENGTH_LONG).show();
            return;
        }

        /**
         * This is identify to one file or many file have to created
         */
        LIST_SIZE = pdfModels.size();
        NO_OF_FILE = LIST_SIZE / SECTOR;
        if (LIST_SIZE % SECTOR != 0) {
            NO_OF_FILE++;
        }
        if (LIST_SIZE > SECTOR) {
            IS_MANY_PDF_FILE = true;
        } else {
            END = LIST_SIZE;
        }
        createProgressBar();
        createPDFFile();
    }

    private void createProgressBar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        //  dialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(false);
        //  progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // progressDialog.setProgress(0);
        progressDialog.setMax(100);
        // progressDialog.setMessage("Loading ...");
        progressDialog.show();
    }

    /**
     * This function call with recursion
     * This recursion depend on number of file (NO_OF_PDF_FILE)
     */
    private void createPDFFile() {

        List<PDFModel> pdfDataList = pdfModels.subList(START, END);
        if (pdfDataList != null) {
            PdfBitmapCache.clearMemory();
            PdfBitmapCache.initBitmapCache(getApplicationContext());
            pdfCreator = new PDFCreatorByXML(MainActivity.this, pdfDataList, LIST_SIZE, NO_OF_PDF_FILE);
            pdfCreator.createPDF(new PDFCreatorByXML.IPdfCallback() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(final int i) {
                    progressDialog.setProgress(i);
                }

                @Override
                public void onExecute() {
                    /**
                     * Execute may pdf files and this is depend on NO_OF_FILE
                     */
                    if (IS_MANY_PDF_FILE) {
                        NO_OF_PDF_FILE++;
                        if (NO_OF_FILE == NO_OF_PDF_FILE - 1) {
                            pdfCreator.downloadAndCombinePDFs();
                        } else {

                            /**
                             *  This is identify to manage sub list of current pdf model list data with START and END
                             */
                            START = END;
                            if (LIST_SIZE % SECTOR != 0) {
                                if (NO_OF_FILE == NO_OF_PDF_FILE) {
                                    END = (START - SECTOR) + LIST_SIZE % SECTOR;
                                }
                            }
                            END = SECTOR + END;
                            createPDFFile();
                        }

                    } else {
                        /**
                         * Merge one pdf file when all file is downloaded
                         */
                        pdfCreator.downloadAndCombinePDFs();
                    }

                }

                @Override
                public void onComplete(String filePath) {
                    progressDialog.dismiss();

                    if (filePath != null) {
                        tvPath.setVisibility(View.VISIBLE);
                        tvPath.setText("Your Pdf path : " + filePath);
                        Toast.makeText(MainActivity.this, "Final pdf file " + filePath, Toast.LENGTH_LONG).show();
                        sharePdf(filePath);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(MainActivity.this, "Error  " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Enable only first received data
     */
    boolean isFirstReceivedItem;

    /**
     * Enable only first pending data
     */
    boolean isFirstPendingItem;


    private void sharePdf(String fileName) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        ArrayList<Uri> uris = new ArrayList<Uri>();
        File fileIn = new File(fileName);

        //  Uri u = Uri.fromFile(fileIn);

        Uri u = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, fileIn);

        uris.add(u);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Can't read pdf file", Toast.LENGTH_SHORT).show();
        }
    }

}
