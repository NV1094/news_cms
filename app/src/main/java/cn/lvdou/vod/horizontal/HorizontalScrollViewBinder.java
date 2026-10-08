package cn.lvdou.vod.horizontal;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.lvdou.vod.R;
import cn.lvdou.vod.ad.AdWebView;
import cn.lvdou.vod.base.BaseItemClickListener;
import cn.lvdou.vod.base.BaseRecyclerAdapter;
import cn.lvdou.vod.bean.RecommendBean2;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.bean.NewRecommendBean2;
import cn.lvdou.vod.utils.MMkvUtils;
import io.reactivex.disposables.Disposable;
import me.drakeet.multitype.ItemViewBinder;

/**
 * @author : yjz
 * @date : 2020/10/29 17:41
 * @des :
 */
public class HorizontalScrollViewBinder extends ItemViewBinder<NewRecommendBean2.DataBean, HorizontalScrollViewBinder.ViewHolder> {

    private BaseItemClickListener baseItemClickListener;
    private StartBean.Ad ad;
    Disposable  disposable1;
    public HorizontalScrollViewBinder setBaseItemClickListener(BaseItemClickListener baseItemClickListener) {
        this.baseItemClickListener = baseItemClickListener;
        StartBean startBean = MMkvUtils.Companion.Builds().loadStartBean("");
        if (startBean != null) {
            if (startBean.getAds() != null && startBean.getAds().getIndex() != null) {
                ad = startBean.getAds().getIndex();
            }
        }
        return this;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_scroller, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull NewRecommendBean2.DataBean item) {
        if (ad != null && !StringUtils.isEmpty(ad.getDescription()) && ad.getStatus() == 1) {
            holder.adWebView.setVisibility(View.VISIBLE);
            holder.adWebView.loadHtmlBody(ad.getDescription());
        } else {
            holder.adWebView.setVisibility(View.GONE);
        }
        RecommendBean2 recommendBean2 = item.getZhui().get(0);
        holder.textView.setText(recommendBean2.getVod_type_name());
        holder.setZhuiBeanList(recommendBean2.getVod_list());
        String home_small_icons = Objects.requireNonNull(MMkvUtils.Companion.Builds().loadStartBean("")).getApp_ui_home_small_icons();
        if (home_small_icons == null || home_small_icons.equals("0") || home_small_icons.equals("")) {
            holder.typeIcon.setVisibility(View.GONE);
        }else{
            holder.typeIcon.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(recommendBean2.getVod_type_img()).into(holder.typeIcon);
        }

        holder.horizontalAdapter.setOnRecyclerViewItemClickListen(new BaseRecyclerAdapter.OnRecyclerViewItemClickListen() {
            @Override
            public void onItemClickListen(View view, int position) {
                baseItemClickListener.onClickItem(view, holder.horizontalAdapter.getItem(position));
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView recyclerView;
        private List<VodBean> zhuiBeanList = new ArrayList<>();
        private HorizontalAdapter horizontalAdapter;
        private TextView textView;
        private ImageView typeIcon;
        private @NonNull
        final AdWebView adWebView;
        //猜你会追
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            adWebView  = itemView.findViewById(R.id.adWebView);
            recyclerView = itemView.findViewById(R.id.recycle_view);
            textView = itemView.findViewById(R.id.item_tv_top_title);
            typeIcon = itemView.findViewById(R.id.type_icon);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(new HorizontalItemDecoration(3, itemView.getContext()));
            horizontalAdapter = new HorizontalAdapter(itemView.getContext(), zhuiBeanList);
            recyclerView.setAdapter(horizontalAdapter);
        }

        public void setZhuiBeanList(List<VodBean> zhuiBeanList) {
            horizontalAdapter.clear();
            horizontalAdapter.addAll(zhuiBeanList);
        }
    }
}
