package me.relex.camerafilter.filter;

import android.content.Context;
import me.relex.camerafilter.R;

public class FilterManager {

    private static int mCurveIndex;
    private static int[] mCurveArrays = new int[] {
            R.raw.cross_1, R.raw.cross_2, R.raw.cross_3, R.raw.cross_4, R.raw.cross_5,
            R.raw.cross_6, R.raw.cross_7, R.raw.cross_8, R.raw.cross_9, R.raw.cross_10,
            R.raw.cross_11,
    };

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
            case ToneCurve:
                mCurveIndex++;
                if (mCurveIndex > 10) {
                    mCurveIndex = 0;
                }
                return new CameraFilterToneCurve(context,
                        context.getResources().openRawResource(mCurveArrays[mCurveIndex]));
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
            case ToneCurve:
                mCurveIndex++;
                if (mCurveIndex > 10) {
                    mCurveIndex = 0;
                }
                return new ImageFilterToneCurve(context,
                        context.getResources().openRawResource(mCurveArrays[mCurveIndex]));
        }
    }

    public enum FilterType {
        Normal, Blend, SoftLight, ToneCurve
    }
}
