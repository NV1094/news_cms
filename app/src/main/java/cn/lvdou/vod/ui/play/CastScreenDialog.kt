package cn.lvdou.vod.ui.play

import android.app.Dialog
import android.content.Context
import cn.lvdou.vod.R

class CastScreenDialog(context: Context, val dialogHeight: Int) : Dialog(context, R.style.CastScreenDialogStyle) {
//    private var playActivity: WeakReference<NewPlayActivity> = WeakReference(context as NewPlayActivity)
//    private var isSearching = true
//    private val mLelinkServiceManager: ILelinkServiceManager by lazy {
//        LelinkServiceManager.getInstance(context)
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
//                                tvMsg.visibility = View.GONE
//                                playActivity.get()?.castScreen()
//                                dismiss()
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
//    init {
//        setContentView(R.layout.dialog_cast_screen)
//        setOnDismissListener {
//            mLelinkServiceManager.stopBrowse()
//        }
//        setOnCancelListener {
//            mLelinkServiceManager.stopBrowse()
//        }
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
//    }
//
//    @SuppressLint("RtlHardcoded")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window!!.attributes = window!!.attributes.apply {
//            width = WindowManager.LayoutParams.WRAP_CONTENT
//            height = if (dialogHeight == -1) {
//                WindowManager.LayoutParams.MATCH_PARENT
//            } else {
//                dialogHeight
//            }
//            gravity = Gravity.TOP or Gravity.RIGHT
//        }
//        rvSelectWorks.layoutManager = MyLinearLayoutManager(context)
//        rvSelectWorks.adapter = selectionAdapter
//    }
//
//
//    class SelectionAdapter : BaseQuickAdapter<CastDevice, BaseViewHolder>(R.layout.item_dialog_cast_screen) {
//
//        override fun convert(helper: BaseViewHolder, item: CastDevice) {
//            helper.setText(R.id.tv, item.name)
//        }
//    }

}
