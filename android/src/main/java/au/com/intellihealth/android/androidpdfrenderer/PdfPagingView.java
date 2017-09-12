package au.com.intellihealth.android.androidpdfrenderer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
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
    private String srcPdfFilename;
    private MuPDFCore core;
    private RelativeLayout mainLayout;
    private MuPDFReaderView readerView;
    private int currentPage;

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

    public void setCurrentPage(int page) {
        Log.i(TAG, "Setting PDF page to : " + page);
        this.currentPage = page;
    }

    private void preparePDF() {
        Log.i(TAG, "preparePDF, " + srcPdfFilename);
        try {
            if (new File(srcPdfFilename).canRead()) {
                core = new MuPDFCore(getContext(), srcPdfFilename);
                MuPDFPageAdapter pageAdapter = new MuPDFPageAdapter(getContext(), new FilePicker.FilePickerSupport() {
                    @Override
                    public void performPickFor(FilePicker filePicker) {}
                }, core);
                readerView = new MuPDFReaderView(getContext());
                readerView.setAdapter(pageAdapter);


                mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
                mainLayout.addView(readerView);
                readerView.bringToFront();
                mainLayout.invalidate();

                WritableMap event = Arguments.createMap();
                event.putString("message", "loadComplete|" + core.countPages());
                ReactContext reactContext = (ReactContext) getContext();
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        getId(),
                        "topChange",
                        event
                );
            } else {
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
        } catch (Exception e) {
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

    private void init() {
        //inflate(getContext(), R.layout.main_layout, this);
    }

}
