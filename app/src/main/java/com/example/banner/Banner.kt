package com.example.banner

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class Banner : ViewGroup, LifecycleObserver {

    companion object {
        private const val TAG = "Banner"
    }

    //container
    private var listOfBitmap: ArrayList<Any>? = null
    private var indicatorContainer: LinearLayout? = null
    private var viewpager2: ViewPager2? = null

    //attrs
    private var interval = 5
    private var indicatorSize = dpToPx(5f)
    private var indicatorColor = Color.BLACK

    //auto
    private var timer: Timer? = null
    private var currentIndex = 1
    private var autoScrollEnabled = true
    private var shouldScroll = false

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        getAttrs(context, attrs)
    }

    private fun getAttrs(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.Banner).apply {
            interval = getInt(R.styleable.Banner_interval, interval)
            indicatorColor = getColor(R.styleable.Banner_colorOfIndicator, indicatorColor)
            indicatorSize = getDimension(R.styleable.Banner_indicatorSize, indicatorSize)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount >= 1) throw Exception("can not add subview")
        indicatorContainer = LinearLayout(context).also {
            it.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            it.orientation = LinearLayout.HORIZONTAL
            it.gravity = Gravity.CENTER
        }
        viewpager2 = ViewPager2(context).also {
            it.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    shouldScroll = (currentIndex == position)
                    currentIndex = position
                    Log.d(TAG, "onPageSelected: position ==> $position")
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    handleState(state)
                }
            })
        }
        addView(viewpager2)
        addView(indicatorContainer)
    }

    private fun handleState(state: Int) {
        Log.d(TAG, "onPageScrollStateChanged: state ==> $state")
        when (state) {
            ViewPager2.SCROLL_STATE_IDLE -> {
                autoScrollEnabled = true
                if (!shouldScroll)
                    truthIndex()
            }

            ViewPager2.SCROLL_STATE_SETTLING -> {

            }

            ViewPager2.SCROLL_STATE_DRAGGING -> {
                autoScrollEnabled = false
            }
        }
    }

    private fun truthIndex() {
        Log.d(TAG, "truthIndex: currentIndex ==> $currentIndex")
        val indicatorIndex = when (currentIndex) {
            listOfBitmap!!.size + 1 -> {
                viewpager2?.setCurrentItem(1, false)
                currentIndex = 1
                0
            }
            0 -> {
                viewpager2?.setCurrentItem(listOfBitmap!!.size, false)
                currentIndex = listOfBitmap!!.size
                listOfBitmap!!.size - 1
            }
            else -> {
                currentIndex - 1
            }
        }
        updateIndicatorAndContent(indicatorIndex)
    }

    private fun addIndicator(count: Int) {
        repeat(count) { _ ->
            val circle = Indicator(context, radius = indicatorSize, color = indicatorColor)
            circle.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).also {
                it.marginStart = 10
                it.marginEnd = 10
            }
            indicatorContainer!!.addView(circle)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewpager2?.measure(widthMeasureSpec, heightMeasureSpec)
        indicatorContainer?.measure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec((indicatorSize * 2).toInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        viewpager2?.layout(0, 0, measuredWidth, measuredHeight)
        indicatorContainer?.layout(
            0,
            measuredHeight - indicatorContainer!!.measuredHeight,
            measuredWidth,
            measuredHeight
        )
    }

    fun setImageList(
        list: ArrayList<Any>,
        layoutId: Int,
        onBindHolder: (holder: BannerAdapter.BannerHolder, value: Any) -> Unit
    ) {
        listOfBitmap = list
        addIndicator(list.size)
        viewpager2?.let {
            it.adapter = BannerAdapter(list, layoutId, onBindHolder)
            it.setCurrentItem(currentIndex, false)
            updateIndicatorAndContent(currentIndex - 1)
        }
    }

    private fun updateIndicatorAndContent(index: Int) {
        indicatorContainer?.let {
            it.children.forEachIndexed { i, view ->
                if (i == index) {
                    (view as Indicator).updateType(Indicator.Type.FILL)
                } else {
                    (view as Indicator).updateType(Indicator.Type.STROKE)
                }
            }
        }
    }

    private fun initTimer(delayTime: Long) {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "run: autoScrollEnabled ==> $autoScrollEnabled")
                if (autoScrollEnabled) {
                    MainScope().launch {
                        viewpager2?.setCurrentItem(
                            ((currentIndex + 1) % (listOfBitmap!!.size + 2)),
                            true
                        )
                    }
                }
            }
        }, delayTime, delayTime)
    }

    private fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    fun setTime(interval: Int) {
        this.interval = interval
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        autoScrollEnabled = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        autoScrollEnabled = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun deleteTimer() {
        cancelTimer()
    }

    fun start() {
        listOfBitmap ?: kotlin.run {
            throw java.lang.Exception("do not set image list")
        }
        initTimer((interval * 1000).toLong())
    }
}