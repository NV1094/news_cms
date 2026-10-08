package cn.lvdou.vod.banner;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.StringUtils;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import cn.lvdou.vod.R;
import cn.lvdou.vod.ad.AdWebView;
import cn.lvdou.vod.bean.BannerBean;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.utils.MMkvUtils;
import me.drakeet.multitype.ItemViewBinder;

@SuppressWarnings("unused")
public class BannerItemViewBinder extends ItemViewBinder<BannerBean, BannerItemViewBinder.ViewHolder> implements BlurBanner.onBannerActionListener {

    private BlurBanner.onBannerActionListener onActionListener;
    private StartBean.Ad ad;
//    private Activity activity;

    public BannerItemViewBinder setOnActionListener(BlurBanner.onBannerActionListener onActionListener, int index) {
        this.onActionListener = onActionListener;
        if (index == -1) {
            return this;
        }
        StartBean startBean = MMkvUtils.Companion.Builds().loadStartBean("");
        if (startBean != null) {
            if (index == 0) {
                if (startBean.getAds() != null && startBean.getAds().getIndex() != null) {
                    ad = startBean.getAds().getIndex();
                }
            } else if (index == 1) {
                if (startBean.getAds() != null && startBean.getAds().getVod() != null) {
                    ad = startBean.getAds().getVod();
                }
            } else if (index == 2) {
                if (startBean.getAds() != null && startBean.getAds().getSitcom() != null) {
                    ad = startBean.getAds().getSitcom();
                }
            } else if (index == 3) {
                if (startBean.getAds() != null && startBean.getAds().getVariety() != null) {
                    ad = startBean.getAds().getVariety();
                }
            } else if (index == 4) {
                if (startBean.getAds() != null && startBean.getAds().getCartoon() != null) {
                    ad = startBean.getAds().getCartoon();
                }
            }
        }
        return this;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_banner, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull BannerBean item) {
        String uihometopbg = Objects.requireNonNull(MMkvUtils.Companion.Builds().loadStartBean("")).getUi_home_topbg();
        if (uihometopbg != null && uihometopbg.equals("1")) { //控制BAN边距
            holder.myBanner.setVisibility(View.GONE);
            holder.myBanners.setVisibility(View.VISIBLE);
            holder.myBanners.setOnBannerActionListener(this);
            holder.myBanners.setDataList(item.getBannerList());
            holder.myBanners.start();
        }else{
            holder.myBanners.setVisibility(View.GONE);
            holder.myBanner.setVisibility(View.VISIBLE);
            holder.myBanner.setOnBannerActionListener(this);
            holder.myBanner.setDataList(item.getBannerList());
            holder.myBanner.start();
        }
//        holder.myBanner.setVisibility(View.VISIBLE);
//        holder.myBanner.setOnBannerActionListener(this);
//        holder.myBanner.setDataList(item.getBannerList());
//        holder.myBanner.start();

        if (ad != null && !StringUtils.isEmpty(ad.getDescription()) && ad.getStatus() == 1) {
            holder.adWebView.setVisibility(View.VISIBLE);
            holder.adWebView.loadHtmlBody(ad.getDescription());
        } else {
            holder.adWebView.setVisibility(View.GONE);
        }

        StartBean.Ad getHome_backg_b = Objects.requireNonNull(MMkvUtils.Companion.Builds().loadStartBean("")).getAds().getHome_backg_b();
        if (getHome_backg_b != null && getHome_backg_b.getStatus() == 1 && getHome_backg_b.getDescription() != null) {
            Picasso.get().load(getHome_backg_b.getDescription()).into(holder.bannerbackg);
        }

//        AdClient adClient = new AdClient(activity);
//        adClient.requestExpressAd("9710", new AdLoadAdapter() {
//            @Override
//            public void onAdLoad(SSPAd ad) {
//                super.onAdLoad(ad);
//                holder.adlayouts.setVisibility(View.VISIBLE);
//                holder.adlayouts.removeAllViews();
//                holder.adlayouts.addView(ad.getView());
//            }
//
//            @Override
//            public void onError(int var1, String var2) {
//                Log.d("你好ssssssss1111", var2);
//                holder.adlayouts.setVisibility(View.GONE);
//            }
//        });
    }

    @Override
    public void onPageChange(int position, Bitmap bitmap) {
        if (onActionListener != null) onActionListener.onPageChange(position, bitmap);
    }

    @Override
    public void onBannerClick(int position, Object o) {
        if (onActionListener != null) onActionListener.onBannerClick(position, o);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private @NonNull
        final BlurBanner<VodBean> myBanner;

        private @NonNull
        final BlurBanner<VodBean> myBanners;

        private @NonNull
        final AdWebView adWebView;

        private @NonNull
        final ImageView bannerbackg;

//        private @NonNull
//        final FrameLayout adlayouts;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            myBanner = itemView.findViewById(R.id.item_banner);
            myBanners = itemView.findViewById(R.id.item_banners);
            adWebView = itemView.findViewById(R.id.adWebView);
//            adlayouts = itemView.findViewById(R.id.ad_layouts);
            bannerbackg = itemView.findViewById(R.id.banner_backg);
        }
    }
}

