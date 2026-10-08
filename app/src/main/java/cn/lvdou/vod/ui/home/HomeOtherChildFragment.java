package cn.lvdou.vod.ui.home;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import cn.lvdou.vod.R;
import cn.lvdou.vod.ads.AdViewBinder;
import cn.lvdou.vod.banner.BannerItemViewBinder;
import cn.lvdou.vod.banner.BlurBanner;
import cn.lvdou.vod.base.BaseItemFragment;
import cn.lvdou.vod.bean.BannerBean;
import cn.lvdou.vod.bean.BaseResult;
import cn.lvdou.vod.bean.CardBean;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.TopBean;
import cn.lvdou.vod.bean.TypeBean;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.card.CardItemViewBinder;
import cn.lvdou.vod.netservice.BannerService;
import cn.lvdou.vod.netservice.CardMoreService;
import cn.lvdou.vod.netservice.TopService;
import cn.lvdou.vod.netservice.VodService;
import cn.lvdou.vod.network.RetryWhen;
import cn.lvdou.vod.ui.play.PlayActivity;
import cn.lvdou.vod.ui.screen.ScreenActivity3;
import cn.lvdou.vod.ui.top.TopItemViewBinder;
import cn.lvdou.vod.utils.AUtils;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.DefaultItemAnimator;
import cn.lvdou.vod.utils.LoginUtils;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.Retrofit2Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.drakeet.multitype.MultiTypeAdapter;

import static net.lucode.hackware.magicindicator.ScrollState.SCROLL_STATE_IDLE;

public class HomeOtherChildFragment extends BaseItemFragment<Type> {

    public static HomeOtherChildFragment newInstance(int position, Type data) {
        Bundle args = new Bundle();
        //设置当前位置
        args.putInt(key_position, position);
        args.putSerializable(key_data, data);
        HomeOtherChildFragment fragment = new HomeOtherChildFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.srl_home_other_child)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.rv_home_other_child)
    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MultiTypeAdapter adapter;
    private List<Object> items = null;
    private boolean isShowFirstItem = true;

    private String[] zlass;
    private int zlass_index;


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_home_other_child;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        //下拉刷新
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        SwipeRefreshLayout.OnRefreshListener onRefreshListener;
        refreshLayout.setOnRefreshListener(onRefreshListener = () -> {
            initData();
            getBannerData();
        });
        refreshLayout.setRefreshing(true);
        onRefreshListener.onRefresh();

    }

    private void initView() {
        adapter = new MultiTypeAdapter();
        //隐藏其他界面的banner
        adapter.register(BannerBean.class, new BannerItemViewBinder().setOnActionListener(new BlurBanner.onBannerActionListener() {
            @Override
            public void onPageChange(int position, Bitmap bitmap) {
                if (HomeFragment.mPosition == _position) {
                    EventBus.getDefault().post(new TopBarEvent().setTopBarBg(bitmap));
                }
            }

            @Override
            public void onBannerClick(int position, Object item) {
                if (LoginUtils.checkLogin(getActivity())) {
                    if (item instanceof Vod) {
                        Vod vod = (Vod) item;
                        PlayActivity.startByVod(vod);
                    }

                }
            }
        },_data.getTypeId()));

        adapter.register(TopBean.class, new TopItemViewBinder(_data.getTypeId()).setActionListener(new TopItemViewBinder.TopItemActionListener() {
            @Override
            public void onClickMore(View view) {
                getTopData();
            }

            @Override
            public void onClickChange(View view) {

            }

            @Override
            public void onClickItem(View view, Object item) {
                if (LoginUtils.checkLogin(getActivity())) {
                    if (item instanceof Vod) {
                        Vod vod = (Vod) item;
                        PlayActivity.startByVod(vod);
                    }

                }

            }
        }));
        adapter.register(CardBean.class, new CardItemViewBinder(true, true, true).setActionListener(new CardItemViewBinder.CardItemActionListener() {
            @Override
            public void onClickMore(View view, Object name, List<VodBean> list, String title) {
                ToastUtils.showShort("更多" + name);
                ScreenActivity3.start(name.toString(), (TypeBean) _data);
            }

            @Override
            public void onClickChange(View view, Object o) {
                if (o instanceof CardBean) {
                    CardBean cardBean = (CardBean) o;
                    cardBean.setPage(cardBean.getPage() + 1);
                    getVodListData(cardBean, false);
                }
            }

            @Override
            public void onClickItem(View view, Object item) {
                if (LoginUtils.checkLogin(getActivity())) {
                    if (item instanceof Vod) {
                        Vod vod = (Vod) item;
                        PlayActivity.startByVod(vod);
                    }

                }

            }
        }));
        adapter.register(StartBean.Ad.class, new AdViewBinder());
//        adapter.register(EndBean.class, new EndItemViewBinder());
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        //滑动加载
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager.findFirstVisibleItemPosition() != 0) {//离开里第一个ItemView
                    if (!isShowFirstItem) {
//                        EventBus.getDefault().post(new TopBarEvent().setTopBarBg(null));
                        isShowFirstItem = true;
                    }
                } else {
                    isShowFirstItem = false;
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    refreshLayout.setEnabled(true);
                    LogUtils.e("滑动到了顶部--" + HomeOtherChildFragment.this.toString());
                } else if (!recyclerView.canScrollVertically(1)) {
                    refreshLayout.setEnabled(true);
                    LogUtils.e("滑动到了顶部--" + HomeOtherChildFragment.this.toString());
//                    zlass_index = zlass_index + 1;
//                    if(zlass == null || zlass_index == zlass.length){
//                        zlass_index = zlass_index -1;
                    if (zlass != null) {
                        if (!_isShowEnd) {
//                            items.add(new EndBean("我是有底线的"));
                            adapter.notifyDataSetChanged();
                            _isShowEnd = true;
                        } else {
//                            ToastUtils.showShort("已没有更多的数据");
                        }
                    }

//                        return;
//                    }
//                    String title = zlass[zlass_index];
//                    CardBean cardBean = new CardBean(title,null);
//                    items.add(cardBean);
                    //adapter.notifyDataSetChanged();
//                    getVodListData(cardBean,true);
                } else {
                    refreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // 查看源码可知State有三种状态：SCROLL_STATE_IDLE（静止）、SCROLL_STATE_DRAGGING（上升）、SCROLL_STATE_SETTLING（下落）
                if (newState == SCROLL_STATE_IDLE) { // 滚动静止时才加载图片资源，极大提升流畅度
//                    EventBus.getDefault().postSticky(new UpdateEvent(false));
//                    adapter.notifyDataSetChanged(); // notify调用后onBindViewHolder会响应调用
                } else {
//                    EventBus.getDefault().postSticky(new UpdateEvent(true));
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        recyclerView.setAdapter(adapter);
    }


    public void initData() {
        if (items != null) {
            items.clear();
            adapter.notifyDataSetChanged();
        } else {
            adapter.setItems(items = new ArrayList<>());
            adapter.notifyDataSetChanged();
        }
        //初始化top的数据
        topBean = null;
        top_page = 1;
    }

    private void initBannerData(List<VodBean> list) {
        if (list == null) return;
        BannerBean bannerBean = new BannerBean(list);
        items.add(bannerBean);
        adapter.notifyDataSetChanged();
    }

    private TopBean topBean;

    private void initTopData(List<VodBean> list) {
        if (list == null) return;
        if (topBean == null) {
            topBean = new TopBean("每日Top排行榜", list);
            items.add(topBean);
            adapter.notifyDataSetChanged();
            getCardData();
        } else {
            upTopData(list);
        }
    }

    private void upTopData(List<VodBean> list) {
        if (list == null) return;
        if (topBean != null) {
            topBean.setVodList(list);
            adapter.notifyDataSetChanged();
        }
    }

    private void initCardData(List<CardBean> list) {
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                items.add(list.get(0));
                StartBean startBean = MMkvUtils.Companion.Builds().loadStartBean("");
                switch (_data.getTypeId()) {
                    case 1:
                        if (startBean != null && startBean.getAds() != null && startBean.getAds().getVod() != null) {
                            items.add(startBean.getAds().getVod());
                        }
                        break;
                    case 2:
                        if (startBean != null && startBean.getAds() != null && startBean.getAds().getSitcom() != null) {
                            items.add(startBean.getAds().getSitcom());
                        }
                        break;
                    case 3:
                        if (startBean != null && startBean.getAds() != null && startBean.getAds().getVariety() != null) {
                            items.add(startBean.getAds().getVariety());
                        }
                        break;
                    case 4:
                        if (startBean != null && startBean.getAds() != null && startBean.getAds().getCartoon() != null) {
                            items.add(startBean.getAds().getCartoon());
                        }
                        break;
                }
            } else {
                items.add(list.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private Disposable disposable1;
    private Disposable disposable2;
    private Disposable disposable3;
    private Disposable disposable4;

    private void getBannerData() {
        BannerService bannerService = Retrofit2Utils.INSTANCE.createByGson(BannerService.class);

        bannerService.getBannerList(_data.getTypeId(), 8, cn.lvdou.vod.utils.AUtils.sign(), cn.lvdou.vod.utils.AUtils.keyTime())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<PageResult<VodBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable1 != null && !disposable1.isDisposed()) {
                            disposable1.dispose();
                            disposable1 = null;
                        }
                        disposable1 = d;
                    }

                    @Override
                    public void onNext(PageResult<VodBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                //隐藏其他界面的banner
                                List<VodBean> list = result.getData().getList();
                                initBannerData(list);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // getTopData();
                        //不显示排行榜数据
                        getCardData();

                    }
                });
    }

    private int top_page = 1;

    private void getTopData() {
        TopService topService = Retrofit2Utils.INSTANCE.createByGson(TopService.class);

        topService.getTopList(_data.getTypeId(), "top_day", 3, top_page, cn.lvdou.vod.utils.AUtils.sign(), cn.lvdou.vod.utils.AUtils.keyTime())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<PageResult<VodBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable2 != null && !disposable2.isDisposed()) {
                            disposable2.dispose();
                            disposable2 = null;
                        }
                        disposable2 = d;
                    }

                    @Override
                    public void onNext(PageResult<VodBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                List<VodBean> list = result.getData().getList();
                                initTopData(list);
                                top_page = result.getData().getPage() + 1;
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

    private void getCardData() {
        CardMoreService cardMoreService = Retrofit2Utils.INSTANCE.createByGson(CardMoreService.class);

        cardMoreService.getCardListByType(_data.getTypeId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<BaseResult<TypeBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable3 != null && !disposable3.isDisposed()) {
                            disposable3.dispose();
                            disposable3 = null;
                        }
                        disposable3 = d;
                    }

                    @Override
                    public void onNext(BaseResult<TypeBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                List<CardBean> list = result.getData().getClasses();
                                initCardData(list);
                                zlass_index = list.size() - 1;
                                zlass = _data.getExtend().getZlass().split(",");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        if (refreshLayout != null) refreshLayout.setRefreshing(false);
                    }
                });
    }


    private void getVodListData(CardBean cardBean, boolean isMore) {
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

        vodService.getVodList(_data.getTypeId(), cardBean.getPage(), 6, cardBean.getTitle(), AUtils.sign(), AUtils.keyTime())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<PageResult<VodBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable4 != null && !disposable4.isDisposed()) {
                            disposable4.dispose();
                            disposable4 = null;
                        }
                        disposable4 = d;
                    }

                    @Override
                    public void onNext(PageResult<VodBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                List<VodBean> list = result.getData().getList();
                                if (list == null || list.size() == 0) {
                                    cardBean.setPage(0);
                                }
                                cardBean.setVods(list);
                                adapter.notifyDataSetChanged();
                                if (!isMore) {
                                    int index = items.indexOf(cardBean);
                                    recyclerView.scrollToPosition(index);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    @Override
    public void onDestroyView() {
        if (disposable1 != null && !disposable1.isDisposed()) {
            disposable1.dispose();
            disposable1 = null;
        }
        if (disposable2 != null && !disposable2.isDisposed()) {
            disposable2.dispose();
            disposable2 = null;
        }
        if (disposable3 != null && !disposable3.isDisposed()) {
            disposable3.dispose();
            disposable3 = null;
        }
        if (disposable4 != null && !disposable4.isDisposed()) {
            disposable4.dispose();
            disposable4 = null;
        }
        super.onDestroyView();
    }


    @Override
    public boolean onBackPressedSupport() {
        if (layoutManager.findFirstVisibleItemPosition() != 0) {
            recyclerView.scrollToPosition(0);
            return true;
        }
        return super.onBackPressedSupport();
    }


}
