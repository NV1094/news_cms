package cn.lvdou.vod.horizontal;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.lvdou.vod.R;
import cn.lvdou.vod.base.BaseRecyclerAdapter;
import cn.lvdou.vod.bean.VodBean;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * @author : yjz
 * @date : 2020/10/29 17:50
 * @des :
 */
public class HorizontalAdapter extends BaseRecyclerAdapter<VodBean> {

    private Context context;

    public HorizontalAdapter(@NonNull Context context, List<VodBean> data) {
        super(context, R.layout.item_horizontal, data, true);
        this.context = context;
    }

    @Override
    public void convert(ViewHolder holder, VodBean item, int position) {

        AppCompatImageView imageView = (AppCompatImageView) holder.getView(R.id.item_iv_card_child_icon);
        //缩略图为空时回退到原图，避免图片不显示（与 RankCardItemViewBinder 的做法保持一致）
        String thumb = item.getVod_pic_thumb();
        String img = TextUtils.isEmpty(thumb) ? item.getVodPic() : thumb;

        MultiTransformation multiTransformation = new MultiTransformation(new CenterCrop(),
                new RoundedCornersTransformation(20, 0, RoundedCornersTransformation.CornerType.ALL));

        if (TextUtils.isEmpty(img)) {
            //没有图片地址时显示占位图，防止 RecyclerView 复用残留旧图
            imageView.setImageResource(R.drawable.error);
        } else {
            Glide.with(context)
                    .load(img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(1.0f)
                    .error(R.drawable.error)
                    .dontAnimate()
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(imageView);
        }

        holder.setText(R.id.item_tv_card_child_title, item.getVodName())
                //猜你会追简介
                .setText(R.id.item_tv_card_child_vod_blurb, item.getVodBlurb());
    }
}
