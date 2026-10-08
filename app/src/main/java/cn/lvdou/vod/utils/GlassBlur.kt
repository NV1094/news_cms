package cn.lvdou.vod.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import eightbitlab.com.blurview.BlurView

/**
 * 毛玻璃（liquid glass）装配工具。
 *
 * BlurView 会对它背后窗口内容做实时模糊快照，BlurView 自身的 android:background
 * （glass_card_bg / glass_panel_bg / glass_pill_bg / glass_bottom_bar_bg 等 v2 玻璃 drawable）
 * 再叠一层半透明填充 + 高亮描边。配合 android:clipToOutline="true" 即可让模糊被圆角裁剪。
 *
 * 若装配失败，玻璃 drawable 本身仍能正常渲染 —— 安全降级，不会白屏或崩溃。
 */
object GlassBlur {

    private const val BLUR_RADIUS = 16f

    /**
     * 扁平化改造：关闭实时毛玻璃模糊（装配改为空实现）。
     * BlurView 未装配时仅渲染自身背景，而 glass_* 背景已改为扁平纯色，
     * 因此所有调用点（Activity/Fragment/对话框）无需改动即为平面风格。
     * 如需回退到毛玻璃，恢复下面两行对 setupInto 的调用即可。
     */
    @JvmStatic
    fun setupAll(root: View?) {
        // 扁平化：不装配 BlurView
        return
    }

    /**
     * 扁平化：同 setupAll，不再装配模糊。
     */
    @JvmStatic
    fun setupWithTarget(target: ViewGroup, root: View?) {
        // 扁平化：不装配 BlurView
        return
    }

    private fun findBlurTarget(activity: Activity): ViewGroup? {
        // 优先使用内容区，保证被模糊的是「窗口里正在显示的内容」
        val content = activity.window?.decorView?.findViewById<View>(android.R.id.content)
        if (content is ViewGroup) return content
        val decor = activity.window?.decorView
        return decor as? ViewGroup
    }

    private fun setupInto(target: ViewGroup, view: View) {
        when (view) {
            is BlurView -> {
                try {
                    // BlurView 2.0.x 的公开 API 为 setupWith(ViewGroup)；
                    // 算法由其内部按系统版本选择（RenderEffect / RenderScript）。
                    view.setupWith(target)
                            .setBlurRadius(BLUR_RADIUS)
                            .setBlurAutoUpdate(true)
                    view.clipToOutline = true
                } catch (t: Throwable) {
                    // 装配失败：保留玻璃 drawable，安全降级
                    t.printStackTrace()
                }
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    setupInto(target, view.getChildAt(i))
                }
            }
        }
    }

    private fun findActivity(context: Context?): Activity? {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
