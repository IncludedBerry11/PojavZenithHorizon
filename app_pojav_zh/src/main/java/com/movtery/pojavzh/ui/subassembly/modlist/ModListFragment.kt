package com.movtery.pojavzh.ui.subassembly.modlist

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils
import com.movtery.pojavzh.utils.anim.AnimUtils.Companion.playVisibilityAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import java.util.concurrent.Future

abstract class ModListFragment : FragmentWithAnim(R.layout.fragment_mod_download) {
    protected var fragmentActivity: FragmentActivity? = null
    private var parentAdapter: RecyclerView.Adapter<*>? = null
    protected var recyclerView: RecyclerView? = null
    private var mModsLayout: View? = null
    private var mOperateLayout: View? = null
    private var mLoadingView: View? = null
    private var mTitleLayout: View? = null
    private var mNameText: TextView? = null
    private var mSubTitleText: TextView? = null
    private var mSelectTitle: TextView? = null
    private var mFailedToLoad: TextView? = null
    private var mIcon: ImageView? = null
    private var mLaunchLink: ImageView? = null
    private var mBackToTop: ImageButton? = null
    private var mReturnButton: Button? = null
    private var mRefreshButton: Button? = null
    protected var releaseCheckBox: CheckBox? = null
    protected var currentTask: Future<*>? = null
    private var releaseCheckBoxVisible = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        init()
    }

    protected open fun init() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        recyclerView?.layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards))
        recyclerView?.layoutManager = layoutManager

        mRefreshButton?.setOnClickListener { refreshTask() }
        releaseCheckBox?.setOnClickListener { initRefresh() }
        mReturnButton?.setOnClickListener {
            parentAdapter?.apply {
                hideParentElement(false)
                recyclerView?.adapter = this
                recyclerView?.scheduleLayoutAnimation()
                parentAdapter = null
                return@setOnClickListener
            }
            ZHTools.onBackPressed(requireActivity())
        }

        mBackToTop?.setOnClickListener { recyclerView?.smoothScrollToPosition(0) }

        currentTask = initRefresh()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.fragmentActivity = requireActivity()
    }

    override fun onPause() {
        cancelTask()
        super.onPause()
    }

    override fun onDestroy() {
        cancelTask()
        super.onDestroy()
    }

    private fun hideParentElement(hide: Boolean) {
        cancelTask()

        mRefreshButton?.isClickable = !hide
        releaseCheckBox?.isClickable = !hide

        setViewAnim(mSelectTitle!!, if (hide) Animations.FadeIn else Animations.FadeOut, (AllSettings.animationSpeed * 0.7).toLong())
        setViewAnim(mRefreshButton!!, if (hide) Animations.FadeOut else Animations.FadeIn, (AllSettings.animationSpeed * 0.7).toLong())
        if (releaseCheckBoxVisible) setViewAnim(releaseCheckBox!!, if (hide) Animations.FadeOut else Animations.FadeIn, (AllSettings.animationSpeed * 0.7).toLong())
    }

    private fun cancelTask() {
        currentTask?.apply { if (!isDone) cancel(true) }
    }

    private fun refreshTask() {
        currentTask = refresh()
    }

    protected abstract fun initRefresh(): Future<*>?
    protected abstract fun refresh(): Future<*>?

    protected fun componentProcessing(state: Boolean) {
        playVisibilityAnim(mLoadingView!!, state)
        recyclerView?.visibility = if (state) View.GONE else View.VISIBLE

        mRefreshButton?.isClickable = !state
        releaseCheckBox?.isClickable = !state
    }

    private fun bindViews(view: View) {
        mModsLayout = view.findViewById(R.id.mods_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)

        recyclerView = view.findViewById(R.id.zh_mod)
        mBackToTop = view.findViewById(R.id.zh_mod_back_to_top)
        mLoadingView = view.findViewById(R.id.zh_mod_loading)
        mIcon = view.findViewById(R.id.zh_mod_icon)
        mLaunchLink = view.findViewById(R.id.zh_launch_link)
        mTitleLayout = view.findViewById(R.id.mod_title_layout)
        mNameText = view.findViewById(R.id.zh_mod_name)
        mSubTitleText = view.findViewById(R.id.zh_mod_subtitle)
        mSelectTitle = view.findViewById(R.id.zh_select_title)
        mFailedToLoad = view.findViewById(R.id.zh_mod_failed_to_load)

        mReturnButton = view.findViewById(R.id.zh_mod_return_button)
        mRefreshButton = view.findViewById(R.id.zh_mod_refresh_button)
        releaseCheckBox = view.findViewById(R.id.zh_mod_release_version)

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager != null && recyclerView.adapter != null) {
                    val lastPosition = layoutManager.findFirstVisibleItemPosition()
                    val b = lastPosition >= 12

                    AnimUtils.setVisibilityAnim(mBackToTop!!, b)
                }
            }
        })
    }

    protected fun setNameText(nameText: String?) {
        mNameText?.text = nameText
    }

    protected fun setSubTitleText(text: String?) {
        mSubTitleText?.apply {
            visibility = if (text != null) View.VISIBLE else View.GONE
            text?.let { this.text = it }
        }
    }

    protected fun setIcon(icon: Drawable?) {
        mIcon?.setImageDrawable(icon)
    }

    protected fun setReleaseCheckBoxGone() {
        releaseCheckBoxVisible = false
        releaseCheckBox?.visibility = View.GONE
    }

    protected fun setFailedToLoad(reasons: String?) {
        val text = fragmentActivity!!.getString(R.string.modloader_dl_failed_to_load_list)
        mFailedToLoad?.text = if (reasons == null) text else StringUtils.insertNewline(text, reasons)
        playVisibilityAnim(mFailedToLoad!!, true)
    }

    protected fun cancelFailedToLoad() {
        playVisibilityAnim(mFailedToLoad!!, false)
    }

    protected fun setLink(link: String?) {
        if (link == null) return
        mLaunchLink?.let { view ->
            view.setOnClickListener { Tools.openURL(fragmentActivity, link) }
            AnimUtils.setVisibilityAnim(view, true)
        }
    }

    fun switchToChild(adapter: RecyclerView.Adapter<*>?, title: String?) {
        if (currentTask!!.isDone && adapter != null) {
            //保存父级，设置选中的标题文本，切换至子级
            parentAdapter = recyclerView!!.adapter
            mSelectTitle?.text = title
            hideParentElement(true)
            recyclerView?.adapter = adapter
            recyclerView?.scheduleLayoutAnimation()
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(mModsLayout!!, Animations.BounceInDown))
            .apply(AnimPlayer.Entry(mOperateLayout!!, Animations.BounceInLeft))
            .apply(AnimPlayer.Entry(mIcon!!, Animations.Wobble))
            .apply(AnimPlayer.Entry(mTitleLayout!!, Animations.FadeInLeft))
            .apply(AnimPlayer.Entry(mReturnButton!!, Animations.FadeInLeft))
            .apply(AnimPlayer.Entry(mRefreshButton!!, Animations.FadeInLeft))
            .apply(AnimPlayer.Entry(releaseCheckBox!!, Animations.FadeInLeft))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(mModsLayout!!, Animations.FadeOutUp))
            .apply(AnimPlayer.Entry(mOperateLayout!!, Animations.FadeOutRight))
    }
}
