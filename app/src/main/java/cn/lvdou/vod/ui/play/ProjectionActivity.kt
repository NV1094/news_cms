package cn.lvdou.vod.ui.play

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.utils.LelinkHelper
import cn.lvdou.vod.utils.MyLinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
//import com.hpplay.sdk.source.api.IConnectListener
//import com.hpplay.sdk.source.browse.api.IBrowseListener
//import com.hpplay.sdk.source.browse.api.ILelinkServiceManager
//import com.hpplay.sdk.source.browse.api.LelinkServiceInfo
//import com.hpplay.sdk.source.browse.api.LelinkServiceManager

//class ProjectionActivity : BaseActivity() {
//    override fun getLayoutResID() = R.layout.activity_projection
//
//    companion object {
//        fun start(context: Context,requestcode :Int) {
//            val intent = Intent(context, ProjectionActivity::class.java)
//            val activity = context as Activity
//            activity.startActivityForResult(intent,requestcode)
//        }
//    }
//
//    override fun initView() {
//        super.initView()
//        rlBack.setOnClickListener { finish() }
//        tvUpdate.setOnClickListener {
//            if (isSearching) {
//                ToastUtils.showShort("正在搜索中！")
//            } else {
//                isSearching = true
//                tvMsg.text = "搜索设备中..."
//                mLelinkServiceManager.browse(ILelinkServiceManager.TYPE_ALL)
//            }
//        }
//        mLelinkServiceManager.setOnBrowseListener { resultCode, list ->
//            Utils.runOnUiThread {
//                isSearching = false
//                if (resultCode == IBrowseListener.BROWSE_SUCCESS) {
//                    if (list.isNullOrEmpty()) {
//                        tvMsg.text = "没有搜索到任何设备"
//                    } else {
//                        tvMsg.visibility = View.GONE
//                        selectionAdapter.setNewData(list)
//                    }
//                } else {
//                    tvMsg.text = "搜索失败"
//                }
//            }
//
//        }
//        mLelinkServiceManager.browse(ILelinkServiceManager.TYPE_ALL)
//
////        window!!.attributes = window!!.attributes.apply {
////            width = WindowManager.LayoutParams.WRAP_CONTENT
////            height = if (dialogHeight == -1) {
////                WindowManager.LayoutParams.MATCH_PARENT
////            } else {
////                dialogHeight
////            }
////            gravity = Gravity.TOP or Gravity.RIGHT
////        }
//        rvSelectWorks.layoutManager = MyLinearLayoutManager(this)
//        rvSelectWorks.adapter = selectionAdapter
//    }
//
//    private var isSearching = true
//    private val mLelinkServiceManager: ILelinkServiceManager by lazy {
//        LelinkServiceManager.getInstance(this)
//    }
//
//    private val selectionAdapter: SelectionAdapter by lazy {
//        SelectionAdapter().apply {
//            setOnItemClickListener { adapter, view, position ->
//                val item = adapter.getItem(position) as LelinkServiceInfo
//                if (!item.isOnLine) {
//                    ToastUtils.showShort("设备不在线")
//                } else if (item.isConnect) {
//                    ToastUtils.showShort("已连接到该设备")
//                } else {
//                    tvMsg.visibility = View.VISIBLE
//                    tvMsg.text = "连接设备中..."
//                    LelinkHelper.getInstance().connect(item, object : IConnectListener {
//                        override fun onConnect(p0: LelinkServiceInfo?, p1: Int) {
//                            Utils.runOnUiThread {
//                                //链接成功
//                                tvMsg.visibility = View.GONE
//                                Log.e("开始投屏","1111111111111111111111111111")
////                                EventBus.getDefault().post("开始投屏")
//                                setResult(Activity.RESULT_OK)
//                                finish()
////                                playActivity.get()?.castScreen()
////                                dismiss()
//                            }
//                        }
//
//                        override fun onDisconnect(p0: LelinkServiceInfo?, p1: Int, p2: Int) {
//                            Utils.runOnUiThread {
//                                tvMsg.text = "连接失败！"
//                            }
//                        }
//
//                    })
//                }
//            }
//        }
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        mLelinkServiceManager.stopBrowse()
//    }
//
//    class SelectionAdapter : BaseQuickAdapter<LelinkServiceInfo, BaseViewHolder>(R.layout.item_dialog_cast_screen) {
//
//        override fun convert(helper: BaseViewHolder, item: LelinkServiceInfo) {
//            if (item != null) {
//                helper.setText(R.id.tv, item.name)
//            }
//        }
//    }
//}