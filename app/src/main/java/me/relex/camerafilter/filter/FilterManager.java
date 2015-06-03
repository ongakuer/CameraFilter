package me.relex.camerafilter.filter;

import android.content.Context;
import me.relex.camerafilter.R;
import me.relex.camerafilter.gles.IFilter;

public class FilterManager {

    private FilterManager() {
    }

    public static IFilter getFilter(FilterType filterType, Context context) {
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

    public enum FilterType {
        Normal, Blend, SoftLight
    }
}
