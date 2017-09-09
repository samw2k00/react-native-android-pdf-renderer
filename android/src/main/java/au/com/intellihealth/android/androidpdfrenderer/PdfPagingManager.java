package au.com.intellihealth.android.androidpdfrenderer;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import javax.annotation.Nullable;

/**
 * Created by Ben on 7/09/2017.
 */

public class PdfPagingManager extends SimpleViewManager<PdfPagingView> {
  private ReactApplicationContext mContext;
  private PdfPagingView mPdfPagingView;

  public PdfPagingManager(ReactApplicationContext context) {
    this.mContext = context;
  }

  @Override
  public String getName() {
    return "PdfPagingManager";
  }

  @Override
  protected PdfPagingView createViewInstance(ThemedReactContext reactContext) {
    //Save local instance
    this.mPdfPagingView = new PdfPagingView(mContext);
    return this.mPdfPagingView;
  }

  @ReactProp(name = "path")
  public void setPath(PdfPagingView view, @Nullable String path) {
    view.setPath(path);
  }

}
