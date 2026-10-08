package cn.lvdou.vod.ui.live;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import cn.lvdou.vod.App;
import cn.lvdou.vod.R;
import cn.lvdou.vod.bean.CardBean;
import cn.lvdou.vod.bean.LiveBean;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.pip.PIPManager;
import cn.lvdou.vod.ui.login.LoginActivity;
import cn.lvdou.vod.ui.play.NewPlayActivity;
import cn.lvdou.vod.ui.play.X5WebActivity;
import cn.lvdou.vod.ui.widget.HitDialog;
import cn.lvdou.vod.utils.LoginUtils;
import cn.lvdou.vod.utils.MMkvUtils;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

public class LiveAdpter extends RecyclerView.Adapter<LiveAdpter.ViewHolder> {
    private List<LiveBean> coll;// 消息对象数组
    private LayoutInflater mInflater;
    private Context context;

    public LiveAdpter(Context context, List<LiveBean> coll) {
        this.coll = coll;
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.live_video_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LiveBean entity = coll.get(position);
        MultiTransformation mation = new MultiTransformation<>(new CenterCrop(), new RoundedCornersTransformation(20, 0, RoundedCornersTransformation.CornerType.ALL));
        Glide.with(holder.itemView.getContext())
                .load(entity.getImg())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.bitmapTransform(mation))
                .into(holder.coverImg);
        holder.tvName.setText(entity.getName());
        holder.llTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartBean.Ad Live_vip = Objects.requireNonNull(MMkvUtils.Companion.Builds().loadStartBean("")).getAds().getLive_vip();
                if (Live_vip == null || Live_vip.getStatus() == 0) { //未开启发现页权限
                    getX5(entity);
                } else {
                    if (!LoginUtils.checkLogin2(context)) {
                        (new HitDialog(context)).setTitle("提示").setMessage("登录后才能访问，确定登录。").setOnHitDialogClickListener(new HitDialog.OnHitDialogClickListener() {
                            public void onCancelClick(@NotNull HitDialog dialog) {
                                Intrinsics.checkParameterIsNotNull(dialog, "dialog");
                                super.onCancelClick(dialog);
                            }

                            public void onOkClick(@NotNull HitDialog dialog) {
                                Intrinsics.checkParameterIsNotNull(dialog, "dialog");
                                super.onOkClick(dialog);
                                ActivityUtils.startActivity(LoginActivity.class);
                            }
                        }).show();
                    } else {
                        if (LoginUtils.checkVIP(context, "此页面仅对会员开放,是否开通")) {
                            getX5(entity);
                        }
                    }
                }
            }
        });

    }

    public long getItemId(int position) {
        return position;
    }

    public void getX5(LiveBean entity){
        if (LoginUtils.checkLogin(context)) {
            if (entity.getUrl().endsWith("mp4") || entity.getUrl().endsWith("m3u8")) {
                Intent intent = new Intent(context, X5WebActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", entity.getUrl());
                bundle.putString("title", "");
                intent.putExtras(bundle);
                ActivityUtils.startActivity(intent);
            } else {
                Intent intent = new Intent(context, X5WebActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", entity.getUrl());
                bundle.putString("title", entity.getName());
                intent.putExtras(bundle);
                ActivityUtils.startActivity(intent);
            }
        }
    }

    @Override
    public int getItemCount() {
        return coll.size();
    }

    public void clearData() {
        this.coll.clear();
        notifyDataSetChanged();
    }

    public void refresh(List<LiveBean> data) {
        reset(data);
        notifyDataSetChanged();
    }

    public void reset(List<LiveBean> data) {
        if (!data.isEmpty()) {
            this.coll.clear();
            for (int i = 0; i < data.size(); i++) {
                this.coll.add(data.get(i));
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout llTotal;
        public TextView tvName;
        public ImageView coverImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llTotal = (LinearLayout) itemView.findViewById(R.id.ll_total);
            coverImg = (ImageView) itemView.findViewById(R.id.live_cover);
            tvName = (TextView) itemView.findViewById(R.id.live_name);
        }
    }
}
