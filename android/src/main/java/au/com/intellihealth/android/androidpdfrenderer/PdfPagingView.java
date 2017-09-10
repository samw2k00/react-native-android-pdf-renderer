package au.com.intellihealth.android.androidpdfrenderer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.File;
import java.io.IOException;

/**
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PdfPagingView extends RelativeLayout {
    private static final String TAG = "PdfPagingView";
    private static final int PORTRAIT = 0, LANDSCAPE = 1;
    private SubsamplingScaleImageView imageView;
    private int currentPage = 0, imageWidth, imageHeight, orientation = PORTRAIT;
    private FloatingActionButton previous, next;
    private PdfRenderer renderer;
    private String srcPdfFilename;

    public PdfPagingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PdfPagingView(Context context) {
        super(context);
        init();
    }

    public void setPath(String path) {
        Log.i(TAG, "PDF Source path: " + path);
        srcPdfFilename = path;
        preparePDF();
    }

    public void setCurrentPage(int page){
        Log.i(TAG, "Setting PDF page to : " + page);
        this.currentPage = page;
        render();
    }

    public int getPageCount(){
        if(renderer != null){
            Log.i(TAG, "Returning page Count : " + renderer.getPageCount());
            return renderer.getPageCount();
        }
        Log.w(TAG, "Renderer not available, returning 0");
        return 0;
    }

    public int getCurrentPage(){
        Log.i(TAG, "Returning current page index : " + currentPage);
        return currentPage;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        imageHeight = h;
        imageWidth = w;

        if (imageWidth > imageHeight) {
            orientation = LANDSCAPE;
        }

        Log.i(TAG, "onSizeChanged (w/h): "+w+"/"+h);
        render();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        //begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        //end

        ss.currentPage = this.currentPage;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        this.currentPage = ss.currentPage;
    }

    private void preparePDF() {
        Log.i(TAG, "preparePDF, "+srcPdfFilename);
        try {
            if(new File(srcPdfFilename).canRead()){
                renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(srcPdfFilename), ParcelFileDescriptor.MODE_READ_ONLY));
                int totalPage = renderer.getPageCount();
                WritableMap event = Arguments.createMap();
                event.putString("message", "loadComplete|"+totalPage);
                ReactContext reactContext = (ReactContext) getContext();
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        getId(),
                        "topChange",
                        event
                );
            }else{
                Log.e(TAG, "The file doesn't exists...");
                WritableMap event = Arguments.createMap();
                event.putString("message", "error|" + "The file doesn't exists");
                ReactContext reactContext = (ReactContext) getContext();
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        getId(),
                        "topChange",
                        event
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
            WritableMap event = Arguments.createMap();
            event.putString("message", "error|" + e.toString());
            ReactContext reactContext = (ReactContext) getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    getId(),
                    "topChange",
                    event
            );
        }
    }
    private void render() {
        Log.i("PDF", "render (w/h)---  " + imageWidth + "x" + imageHeight);

        try {
            if (imageWidth > 0 && imageHeight > 0) {
                if (currentPage < 0) {
                    currentPage = 0;
                    previous.setEnabled(false);
                } else if (currentPage > renderer.getPageCount() - 1) {
                    currentPage = renderer.getPageCount() - 1;
                    next.setEnabled(false);
                }else{
                    if(!previous.isEnabled()){
                        previous.setEnabled(true);
                    }
                    if(!next.isEnabled()){
                        next.setEnabled(true);
                    }
                }

                Log.i(TAG, "screenHeight: " + imageHeight + " | screenWidth: " + imageWidth);

                // We still need a bitmap to convert the PDF page
                int pdfPageHeight = 0, pdfPageWidth = 0;

                Log.i(TAG, "----------------------- Page " + currentPage + " -------------------------");
                PdfRenderer.Page pdfPage=null;
                try {
                    pdfPage = renderer.openPage(currentPage);
                } finally {
                    if(pdfPage != null){
                        pdfPage.close();
                        pdfPage = renderer.openPage(currentPage);
                    }
                }
                pdfPageHeight = pdfPage.getHeight();
                pdfPageWidth = pdfPage.getWidth();
                Log.i(TAG, "pdfPageHeight: " + pdfPageHeight + " | pdfPageWidth: " + pdfPageWidth);

                // Detect if the PDf page is in landscape or portrait
                int pdfPageOrientation = PORTRAIT;
                if(pdfPageWidth > pdfPageHeight){
                    pdfPageOrientation = LANDSCAPE;
                }
                // Check if we need to scale up or down the page.
                // Since we want good quality even in landscape, we will base our scale ONLY
                // on height ratio...
                // This means the landscape pages WILL be bigger in size (memory).
                float scale = (float)imageHeight / (float)pdfPageHeight;
                Log.i(TAG, "scale: " + scale);

                pdfPageWidth = (int) (scale * pdfPageWidth);
                pdfPageHeight = (int) (scale * pdfPageHeight);
                Log.i(TAG, "scaledPdfPageHeight: " + pdfPageHeight + " | scaledPdfPageWidth: " + pdfPageWidth);

                // All page should have the same height
                Bitmap currentPageBitmap = Bitmap.createBitmap(pdfPageWidth, pdfPageHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(currentPageBitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(currentPageBitmap, 0, 0, null);

                pdfPage.render(currentPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                pdfPage.close();

                ImageSource imgSrc = ImageSource.cachedBitmap(currentPageBitmap);
                imageView.setImage(imgSrc);
                imageView.invalidate();

                // broadcast to REACT NATIVE whats the current page
                WritableMap event = Arguments.createMap();
                event.putString("message", "currentPage|"+ (currentPage+ 1));
                ReactContext reactContext = (ReactContext) getContext();
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        getId(),
                        "topChange",
                        event
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        inflate(getContext(), R.layout.activity_pdf_paging, this);
        imageView = (SubsamplingScaleImageView) findViewById(R.id.imagepdf);

        previous = (FloatingActionButton) findViewById(R.id.pdfPrevious);
        next = (FloatingActionButton) findViewById(R.id.pdfNext);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage++;
                render();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage--;
                render();
            }
        });
    }


    static class SavedState extends BaseSavedState {
        int currentPage;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.currentPage);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
