package au.com.intellihealth.android.androidpdfrenderer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class MainActivity extends RelativeLayout {
    private Context context;
    private static int PORTRAIT = 0, LANDSCAPE = 1;
    private int orientation = PORTRAIT;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final float PDF_PAGE_PADDING = 5f;
    private ImageViewTouch imageView;
    private int currentPage = 0;
    private static final String TAG = "PDF";
    private int maxPdfWidth = 0, maxPdfHeight = 0;
    private int maxScaledWidth = 0, maxScaledHeight = 0;
    private float screenWidthRatio = 1.0f;
    private int totalPdfHeight = 0;
    private int imageWidth = 0, imageHeight = 0;
    private PdfRenderer renderer;
    private String path;

    public MainActivity(Context context, AttributeSet set){
        super(context,set);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(this);


        imageView = (ImageViewTouch) findViewById(R.id.image);

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Here you can get the size :)
                imageWidth = imageView.getWidth();
                //getResources().getDisplayMetrics().density;
                imageHeight = imageView.getHeight();

                if(imageWidth > imageHeight){
                    orientation = LANDSCAPE;
                }
                preparePDF();
                render();
            }
        });


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void preparePDF(){
        File file = null;
        if(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canRead()) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/test2.pdf");
            Log.i("PDF", file.toString());
        }else if(Environment.getDownloadCacheDirectory().canRead()){
            file = new File(Environment.getDownloadCacheDirectory().toString() + "/test2.pdf");
            Log.i("PDF", file.toString());
        }else{
            file = new File(Environment.getRootDirectory().toString() + "/SDCard/Download/test2.pdf");
            Log.i("PDF", file.toString());
        }
        // Log.i("PDF", "Can read? "+file.canRead());

        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            int pdfPageHeight = 0, pdfPageWidth = 0;
            for (int i = 0; i < renderer.getPageCount(); i++) {
                PdfRenderer.Page pdfPage = renderer.openPage(i);
                pdfPageHeight = pdfPage.getHeight();
                pdfPageWidth = pdfPage.getWidth();

                if (pdfPageWidth > maxPdfWidth) {
                    //Log.i(TAG, "Current (w/h): "+maxPdfWidth+"/"+maxPdfHeight+" | new: "+pdfPageWidth+"/"+pdfPageHeight);
                    maxPdfWidth = pdfPageWidth;
                    maxPdfHeight = pdfPageHeight;
                }
                totalPdfHeight += pdfPageHeight;

                pdfPage.close();
            }
            // Adding the padding between the pages
            totalPdfHeight += (renderer.getPageCount()-1) * PDF_PAGE_PADDING;
            // Calculates the screen width ratio (we're scrolling vertically)
            if(orientation == PORTRAIT) {
                Log.i(TAG, "In PORTRAIT");
                screenWidthRatio = (float) imageWidth / maxPdfWidth;
            }else{
                Log.i(TAG, "In LANDSCAPE");
                screenWidthRatio = (float) imageHeight / maxPdfHeight;
            }
            Log.i(TAG, "Max PDF Height: " + maxPdfHeight + " | max PDF Width: " + maxPdfWidth + " | total PDF Height: " + totalPdfHeight + " | screenWidthRatio:" + screenWidthRatio);
            maxScaledWidth = (int) (maxPdfWidth * screenWidthRatio);
            maxScaledHeight = (int) (maxPdfHeight * screenWidthRatio);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions(Activity thisActivity){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private void render() {
        Log.i("PDF", "render (w/h)---  " + imageWidth + "x" + imageHeight);

        try {
            if (imageWidth > 0 && imageHeight > 0) {
                Log.i(TAG, "maxPdfHeight: " + maxPdfHeight+ " | maxPdfWidth: " + maxPdfWidth);
                Log.i(TAG, "screenHeight: " + imageHeight+ " | screenWidth: " + imageWidth);
                Log.i(TAG, "totalPdfHeight: " + totalPdfHeight);

                // For memory optimisation, we create a bitmap of the full height
                //FIXME revert to maxPdfWidth , totalPdfHeight
//                maxPdfWidth = 419;
//                totalPdfHeight = 595;
//                maxPdfHeight = 595;
                // FIXME
                Bitmap finalBitmap = Bitmap.createBitmap((int) (maxPdfWidth*screenWidthRatio), (int)(totalPdfHeight*screenWidthRatio), Bitmap.Config.ARGB_8888);
                // Then convert it to a canvas (therefore using GPU instead of CPU)
                Canvas canvas = new Canvas(finalBitmap);
                canvas.drawARGB(0, 225, 225, 255);

                // We still need a bitmap to convert the PDF page
                Bitmap currentPageBitmap = null;
                PdfRenderer.Page pdfPage = null;
                int pdfPageHeight = 0, pdfPageWidth = 0;
                float topOffset = 0f, leftOffset = 0f;
                float scale = 1f;
                for (int i = 0; i < renderer.getPageCount(); i++) {
                    //FIXME
//                int i =0;
                    Log.i(TAG, "----------------------- Page "+i+" -------------------------");
                    pdfPage = renderer.openPage(i);
                    pdfPageHeight = pdfPage.getHeight();
                    pdfPageWidth = pdfPage.getWidth();
                    Log.i(TAG, "pdfPageHeight: " + pdfPageHeight + " | pdfPageWidth: " + pdfPageWidth);

                    // Calculate ratio maxHeight/pageHeight

                    if(orientation == PORTRAIT) {
                        scale = (float) pdfPageHeight / maxPdfHeight;
                    }else{
                        scale = (float) pdfPageWidth / maxPdfWidth;
                    }
                    Log.i(TAG, "Scale: "+scale);

                    // scale width based on ratio
                    pdfPageWidth = (int) (pdfPageWidth*scale);
                    pdfPageHeight = (int) (pdfPageHeight*scale);
                    Log.i(TAG, "pdfPageHeight: " + pdfPageHeight + " | pdfPageWidth: " + pdfPageWidth);

                    // Scaling the entire page
                    pdfPageWidth = (int) (pdfPageWidth * screenWidthRatio);
                    pdfPageHeight = (int) (pdfPageHeight * screenWidthRatio);
                    Log.i(TAG, "pdfPageHeight: " + pdfPageHeight + " | pdfPageWidth: " + pdfPageWidth);

                    // All page should have the same height
                    currentPageBitmap = Bitmap.createBitmap(pdfPageWidth, pdfPageHeight, Bitmap.Config.ARGB_8888);
                    pdfPage.render(currentPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    // Calculate the width offset to center the page
                    if(pdfPageWidth < maxPdfWidth){
                        leftOffset = (imageWidth-pdfPageWidth)/2;
                    }else{
                        // reset the left offset
                        leftOffset = 0f;
                    }

                    // Render the bitmap to the canvas
                    canvas.drawBitmap(currentPageBitmap, leftOffset, topOffset, null);
                    pdfPage.close();

                    // Setting the new offset with some padding
                    topOffset += (pdfPageHeight + PDF_PAGE_PADDING);
                    currentPageBitmap.recycle();
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
                Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                imageView.setImageBitmap(decoded);
                imageView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    private Bitmap mergeImages(Bitmap top, Bitmap bottom) {
        Bitmap result = Bitmap.createBitmap(maxScaledWidth, (int)(top.getHeight()*screenWidthRatio)+(int)(bottom.getHeight()*screenWidthRatio)+(int)PDF_PAGE_PADDING, top.getConfig());
        return result;
    }
}
