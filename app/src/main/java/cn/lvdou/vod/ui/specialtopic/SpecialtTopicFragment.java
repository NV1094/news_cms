package cn.lvdou.vod.ui.specialtopic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import butterknife.BindView;
import cn.lvdou.vod.R;
import cn.lvdou.vod.base.BaseMainFragment;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.SpecialtTopicBean;
import cn.lvdou.vod.netservice.TopicService;
import cn.lvdou.vod.network.RetryWhen;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.Retrofit2Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
public class SpecialtTopicFragment extends BaseMainFragment {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.topic_listview)
    ListView topicListView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    List<SpecialtTopicBean> topicEntities;
    SpecialtopicAdpter2 activityLevelAdpter;

    Disposable  disposable1;

    boolean isInit;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_specialtopic;
    }

    public static SpecialtTopicFragment newInstance() {
        Bundle args = new Bundle();
        SpecialtTopicFragment fragment = new SpecialtTopicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInit) {
            isInit = true;
            topicEntities = new ArrayList<>();
            activityLevelAdpter = new SpecialtopicAdpter2(getActivity(), topicEntities);
            topicListView.setAdapter(activityLevelAdpter);
            activityLevelAdpter.notifyDataSetChanged();
            refreshLayout.setDisableContentWhenRefresh(false);//是否在刷新的时候禁止列表的操作
            refreshLayout.setDisableContentWhenLoading(false);//是否在加载的时候禁止列表的操作
            refreshLayout.setEnableLoadMore(false);//是否启用上拉加载功能
            refreshLayout.setEnableRefresh(false);//是否启用上拉加载功能;
            refreshLayout.setEnableAutoLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshlayout) {
                    refreshlayout.finishRefresh(1000);
                }
            });

            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                    refreshLayout.finishRefresh(1000);
                }
            });

            topicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), TopicDetailActivity.class);
                    intent.putExtra("topicid", topicEntities.get(position).getToppic_id());
                    Objects.requireNonNull(getActivity()).startActivity(intent);
                }
            });
            getTopicData();
        }
    }

    private void getTopicData() {
        TopicService cardService = Retrofit2Utils.INSTANCE.createByGson(TopicService.class);

        assert cardService != null;
        cardService.getTopicList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<PageResult<SpecialtTopicBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable1 != null && !disposable1.isDisposed()) {
                            disposable1.dispose();
                            disposable1 = null;
                        }
                        disposable1 = d;
                    }

                    @Override
                    public void onNext(PageResult<SpecialtTopicBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                List<SpecialtTopicBean> list = result.getData().getList();
                                topicEntities.clear();
                                topicEntities.addAll(list);
                                activityLevelAdpter.notifyDataSetChanged();
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


    @Override
    public void onDestroyView() {
        if (disposable1 != null && !disposable1.isDisposed()) {
            disposable1.dispose();
            disposable1 = null;
        }

        super.onDestroyView();
    }

}
