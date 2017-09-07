package au.com.intellihealth.android.androidpdfrenderer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class CustomisedView extends LinearLayout {
  private String text;
  private Context context;
  private TextView textView;

  public CustomisedView(Context context) {
    super(context);
    this.context = context;
    init(null, 0);
  }

  public CustomisedView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    init(attrs, 0);
  }

  public CustomisedView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this.context = context;
    init(attrs, defStyle);
  }

  private void init(AttributeSet attrs, int defStyle) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View v = inflater.inflate(R.layout.main_layout, this, true);

    textView = (TextView) v.findViewById(R.id.text);
  }

  public void setText(String text){
    textView.setText(text);
  }

}
