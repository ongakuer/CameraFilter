package me.relex.camerafilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import me.relex.camerafilter.filter.FilterManager;
import me.relex.camerafilter.image.ImageEglSurface;
import me.relex.camerafilter.image.ImageRenderer;

public class ImageFilterActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImageView;
    private ImageRenderer mImageRenderer;
    private FilterTask mFilterTask;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_filter);

        mImageView = (ImageView) findViewById(R.id.image_view);

        findViewById(R.id.filter_normal).setOnClickListener(this);
        findViewById(R.id.filter_tone_curve).setOnClickListener(this);
        findViewById(R.id.filter_soft_light).setOnClickListener(this);

        mImageRenderer =
                new ImageRenderer(getApplicationContext(), FilterManager.FilterType.Normal);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_normal:
                startFilterTask(FilterManager.FilterType.Normal);
                break;
            case R.id.filter_tone_curve:
                startFilterTask(FilterManager.FilterType.ToneCurve);
                break;
            case R.id.filter_soft_light:
                startFilterTask(FilterManager.FilterType.SoftLight);
                break;
        }
    }

    private void startFilterTask(FilterManager.FilterType filterType) {
        if (mFilterTask == null || mFilterTask.getStatus() != AsyncTask.Status.RUNNING) {
            mFilterTask = new FilterTask(getApplicationContext(), mImageRenderer, filterType);
            mFilterTask.execute();
        }
    }

    public class FilterTask extends AsyncTask<Void, Void, Bitmap> {

        private Context mContext;
        private FilterManager.FilterType mFilterType;
        private ImageRenderer mRenderer;

        public FilterTask(Context context, ImageRenderer renderer,
                FilterManager.FilterType filterType) {
            mFilterType = filterType;
            mContext = context;
            mRenderer = renderer;
        }

        @Override protected Bitmap doInBackground(Void... params) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            final Bitmap bitmap =
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.raw_image,
                            options);
            ImageEglSurface imageEglSurface =
                    new ImageEglSurface(bitmap.getWidth(), bitmap.getHeight()); //设置输出宽高,
            imageEglSurface.setRenderer(mRenderer);
            mRenderer.changeFilter(mFilterType);
            mRenderer.setImageBitmap(bitmap);
            imageEglSurface.drawFrame();
            Bitmap filterBitmap = imageEglSurface.getBitmap();
            imageEglSurface.release();
            mRenderer.destroy();

            return filterBitmap;
        }

        @Override protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap == null) {
                return;
            }

            mImageView.setImageBitmap(bitmap);
        }
    }
}
