package cn.lvdou.vod.card;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ColorUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.lvdou.vod.App;
import cn.lvdou.vod.R;
import cn.lvdou.vod.bean.VodBean;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.drakeet.multitype.ItemViewBinder;

public class RankCardItemViewBinder extends ItemViewBinder<VodBean, RankCardItemViewBinder.ViewHolder> implements View.OnClickListener {

    private View.OnClickListener mListener;
    private String mDay;

    public RankCardItemViewBinder setActionListener(View.OnClickListener listener) {
        this.mListener = listener;
        return this;
    }

    public RankCardItemViewBinder(String day) {
        mDay = day;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_rank_card, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull VodBean item) {
        if (holder.total_view != null) {
            holder.total_view.setOnClickListener(this);
            holder.total_view.setTag(item);
        }

        holder.tv_tag.setVisibility(View.INVISIBLE);
        holder.iv_tag.setVisibility(View.INVISIBLE);
        switch (holder.getAdapterPosition()) {
            case 0:
                holder.tv_tag.setTextColor(App.getApplication().getContext().getResources().getColor(R.color.white));
                holder.tv_tag.setVisibility(View.VISIBLE);
                holder.iv_tag.setVisibility(View.VISIBLE);
                holder.tv_tag.setText("1");
                holder.iv_tag.setBackgroundResource(R.drawable.icon_hot_list_sort_first_bg);
                break;
            case 1:
                holder.tv_tag.setTextColor(App.getApplication().getContext().getResources().getColor(R.color.white));
                holder.tv_tag.setVisibility(View.VISIBLE);
                holder.iv_tag.setVisibility(View.VISIBLE);
                holder.tv_tag.setText("2");
                holder.iv_tag.setBackgroundResource(R.drawable.icon_hot_list_sort_second_bg);
                break;
            case 2:
                holder.tv_tag.setTextColor(App.getApplication().getContext().getResources().getColor(R.color.white));
                holder.tv_tag.setVisibility(View.VISIBLE);
                holder.iv_tag.setVisibility(View.VISIBLE);
                holder.tv_tag.setText("3");
                holder.iv_tag.setBackgroundResource(R.drawable.icon_hot_list_sort_third_bg);
                break;
            default:
                holder.tv_tag.setTextColor(App.getApplication().getContext().getResources().getColor(R.color.white));
                holder.tv_tag.setVisibility(View.VISIBLE);
                holder.iv_tag.setVisibility(View.VISIBLE);
                holder.tv_tag.setText(String.valueOf(holder.getAdapterPosition() + 1));
                holder.iv_tag.setBackgroundResource(R.drawable.icon_recommend_person_2);
                break;
        }

        holder.tv_name.setText(item.getVod_name());
        Log.d("影视数据", "onBindViewHolder: "+ item );
        String vod_class = item.getVod_class().replaceAll(",", "/");
        holder.tv_type.setText(item.getVod_area() + "/" + vod_class);
        holder.tv_desc.setText(item.getVod_blurb());
        if (item.getType().getTypeName().equals("电影")) {
            // holder.tv_score.setTextColor(ColorUtils.getColor(R.color.colorPrimary)); old 配色
            holder.tv_score.setTextColor(ColorUtils.getColor(R.color.chocolate));
            TextPaint tp = holder.tv_score.getPaint();
            tp.setFakeBoldText(true);
            holder.tv_score.setText(item.getVod_score() + "分");
        } else {
            holder.tv_score.setTextColor(ColorUtils.getColor(R.color.chocolate));
            holder.tv_score.setText(item.getVodRemarks());
            TextPaint tp = holder.tv_score.getPaint();
            tp.setFakeBoldText(false);
        }
        switch (mDay) {
            case "vod_hits_month desc":
                holder.tv_count.setText("" + item.getVod_hits_month() + "");
                break;
            case "vod_hits_week desc":
                holder.tv_count.setText("" + item.getVod_hits_week() + "");
                break;
            case "vod_hits_day desc":
                holder.tv_count.setText("" + item.getVod_hits_day() + "");
                break;
            case "vod_hits desc":
                holder.tv_count.setText("" + item.getVod_hits() + "");
                break;
            default:
                break;
        }
        String url = "";
        if (item.getVod_pic_thumb() != null && !item.getVod_pic_thumb().isEmpty()) {
            url = item.getVod_pic_thumb();
        } else {
            url = item.getVod_pic();
        }
        MultiTransformation mation = new MultiTransformation(new CenterCrop(), new RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL));
        Glide.with(App.getApplication())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.bitmapTransform(mation))

                .into(holder.iv_cover);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onClick(view);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private @NonNull
        final ImageView iv_tag;
        final ImageView iv_cover;
        final TextView tv_score;
        final TextView tv_tag;
        final TextView tv_name;
        final TextView tv_type;
        final TextView tv_desc;
        final TextView tv_count;
        final View total_view;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_tag = itemView.findViewById(R.id.iv_tag);
            tv_tag = itemView.findViewById(R.id.tv_tag);
            iv_cover = itemView.findViewById(R.id.iv_cover);
            tv_score = itemView.findViewById(R.id.tv_score);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_desc = itemView.findViewById(R.id.tv_desc);
            tv_count = itemView.findViewById(R.id.tv_count);
            total_view = itemView.findViewById(R.id.total_view);
        }
    }

    @SuppressWarnings("unused")
    public interface CardItemActionListener {

        void onClickItem(View view, Object item);

    }

}
