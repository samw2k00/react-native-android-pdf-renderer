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
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PdfPagingView extends RelativeLayout {
    private static final String TAG = "PdfPagingView";
    private static final int PORTRAIT = 0, LANDSCAPE = 1;
    private SubsamplingScaleImageView imageView;
    private int imageWidth, imageHeight, orientation = PORTRAIT;
    private Button previous, next;
    private static int NUM_PAGES = 0;
    private PdfRenderer renderer = null;
    private static ViewPager mPager;
    private String srcPdfFilename;
    private ArrayList<Bitmap> pdfPagesArray = new ArrayList();

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        imageHeight = h;
        imageWidth = w;

        if (imageWidth > imageHeight) {
            orientation = LANDSCAPE;
        }
        Log.i(TAG, "onSizeChanged (w/h): " + w + "/" + h);
        preparePDF();

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

        //ss.currentPage = this.currentPage;

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

        //this.currentPage = ss.currentPage;
    }


    private void preparePDF() {
        if (imageWidth == 0 || imageHeight == 0) {
            Log.i(TAG, "imageWidth || imageHeight == 0");
            return;
        }

        try {
            Log.i(TAG, "preparePDF, " + srcPdfFilename);
            if (new File(srcPdfFilename).canRead()) {
                if(renderer == null){
                    renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(srcPdfFilename), ParcelFileDescriptor.MODE_READ_ONLY));
                }
                NUM_PAGES = renderer.getPageCount();
                for (int i = 0; i < renderer.getPageCount(); i++) {
                    PdfRenderer.Page pdfPage = renderer.openPage(i);
                    pdfPagesArray.add(renderPage(pdfPage));
                    pdfPage.close();
                }

                mPager.setAdapter(new SlidingImageAdapter(getContext(), pdfPagesArray));
            } else {
                Log.e(TAG, "The file doesn't exists...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap renderPage(PdfRenderer.Page pdfPage) {
        Bitmap currentPageBitmap = null;
        try {
            Log.i(TAG, "----------------------- Page " + pdfPage.getIndex() + " -------------------------");
            int pdfPageHeight = pdfPage.getHeight();
            int pdfPageWidth = pdfPage.getWidth();
            Log.i(TAG, "pdfPageWidth: " + pdfPageWidth+ " | pdfPageHeight: " + pdfPageHeight);
            Log.i(TAG, "imageWidth: "+imageWidth + " | imageHeight: " + imageHeight);

            //pdfPageWidth = density * pdfPageWidth / 150;
            //pdfPageHeight = density * pdfPageHeight / 150;

            // All page should have the same height
            currentPageBitmap = Bitmap.createBitmap(pdfPageWidth, pdfPageHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(currentPageBitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(currentPageBitmap, 0, 0, null);

            pdfPage.render(currentPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentPageBitmap;
    }

    private void init() {
        inflate(getContext(), R.layout.activity_pdf_pager, this);
        imageView = (SubsamplingScaleImageView) findViewById(R.id.imagepdf);
        mPager = (ViewPager) findViewById(R.id.pager);

        previous = (Button) findViewById(R.id.pdfPrevious);
        next = (Button) findViewById(R.id.pdfNext);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //currentPage++;
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //currentPage--;
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
