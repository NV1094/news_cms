package jaygoo.library.m3u8downloader.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import java.util.ArrayList;
import java.util.List;

import jaygoo.library.m3u8downloader.M3U8DownloaderPro;
import jaygoo.library.m3u8downloader.OnDeleteTaskListener;
import jaygoo.library.m3u8downloader.OnTaskDownloadListener;
import jaygoo.library.m3u8downloader.R;
import jaygoo.library.m3u8downloader.control.DownloadPresenter;
import jaygoo.library.m3u8downloader.db.M3U8dbManager;
import jaygoo.library.m3u8downloader.db.dao.DowningDao;
import jaygoo.library.m3u8downloader.db.table.M3u8DownloadingInfo;
import jaygoo.library.m3u8downloader.view.item.M3u8DoneItem;
import jaygoo.library.m3u8downloader.view.item.M3u8Item;
import jaygoo.library.m3u8downloader.view.item.M3u8ItemViewBinder;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author huangyong
 * createTime 2019-09-27
 */
public class DownloadingItemList extends Fragment implements View.OnClickListener {

    private ArrayList<Object> items;
    private RecyclerView recyclerView;
    private MultiTypeAdapter multiTypeAdapter;
    private M3u8ItemViewBinder m3u8ItemViewBinder;
    private LinearLayout rlEdit;
    private TextView tvSelect, tvSelectCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_list_layout, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        //读取数据库，获取正在进行的任务
        rlEdit = view.findViewById(R.id.rlEdit);
        tvSelect = view.findViewById(R.id.tvSelect);
        tvSelectCount = view.findViewById(R.id.tvSelectCount);

        m3u8ItemViewBinder = new M3u8ItemViewBinder();

        recyclerView = view.findViewById(R.id.down_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        multiTypeAdapter = new MultiTypeAdapter();
        multiTypeAdapter.register(M3u8Item.class, m3u8ItemViewBinder);
        items = new ArrayList<>();
        multiTypeAdapter.setItems(items);
        recyclerView.setAdapter(multiTypeAdapter);

        initData();

    }

    private void initData() {

        tvSelect.setOnClickListener(this);
        tvSelectCount.setOnClickListener(this);

        DowningDao downingDao = M3U8dbManager.getInstance(getContext()).downingDao();
        final List<M3u8DownloadingInfo> daoAll = downingDao.getAll();
        if (daoAll != null && daoAll.size() > 0) {
            for (final M3u8DownloadingInfo info : daoAll) {
                M3u8Item m3u8Item = new M3u8Item(new M3u8ItemViewBinder.OnItemListener() {
                    @Override
                    public void onListenerInit(OnTaskDownloadListener taskDownloadListener, M3u8Item m3u8Item) {
                        String taskUrl = DownloadPresenter.getTaskById(m3u8Item.getTaskId());

                        M3U8DownloaderPro.getInstance().addTaskListener(taskUrl, taskDownloadListener);
                    }

                    @Override
                    public void onClick(String taskId, String name, String posterUrl, OnTaskDownloadListener onTaskDownloadListener) {
                        String taskUrl = DownloadPresenter.getTaskById(taskId);

                        if (!TextUtils.isEmpty(taskUrl)) {
                            M3U8DownloaderPro.getInstance().download(taskUrl, name, posterUrl, onTaskDownloadListener);
                        } else {
                            Toast.makeText(getContext(), "重启失败，稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLongClick(final M3u8Item m3u8Item) {
                        if (m3u8ItemViewBinder.getEditModel()) {
                            m3u8ItemViewBinder.setEditModel(false);
                            multiTypeAdapter.notifyDataSetChanged();
                            rlEdit.setVisibility(View.GONE);
                        } else {
                            rlEdit.setVisibility(View.VISIBLE);
                            m3u8ItemViewBinder.setEditModel(true);
                            multiTypeAdapter.notifyDataSetChanged();
                        }
//                        final LoadingPopupView loadingPopupView = new XPopup.Builder(getContext()).asLoading("正在处理...");
//                        String taskUrl = DownloadPresenter.getTaskById(m3u8Item.getTaskId());
//                        if (TextUtils.isEmpty(taskUrl)){
//                            return;
//                        }
//                        new XPopup.Builder(getContext()).asConfirm("提示！", "确定删除当前下载任务？", new OnConfirmListener() {
//                            @Override
//                            public void onConfirm() {
//                                M3U8DownloaderPro.getInstance().cancelAndDelete(DownloadPresenter.getTaskById(m3u8Item.getTaskId()), new OnDeleteTaskListener() {
//                                    @Override
//                                    public void onStart() {
//                                        loadingPopupView.show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
//                                        items.remove(m3u8Item);
//                                        DownloadPresenter.DeleteTask(m3u8Item.getTaskId());
//                                        loadingPopupView.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Toast.makeText(getContext(), "任务和本地文件已删除", Toast.LENGTH_SHORT).show();
//                                                loadingPopupView.dismiss();
//                                                multiTypeAdapter.notifyDataSetChanged();
//                                            }
//                                        });
//
//                                    }
//
//                                    @Override
//                                    public void onFail() {
//                                        loadingPopupView.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Toast.makeText(getContext(), "删除失败，请手动删除", Toast.LENGTH_SHORT).show();
//                                                loadingPopupView.dismiss();
//                                            }
//                                        });
//
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable errorMsg) {
//                                        loadingPopupView.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Toast.makeText(getContext(), "删除出错，请手动删除", Toast.LENGTH_SHORT).show();
//                                                loadingPopupView.dismiss();
//                                            }
//                                        });
//
//                                    }
//                                });
//                            }
//                        }).show();
                    }

                    @Override
                    public void onSelect(int selectSize) {
                        tvSelectCount.setText("删除(" + m3u8ItemViewBinder.getSelectItem().size() + ")");
                        if (selectSize != 0 && selectSize == daoAll.size()) {
                            tvSelect.setText("取消全选");
                        } else {
                            tvSelect.setText("全选");
                        }
                    }
                }, info.getTaskId(), info.getTaskName(), info.getTaskPoster());
                items.add(m3u8Item);

            }
            multiTypeAdapter.notifyDataSetChanged();

        }
    }


    public void refreshList() {

        if (multiTypeAdapter != null && items != null) {
            items.clear();
            initData();
        }
    }

    boolean isCanDelete = true;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvSelect) {
            if (m3u8ItemViewBinder.isSelectAll()) {
                tvSelect.setText("全选");
                m3u8ItemViewBinder.setSelectAll(false);
                multiTypeAdapter.notifyDataSetChanged();
                tvSelectCount.setText("删除(" + m3u8ItemViewBinder.getSelectItem().size() + ")");
            } else {
                tvSelect.setText("取消全选");
                m3u8ItemViewBinder.setSelectAll(true);
                multiTypeAdapter.notifyDataSetChanged();
                tvSelectCount.setText("删除(" + m3u8ItemViewBinder.getSelectItem().size() + ")");
            }

        } else if (v.getId() == R.id.tvSelectCount) {
            final List<M3u8Item> selectItem = m3u8ItemViewBinder.getSelectItem();
            if (selectItem.size() != 0) {
                for (int i = 0; i < selectItem.size(); i++) {
                    M3u8Item m3u8Item = selectItem.get(i);
                    String taskUrl = DownloadPresenter.getTaskById(m3u8Item.getTaskId());
                    if (TextUtils.isEmpty(taskUrl)) {
                        isCanDelete = false;
                        break;
                    }
                }
                new XPopup.Builder(getContext()).asConfirm("提示！", "确定删除当前下载任务？", new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        if (isCanDelete) {
                            List<M3u8Item> deletList = new ArrayList<>();
                            deletList.addAll(selectItem);
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < deletList.size(); i++) {
                                M3u8Item m3u8Item = deletList.get(i);

                                items.remove(m3u8Item);
                                m3u8ItemViewBinder.deleteSlectItem(m3u8Item);
                                list.add(m3u8Item.getTaskId());
                            }
                            DownloadPresenter.DeleteTasks(list);
                            multiTypeAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "数据没有准备好", Toast.LENGTH_SHORT).show();
                        }
                        if (items.size() == 0) {
                            rlEdit.setVisibility(View.GONE);
                            tvSelectCount.setText("删除(0)");
                        }
                    }
                }).show();
            }
        }

    }
}
