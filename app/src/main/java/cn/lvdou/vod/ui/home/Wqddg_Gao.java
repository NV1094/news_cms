package cn.lvdou.vod.ui.home;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.youxiao.ssp.ad.bean.SSPContentItem;
import com.youxiao.ssp.ad.listener.IContentPageListener;
import com.youxiao.ssp.ad.listener.IContentVideoListener;
import com.youxiao.ssp.core.SSPSdk;

import cn.lvdou.vod.R;
import cn.lvdou.vod.base.BaseMainFragment;

public class Wqddg_Gao extends BaseMainFragment {

    public static Wqddg_Gao newInstance() {
        Bundle args = new Bundle();
        Wqddg_Gao fragment = new Wqddg_Gao();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.framage_wqddg_gao;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
