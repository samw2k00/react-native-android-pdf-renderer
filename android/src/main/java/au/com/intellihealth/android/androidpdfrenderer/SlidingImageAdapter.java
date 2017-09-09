package au.com.intellihealth.android.androidpdfrenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

/**
 * Created by Ben on 9/09/2017.
 */

public class SlidingImageAdapter extends PagerAdapter {
    private static final String TAG = "SlidingImageAdapter";
    private ArrayList<Bitmap> IMAGES;
    private LayoutInflater inflater;
    private Context context;
    public SlidingImageAdapter(Context context, ArrayList<Bitmap> IMAGES) {
        Log.i(TAG, "SlidingImageAdapter instantiated");
        this.context = context;
        this.IMAGES=IMAGES;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        Log.i(TAG, "instantiateItem - position");
        View imageLayout = inflater.inflate(R.layout.activity_pdf_page, view, false);

        assert imageLayout != null;
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) imageLayout.findViewById(R.id.imagepdf);


        ImageSource imgSrc = ImageSource.cachedBitmap(IMAGES.get(position));
        imageView.setImage(imgSrc);

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}
