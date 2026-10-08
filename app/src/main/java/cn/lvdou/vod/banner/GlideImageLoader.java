package cn.lvdou.vod.banner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.youth.banner.loader.ImageLoader;

import cn.lvdou.vod.R;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.yokeyword.fragmentation.SupportActivity;


@SuppressWarnings("unused,WeakerAccess")
public class GlideImageLoader extends ImageLoader {

    private static final long serialVersionUID = 7338929115611451024L;

    private int errorId = R.drawable.blur_bg;

    private getBitmapListener bitmapListener;

    public GlideImageLoader setBitmapListener(getBitmapListener bitmapListener) {
        this.bitmapListener = bitmapListener;
        return this;
    }

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        BannerData data = (BannerData) path;
        if (data == null) return;

        MultiTransformation multiTransformation = new MultiTransformation(new CenterCrop(),
                new RoundedCornersTransformation(20, 0, RoundedCornersTransformation.CornerType.ALL));

        Log.e("TAG", "displayImage: " + data.getBannerImg() );

        Glide.with(context)
                .asBitmap()
                .load(data.getBannerImg())
                .thumbnail(1.0f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .error(errorId)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (bitmapListener != null)
                            bitmapListener.getBitmap(ImageUtils.getBitmap(errorId), String.valueOf(model));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (bitmapListener != null)
                            bitmapListener.getBitmap(resource, String.valueOf(model));
                        return false;
                    }
                })
                .into(imageView);
    }

    public interface getBitmapListener {
        void getBitmap(Bitmap bitmap, String key);
    }

}
