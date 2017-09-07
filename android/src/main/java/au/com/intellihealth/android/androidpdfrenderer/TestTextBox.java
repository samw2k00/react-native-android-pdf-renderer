package au.com.intellihealth.android.androidpdfrenderer;

import android.support.annotation.Nullable;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Created by samwei on 7/9/17.
 */

public class TestTextBox extends SimpleViewManager {

    private Context context;

    public TestTextBox(ReactApplicationContext reactContext){
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "TestTextBox";
    }

    @Override
    protected CustomisedView createViewInstance(ThemedReactContext reactContext) {
      CustomisedView newText = new CustomisedView(reactContext);
        return newText;
    }

    @ReactProp(name = "text")
    public void setText(CustomisedView view, @Nullable String text) {
        view.setText(text);
    }
}
