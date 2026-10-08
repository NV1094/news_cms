package cn.lvdou.vod.ui.task

import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.TaskBean
import cn.lvdou.vod.bean.TaskItemBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.BarUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.LoadingObserver

class TaskActivity : BaseActivity() {

    private val ivBack: ImageView by lazy { findViewById(R.id.ivBack) }
    private val rvTask: RecyclerView by lazy { findViewById(R.id.rvTask) }

    private val taskAdapter by lazy {
        TaskAdapter()
    }

    override fun getLayoutResID(): Int {
        BarUtils.setStatusBarLightMode(this, true)
        return R.layout.activity_task
    }

    override fun initView() {
        super.initView()
        rvTask.layoutManager = LinearLayoutManager(mActivity)
        rvTask.adapter = taskAdapter
    }

    override fun initListener() {
        super.initListener()
        ivBack.setOnClickListener {
            finish()
        }
    }

    override fun initData() {
        super.initData()
        getTaskList()
    }

    private fun getTaskList() {
        var vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(mActivity, vodService.getTaskList(),
                object : LoadingObserver<TaskBean>(mActivity) {
                    override fun onSuccess(data: TaskBean) {
                        val taskItems = ArrayList<TaskItemBean>()
                        taskItems.add(TaskItemBean(data.sign))
                        taskItems.add(TaskItemBean(data.share))
                        taskItems.add(TaskItemBean(data.comment))
                        taskItems.add(TaskItemBean(data.mark))
                        taskItems.add(TaskItemBean(data.danmu))
                        taskAdapter.setNewData(taskItems)
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }

    class TaskAdapter : BaseQuickAdapter<TaskItemBean, BaseViewHolder>(R.layout.item_task) {
        override fun convert(helper: BaseViewHolder, item: TaskItemBean?) {
            item?.run {
                helper.setText(R.id.item_tv_task_t1, item.title)
                helper.setText(R.id.item_tv_task_t2, item.info)
                helper.setText(R.id.item_tv_task_t3, "+${item.points}分")

            }
        }

    }

    companion object {
        fun start() {
            ActivityUtils.startActivity(TaskActivity::class.java, R.anim.slide_in_right, R.anim.no_anim)
        }
    }
}
