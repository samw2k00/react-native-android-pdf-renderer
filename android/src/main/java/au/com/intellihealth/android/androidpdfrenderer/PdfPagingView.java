package au.com.intellihealth.android.androidpdfrenderer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;


/**
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PdfPagingView extends RelativeLayout {
  private static final String TAG = "PdfPagingView";
  private static final int PDF_PAGE_PADDING = 5;
  private static final int PORTRAIT = 0, LANDSCAPE = 1;
  private static final String CURRENT_PAGE = "CURRENT_PAGE";
  private SubsamplingScaleImageView imageView;
  private int currentPage = 0, imageWidth, imageHeight, orientation = PORTRAIT;
  private Button previous, next;
  private PdfRenderer renderer;
  private String srcPdfFilename = "test.pdf";

  public PdfPagingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  public PdfPagingView(Context context) {
    super(context);
    init(null, 0);
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
    try {
      File file,folder;
      if (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canRead()) {
        folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      } else if (Environment.getDownloadCacheDirectory().canRead()) {
        folder = Environment.getDownloadCacheDirectory();
      } else {
        folder = Environment.getRootDirectory();
      }
      Log.i(TAG, "PDF Source Folder: " + folder.toString());

      file = new File(folder, srcPdfFilename);
      Log.i(TAG, "PDF Source file: " + folder.toString());


      renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));

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
        float topOffset = 0f, leftOffset = 0f;
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

  private void init(AttributeSet attrs, int defStyle) {

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
