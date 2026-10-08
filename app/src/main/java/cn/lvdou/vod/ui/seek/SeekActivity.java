package cn.lvdou.vod.ui.seek;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.youxiao.ssp.ad.bean.SSPAd;
import com.youxiao.ssp.ad.core.AdClient;
import com.youxiao.ssp.ad.listener.AdLoadAdapter;
import com.youxiao.ssp.base.tools.h;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.App;
import cn.lvdou.vod.R;
import cn.lvdou.vod.ad.AdWebView;
import cn.lvdou.vod.base.BaseActivity;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.netservice.VodService;
import cn.lvdou.vod.network.RetryWhen;
import cn.lvdou.vod.ui.feedback.FeedbackActivity;
import cn.lvdou.vod.ui.play.PlayActivity;
import cn.lvdou.vod.utils.AUtils;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.LoginUtils;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.Retrofit2Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.drakeet.multitype.MultiTypeAdapter;

public class SeekActivity extends BaseActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.awvSeek)
    AdWebView awvSeek;
    @BindView(R.id.shv_seek)
    SeekHistoryView seekHistoryView;
    @BindView(R.id.tv_seek_seek)
    TextView tv_seek;
    @BindView(R.id.et_seek_seek)
    EditText editText;
    @BindView(R.id.nsv)
    NestedScrollView nsv;
    @BindView(R.id.rv_seek_result)
    RecyclerView result_recyclerView;
    @BindView(R.id.tv_seek_hot_title)
    AppCompatTextView tvSeekHotTitle;
    @BindView(R.id.iv_seek_clear_seek)
    ImageView ivSeekClearSeek;
    @BindView(R.id.iv_msc)
    ImageView iv_msc;
    @BindView(R.id.rvAssociate)
    RecyclerView rvAssociate;
    @BindView(R.id.linear_s)
    FrameLayout linear_s;
    @BindView(R.id.rv_seek_hot)
    RecyclerView recyclerView;
    @BindView(R.id.iv_seek_clear_history1)
    LinearLayout iv_seek_clear_history1;
    @BindView(R.id.fankui)
    TextView fankui;
    @BindView(R.id.linear_Banner)
    FrameLayout linear_Banner;

    private MultiTypeAdapter result_adapter;
    private List<Object> result_items;
    private MultiTypeAdapter adapter;
    private List<Object> items;
    private boolean isSeek = false;
    private int mPage = 1;
    private String mWd;
    private boolean isEnd = false;
    private Disposable disposable;
    private Disposable disposable2;
    private AssociateAdapter associateAdapter;
    private boolean isAssociate;//是否是联想结果点击改变的搜索框的文字
    private String getService_qq;
    private AdClient adClient = new AdClient(SeekActivity.this);

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.fankui)
    void FanKui() {
        if (fankui.getText().toString().equals("联系客服") && getService_qq != null) {
            String link = "mqq://im/chat?chat_type=wpa&uin=" + getService_qq + "&version=1&src_type=web";
            Intent var3 = new Intent("android.intent.action.VIEW", Uri.parse(link));
            if (var3.resolveActivity(SeekActivity.this.getMActivity().getPackageManager()) != null) {
                ActivityUtils.startActivity(var3);
            } else {
                ToastUtils.showShort("未安装QQ!!", new Object[0]);
            }
        } else {
            String message = "搜索不到：" + mWd + "，请审核添加";
            FeedbackActivity.starts(this, message);
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "NonConstantResourceId"})
    @OnClick(R.id.tv_seek_seek)
    void seek() {
        iv_seek_clear_history1.setVisibility(View.GONE);
        if (isSeek) {
            tv_seek.setText("搜索");
            nsv.setVisibility(View.VISIBLE);
            result_recyclerView.setVisibility(View.GONE);
            if (result_items != null) {
                result_items.clear();
            }
            result_adapter.notifyDataSetChanged();
            isSeek = false;
        } else {
            tv_seek.setText("取消");
            nsv.setVisibility(View.GONE);
            result_recyclerView.setVisibility(View.VISIBLE);
            isSeek = true;
            String data = editText.getText().toString();
            if (!StringUtils.isEmpty(data)) {
                seekHistoryView.addData(data);
            }
            mWd = editText.getText().toString();
            mPage = 1;
            isEnd = false;
            KeyboardUtils.hideSoftInput(this);
            getResultData(mWd);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.iv_seek_clear_seek)
    void clearSeek() {
        editText.setText("");
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.iv_seek_clear_history)
    void clearHistory() {
        if (seekHistoryView != null) {
            seekHistoryView.clear();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.rlBack)
    void back() {
        finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_seek;
    }

    float width = 0;
    float height = 0;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            width = linear_s.getMeasuredWidth();
            height = linear_s.getMeasuredHeight();
        }
    }

    private void changeIntoText() {

        // ②初始化有交互动画的语音识别器
        RecognizerDialog iatDialog = new RecognizerDialog(SeekActivity.this, mInitListener);
        //③设置监听，实现听写结果的回调
        iatDialog.setParameter(SpeechConstant.ASR_PTT,"0");
        iatDialog.setParameter(SpeechConstant.RESULT_TYPE, "plain");
        iatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        iatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        iatDialog.setListener(new RecognizerDialogListener() {

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                Log.d("识别成功", recognizerResult.getResultString());
                editText.setText(recognizerResult.getResultString());
//                editText.requestFocus();
                editText.setSelection(recognizerResult.getResultString().length());
            }

            @Override
            public void onError(SpeechError speechError) {
                //自动生成的方法存根
                Log.d(TAG, "onError: 错误");
                speechError.getPlainDescription(true);
            }
        });
        //开始听写，需将sdk中的assets文件下的文件夹拷入项目的assets文件夹下（没有的话自己新建）
        iatDialog.show();
    }

    private static final String TAG = "SeekActivity";
    private InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            Toast.makeText(SeekActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
        }
    };
    /**
     * 推送消息测试
     */
    public void senMsc(View view) {
        changeIntoText();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seekHistoryView.setOnItemClickListener((view, data) -> {
            showSearchResult(data);
        });
        if (ApiConfig.UNFEIYUYIN == 1) {
            iv_msc.setVisibility(View.VISIBLE);
        }else{
            iv_msc.setVisibility(View.GONE);
        }
        initHotView();
        initHot(MMkvUtils.Companion.Builds().loadSearchHot(""));
        initResultView();
        initAssociateResult();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iv_seek_clear_history1.setVisibility(View.GONE);
                if (isAssociate) {
                    isAssociate = false;
                    return;
                }
                if (TextUtils.isEmpty(s.toString())) {
                    ivSeekClearSeek.setVisibility(View.GONE);
                    if (ApiConfig.UNFEIYUYIN == 1) {
                        iv_msc.setVisibility(View.VISIBLE);
                    }else{
                        iv_msc.setVisibility(View.GONE);
                    }
                    hideAsociateView();
                } else {
                    ivSeekClearSeek.setVisibility(View.VISIBLE);
                    iv_msc.setVisibility(View.GONE);
                    getAssociateResult(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                /*判断是否是“搜索”键*/
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideSoftInput(SeekActivity.this);
                    tv_seek.setText("取消");
                    nsv.setVisibility(View.GONE);
                    result_recyclerView.setVisibility(View.VISIBLE);
                    isSeek = true;

                    String data = editText.getText().toString();
                    if (!StringUtils.isEmpty(data)) {
                        seekHistoryView.addData(data);
                    }
                    mWd = editText.getText().toString();
                    mPage = 1;
                    isEnd = false;
                    getResultData(mWd);
                    return true;
                }
                return false;
            }
        });
    }

    //为了更好的释放资源，在页面销毁时调用release方法释放资源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (App.SdkSwitch){
            adClient.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getXWTAd();
    }

    private void getXWTAd() {
        StartBean startBean = MMkvUtils.Companion.Builds().loadStartBean("");
        if (startBean != null && startBean.getAds() != null) {
            if (App.SdkSwitch && startBean.getAds().getSdk_banner() != null) {
                StartBean.Ad banner = startBean.getAds().getSdk_banner();
                if (banner.getDescription() != null && banner.getStatus() == 1) {
                    adClient.requestBannerAd(linear_Banner, banner.getDescription(), new AdLoadAdapter() {
                        @Override
                        public void onAdLoad(SSPAd ad) {
                            super.onAdLoad(ad);
                            linear_Banner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(int var1, String var2) {
                            linear_Banner.setVisibility(View.GONE);
                        }
                    });
                }else{
                    linear_Banner.setVisibility(View.GONE);
                }
            }else{
                linear_Banner.setVisibility(View.GONE);
            }

            if (App.SdkSwitch && startBean.getAds().getSdk_informationflow() != null) {
                StartBean.Ad informationflow = startBean.getAds().getSdk_informationflow();
                if (informationflow.getDescription() != null && informationflow.getStatus() == 1) {
                    adClient.requestExpressAd(informationflow.getDescription(), new AdLoadAdapter() {
                        @Override
                        public void onAdLoad(SSPAd ad) {
                            super.onAdLoad(ad);
                            linear_s.setVisibility(View.VISIBLE);
                            linear_s.removeAllViews();
                            linear_s.addView(ad.getView());
                        }
                        @Override
                        public void onError(int var1, String var2) {
                            linear_s.setVisibility(View.GONE);
                        }
                    });
                }else{
                    linear_s.setVisibility(View.GONE);
                }
            }else{
                linear_s.setVisibility(View.GONE);
            }

            if (startBean.getAds().getSearcher() != null) {
                StartBean.Ad searcher = startBean.getAds().getSearcher();
                if (searcher.getDescription() == null || searcher.getStatus() == 0) {
                    awvSeek.setVisibility(View.GONE);
                } else {
                    awvSeek.setVisibility(View.VISIBLE);
                    awvSeek.loadHtmlBody(searcher.getDescription());
                }
            } else {
                awvSeek.setVisibility(View.GONE);
            }

            if (startBean.getAds().getSearch_feedback() != null && startBean.getAds().getService_qq() != null) {
                StartBean.Ad feedback = startBean.getAds().getSearch_feedback();
                StartBean.Ad serviceqq = startBean.getAds().getService_qq();
                if (feedback.getDescription() == null || feedback.getStatus() == 0 || serviceqq.getDescription() == null) {
                    fankui.setText("点此反馈");
                } else {
                    fankui.setText("联系客服");
                    getService_qq = serviceqq.getDescription();
                }
            } else {
                fankui.setText("点此反馈");
            }
        }else{
            fankui.setText("点此反馈");
            awvSeek.setVisibility(View.GONE);
            linear_s.setVisibility(View.GONE);
            linear_Banner.setVisibility(View.GONE);
        }
    }

    private void initHotView() {
        adapter = new MultiTypeAdapter();
        SeekHotItemViewBinder seekHotItemViewBinder = new SeekHotItemViewBinder();
        seekHotItemViewBinder.setBaseItemClickListener((view, item) -> {
            showSearchResult(item.toString());
        });
        adapter.register(String.class, seekHotItemViewBinder);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initHot(List<String> list) {
        if (list == null) {
            tvSeekHotTitle.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        if (items == null) {
            items = new ArrayList<>();
            adapter.setItems(items);
        }
        if (list.size() > 10) {
            list = list.subList(0, 10);
        }
        if (items != null) {
            items.clear();
            items.addAll(list);
        }
        adapter.notifyDataSetChanged();
    }

    private void initResultView() {
        result_adapter = new MultiTypeAdapter();
        SeekResultItemViewBinder seekResultItemViewBinder = new SeekResultItemViewBinder();
        seekResultItemViewBinder.setBaseItemClickListener((view, item) -> {
            if (item instanceof VodBean) {
                VodBean vodBean = (VodBean) item;
                PlayActivity.startByVod(vodBean);
            }
        });
        result_adapter.register(VodBean.class, seekResultItemViewBinder);
//        result_adapter.register(EndBean.class, new EndItemViewBinder());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        result_recyclerView.setLayoutManager(linearLayoutManager);

        //滑动加载
        result_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(-1)) {
                    LogUtils.e("滑动到了顶部--");
                } else if (!recyclerView.canScrollVertically(1)) {
                    LogUtils.e("滑动到了底部--");
                    if (isEnd) return;
                    mPage = mPage + 1;
                    getResultData(mWd);
                }
            }
        });
        result_recyclerView.setAdapter(result_adapter);
    }

    private void initAssociateResult() {
        rvAssociate.setLayoutManager(new LinearLayoutManager(this));
        associateAdapter = new AssociateAdapter();
        associateAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                VodBean item = (VodBean) adapter.getItem(position);
                hideAsociateView();
                showSearchResult(item.getVodName());
            }
        });
        rvAssociate.setAdapter(associateAdapter);
    }

    private void showSearchResult(String data) {
        tv_seek.setText("取消");
        nsv.setVisibility(View.GONE);
        result_recyclerView.setVisibility(View.VISIBLE);
        isSeek = true;
        isAssociate = true;
        mWd = data;
        editText.setText(data);
        editText.setSelection(data.length());
        if (!StringUtils.isEmpty(data)) {
            seekHistoryView.addData(data);
        }
        KeyboardUtils.hideSoftInput(this);
        mPage = 1;
        isEnd = false;
        getResultData(data);
    }

    private void getAssociateResult(String data) {
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

        assert vodService != null;
        vodService.getVodList(1, 20, data, AUtils.sign(), AUtils.keyTime())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 3))
                .subscribe(new Observer<PageResult<VodBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (disposable != null && !disposable.isDisposed()) {
                            disposable.dispose();
                            disposable = null;
                        }
                        disposable = d;
                    }

                    @Override
                    public void onNext(PageResult<VodBean> result) {
                        if (result != null) {
                            if (result.isSuccessful()) {
                                List<VodBean> list = result.getData().getList();
                                if (list != null && list.size() != 0) {
                                    showAssociateView();
                                    associateAdapter.setNewData(list);
                                } else {
                                    hideAsociateView();
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

    private void initResult(List<?> list) {
        if (list == null) return;
        if (result_items == null) {
            result_items = new ArrayList<>();
            result_adapter.setItems(result_items);
        } else {
            if (mPage == 1 && result_items != null) {
                result_items.clear();
            }
        }
        result_items.addAll(list);
        result_adapter.notifyDataSetChanged();
    }

    private void getResultData(String data) {
        final ProgressDialog progressDialog = ProgressDialog.show(SeekActivity.this.getMActivity(), "", "正在搜索：" + data);
        iv_seek_clear_history1.setVisibility(View.GONE);
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

        vodService.getVodList(mPage, 10, data, AUtils.sign(), AUtils.keyTime())
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

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onNext(PageResult<VodBean> result) {
                        progressDialog.dismiss();
                        if (result != null) {
                            if (result.isSuccessful()) {
                                List<VodBean> list = result.getData().getList();
                                hideAsociateView();
                                if (list == null || list.size() == 0) {
                                    if (mPage == 1) {
                                        result_items = new ArrayList<>();
                                        ToastUtils.showShort("没有搜索到相关影片");
                                        iv_seek_clear_history1.setVisibility(View.VISIBLE);
                                        result_adapter.setItems(result_items);
                                    } else {
                                        ToastUtils.showShort("别拉了！到底了~~~");
                                    }
                                    result_adapter.notifyDataSetChanged();
                                    isEnd = true;
                                } else {
                                    initResult(list);
                                }

                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        progressDialog.dismiss();
                    }
                });
    }

    private void showAssociateView() {
        rvAssociate.setVisibility(View.VISIBLE);
    }

    private void hideAsociateView() {
        rvAssociate.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressedSupport() {
        if (isSeek) {
            seek();
        } else {
            super.onBackPressedSupport();
        }
    }
}
