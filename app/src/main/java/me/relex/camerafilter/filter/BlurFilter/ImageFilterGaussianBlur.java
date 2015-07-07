package me.relex.camerafilter.filter.BlurFilter;

import android.content.Context;
import me.relex.camerafilter.filter.CameraFilter;
import me.relex.camerafilter.filter.FilterGroup;

public class ImageFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public ImageFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new ImageFilterGaussianSingleBlur(context, blur, false));
        addFilter(new ImageFilterGaussianSingleBlur(context, blur, true));
    }
}
