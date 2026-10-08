package cn.lvdou.vod.ui.rank;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.android.material.tabs.TabLayout;
import com.youxiao.ssp.ad.bean.SSPAd;
import com.youxiao.ssp.ad.core.AdClient;
import com.youxiao.ssp.ad.listener.AdLoadAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import cn.lvdou.vod.App;
import cn.lvdou.vod.R;
import cn.lvdou.vod.base.BaseMainFragment;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.RankOrderEvent;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.TypeBean;
import cn.lvdou.vod.netservice.TypeService;
import cn.lvdou.vod.network.RetryWhen;
import cn.lvdou.vod.ui.home.RankPagerFragmentAdapter;
import cn.lvdou.vod.ui.seek.SeekActivity;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.DensityUtils;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.Retrofit2Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RankFragmentB extends BaseMainFragment {
    @BindView(R.id.tv_title)
    TextView titleTv;
    Disposable disposable1;
    @BindView(R.id.tl_rank)
    TabLayout tl_rank;
    private RankPagerFragmentAdapter pagerFragmentAdapter;
    @BindView(R.id.vp_rank)
    ViewPager vp_rank;
    @BindView(R.id.rl_day)
    RelativeLayout rl_day;
    @BindView(R.id.tv_day)
    TextView tv_day;
    @BindView(R.id.hot_data_up_time)
    TextView hot_data_up_time;
    @BindView(R.id.iv_show)
    ImageView iv_show;
    @BindView(R.id.app_my_sc_sch)
    ImageView app_my_sc_sch;
    @BindView(R.id.linear_Banners)
    FrameLayout linear_Banners;

    private TabLayout.Tab oldTab;
    private View oldView;
    public static volatile int mPosition;
    private boolean isInit = false;
    private int mDay = 1;
    private RankPopup popup;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_rankb;
    }

    public static RankFragmentB newInstance() {
        Bundle args = new Bundle();
        RankFragmentB fragment = new RankFragmentB();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!isInit) getData();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopGet();
    }

    @Override
    public void onDestroyView() {
        if (disposable1 != null && !disposable1.isDisposed()) {
            disposable1.dispose();
            disposable1 = null;
        }
        super.onDestroyView();
    }


    @SuppressLint("SetTextI18n")
    private void initView() {
        popup = new RankPopup(getActivity());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        hot_data_up_time.setText("基于实时热度排行 " + df.format(new Date()) + " 更新");
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                iv_show.setBackgroundResource(R.drawable.icon_open);
            }
        });
        rl_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popup.isShowing()) {
                    popup.dismiss();
                } else {
                    iv_show.setBackgroundResource(R.drawable.icon_close);
                    popup.showAtLocation(rl_day, Gravity.RIGHT | Gravity.TOP, DensityUtils.INSTANCE.dp2px(getActivity(), 5), DensityUtils.INSTANCE.dp2px(getActivity(), 230));
                }
            }
        });
        app_my_sc_sch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(SeekActivity.class);
            }
        });
        vp_rank.setOffscreenPageLimit(1);
        vp_rank.setAdapter(pagerFragmentAdapter = new RankPagerFragmentAdapter(getChildFragmentManager()));
        tl_rank.setupWithViewPager(vp_rank);
        TabLayout.OnTabSelectedListener onTabSelectedListener;
        tl_rank.addOnTabSelectedListener(onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LogUtils.e("选项卡切换！" + tab.toString());
                if (tab != oldTab) {
                    oldTab = tab;
                    if (oldView != null) oldView.animate().scaleX(1).scaleY(1).start();
                    LinearLayout linearLayout = tab.view;
                    linearLayout.setClipChildren(false);
                    linearLayout.setClipToPadding(false);
                    View view = linearLayout.getChildAt(1);
                    view.animate().scaleX(1.1f).scaleY(1.1f).start();
                    mPosition = tab.getPosition();
                    oldView = view;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        TabLayout.Tab tab = tl_rank.getTabAt(0);
        if (tab != null) {
            onTabSelectedListener.onTabSelected(tab);
        }
//        handler.postDelayed(runnable, 1000);
    }

//    private static final int MAX_TIME = 30;
//    private int start_time = MAX_TIME;
//    private Handler handler = new Handler();
//    private boolean isClosed = false;
//
//    private Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            if (!isClosed) {
//                isClosed = true;
//                StartBean startBean = MMkvUtils.Companion.Builds().loadStartBean("");
//                if (startBean != null && startBean.getAds() != null && startBean.getAds().getSdk_banner() != null) {
//                    StartBean.Ad banner = startBean.getAds().getSdk_banner();
//                    if (App.SdkSwitch && banner.getDescription() != null && banner.getStatus() != 0) {
//                        AdClient adClient = new AdClient(_mActivity);
//                        adClient.requestBannerAd(linear_Banners, banner.getDescription(), new AdLoadAdapter() {
//                            @Override
//                            public void onAdLoad(SSPAd ad) {
//                                super.onAdLoad(ad);
//                                linear_Banners.setVisibility(View.VISIBLE);
//                            }
//
//                            @Override
//                            public void onError(int var1, String var2) {
//                                linear_Banners.setVisibility(View.GONE);
//                            }
//                        });
//                    }
//                }
//            }
//            start_time -= 1;
//            if (start_time >= 0) {
//                handler.postDelayed(runnable, 1000);
//            } else {
//                linear_Banners.setVisibility(View.GONE);
//            }
//        }
//    };

    private Disposable disposable;

    private void getData() {
        isInit = false;
        TypeService typeService = Retrofit2Utils.INSTANCE.createByGson(TypeService.class);

        typeService.getTypeList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<PageResult<TypeBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable != null && !disposable.isDisposed()) {
                            disposable.dispose();
                            disposable = null;
                        }
                        disposable = d;
                    }

                    @Override
                    public void onNext(PageResult<TypeBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                isInit = true;
                                List<TypeBean> list = result.getData().getList();
                                List<TypeBean> newList = new ArrayList<>();
                                ArrayList<Integer> sortList = new ArrayList<>();
                                for (TypeBean bean : list) {
                                    sortList.add(bean.getType_sort());
                                }
                                Collections.sort(sortList);

                                for (int i = 0; i < sortList.size(); i++) {
                                    for (int j = 0; j < list.size(); j++) {
                                        TypeBean bean = list.get(j);
                                        if (sortList.get(i) == bean.getType_sort()) {
                                            newList.add(bean);
                                        }
                                    }
                                }
                                vp_rank.setAdapter(pagerFragmentAdapter = new RankPagerFragmentAdapter(getChildFragmentManager()));
                                if (pagerFragmentAdapter != null) {
                                    pagerFragmentAdapter.addData(newList);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void stopGet() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(RankOrderEvent event) {
        mDay = event.rankOrder;
        switch (event.rankOrder) {
            case 1:
                tv_day.setText("总榜");
                break;
            case 2:
                tv_day.setText("月榜");
                break;
            case 3:
                tv_day.setText("周榜");
                break;
            case 4:
                tv_day.setText("日榜");
                break;
        }
    }
}
