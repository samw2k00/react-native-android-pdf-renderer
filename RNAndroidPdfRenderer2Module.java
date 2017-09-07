
package au.com.intellihealth.android.androidpdfrenderer;


import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class RNAndroidPdfRenderer2Module extends SimpleViewManager<MainActivity> {
    private Context context;

    @Override
    public MainActivity createViewInstance(ThemedReactContext context) {
        return new MainActivity(context,null);
    }

    @Override
    public String getName() {
        return "PDFRenderer";
  }

    @ReactProp(name = "path")
    public void setPath(MainActivity mainActivity, String path) {
        mainActivity.setPath(path);
    }

    public RNAndroidPdfRenderer2Module(ReactApplicationContext reactContext){
        this.context = reactContext;
    }



}