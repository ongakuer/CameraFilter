package me.relex.camerafilter.filter;

import android.content.Context;
import me.relex.camerafilter.R;

public class FilterManager {

    private FilterManager() {
    }

    public static IFilter getCameraFilter(FilterType filterType, Context context) {
        switch (filterType) {
            case Normal:
            default:
                return new CameraFilter(context);
            case Blend:
                return new CameraFilterBlend(context, R.drawable.mask);
            case SoftLight:
                return new CameraFilterBlendSoftLight(context, R.drawable.mask);
        }
    }

    public static IFilter getImageFilter(FilterType filterType, Context context) {
        switch (filterType) {
            case Normal:
            default:
                return new ImageFilter(context);
            case Blend:
                return new ImageFilterBlend(context, R.drawable.mask);
            case SoftLight:
                return new ImageFilterBlendSoftLight(context, R.drawable.mask);
        }
    }

    public enum FilterType {
        Normal, Blend, SoftLight
    }
}
