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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

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
    private Button previous, next;
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
            }else{
                Log.e(TAG, "The file doesn't exists...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void render() {
        Log.i("PDF", "render (w/h)---  " + imageWidth + "x" + imageHeight);

        try {
            if (imageWidth > 0 && imageHeight > 0) {
                if (currentPage < 0) {
                    currentPage = 0;
                } else if (currentPage > renderer.getPageCount() - 1) {
                    currentPage = renderer.getPageCount() - 1;
                }
                //TODO don't render if we're already on the last page... (disable next/previous button)

                Log.i(TAG, "screenHeight: " + imageHeight + " | screenWidth: " + imageWidth);

                // We still need a bitmap to convert the PDF page
                int pdfPageHeight = 0, pdfPageWidth = 0;
                float scale = 1f;

                Log.i(TAG, "----------------------- Page " + currentPage + " -------------------------");
                PdfRenderer.Page pdfPage = renderer.openPage(currentPage);
                pdfPageHeight = pdfPage.getHeight();
                pdfPageWidth = pdfPage.getWidth();
                Log.i(TAG, "pdfPageHeight: " + pdfPageHeight + " | pdfPageWidth: " + pdfPageWidth);

                int density = getResources().getDisplayMetrics().densityDpi;
                Log.i(TAG, "density: " + density);
                pdfPageWidth = density * pdfPageWidth / 150;
                pdfPageHeight = density * pdfPageHeight / 150;

                // All page should have the same height
                Bitmap currentPageBitmap = Bitmap.createBitmap(pdfPageWidth, pdfPageHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(currentPageBitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(currentPageBitmap, 0, 0, null);

                pdfPage.render(currentPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                ImageSource imgSrc = ImageSource.cachedBitmap(currentPageBitmap);
                imageView.setImage(imgSrc);
                imageView.invalidate();
                pdfPage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        inflate(getContext(), R.layout.activity_pdf_paging, this);
        imageView = (SubsamplingScaleImageView) findViewById(R.id.imagepdf);

        previous = (Button) findViewById(R.id.pdfPrevious);
        next = (Button) findViewById(R.id.pdfNext);

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

        /*
        imageView.post(new Runnable() {
           @Override
           public void run() {
               imageHeight = imageView.getMeasuredHeight();
               imageWidth = imageView.getMeasuredWidth();
           }
        });
        */
            /*
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Here you can get the size :)
                imageWidth = imageView.getWidth();
                //getResources().getDisplayMetrics().density;
                imageHeight = imageView.getHeight();

                if (imageWidth > imageHeight) {
                    orientation = LANDSCAPE;
                }

                preparePDF();
                render();
            }
        });
*/

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
