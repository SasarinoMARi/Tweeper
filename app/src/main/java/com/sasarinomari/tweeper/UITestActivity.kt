package com.sasarinomari.tweeper

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Billing.BillingActivity
import com.sasarinomari.tweeper.Hetzer.New.ActivitySelectHetzerType
import com.sasarinomari.tweeper.MediaDownload.MediaDownloadActivity
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import kotlinx.android.synthetic.main.activity_uitest.*
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import su.levenetc.android.textsurface.Text
import su.levenetc.android.textsurface.TextBuilder
import su.levenetc.android.textsurface.animations.*
import su.levenetc.android.textsurface.contants.Align.*
import su.levenetc.android.textsurface.contants.Direction
import su.levenetc.android.textsurface.contants.Pivot
import su.levenetc.android.textsurface.contants.Side
import kotlin.random.Random


class UITestActivity : BaseActivity() {

    private val LOG_TAG = "UITest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uitest)

        val list = ListView(this)
        setContentView(list)

        val values = arrayOf(
            "Launch App",
            "Text Surface Test",
            "Recycler Injector Test",
            "Reward Ad Test",
            "Billing Activity Test",
            "Firebase Logging Test",
            "Spotlight Test",
            "Media Download Test",
            "Connection Check",
            "New Hetzer Test"
        )

        list.adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, android.R.id.text1, values
        )
        list.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        list.setOnItemClickListener { _, _, position, _ ->
            when(position) {
                1 -> testTextSufrace()
                2 -> testRecyclerInjector()
                3 -> testRewardAd()
                4 -> testBillingActivity()
                5 -> testFirebaseLogging()
                6 -> testSpotlight()
                7 -> textMediaDownload()
                8 -> connectionCheck()
                9 -> testNewHetzer()
            }
        }
    }

    private fun testNewHetzer() {
        startActivity(Intent(this, ActivitySelectHetzerType::class.java))
    }

    private fun connectionCheck() {
        val result = TwitterAdapter.isConnected(this)
        Log.i(LOG_TAG, result.toString())
    }

    private fun textMediaDownload() {
        startActivity(Intent(this, MediaDownloadActivity::class.java))
    }

    private fun testSpotlight() {
        spotlightTarget.setOnClickListener {
            val firstRoot = FrameLayout(this)
            val first = layoutInflater.inflate(R.layout.fragment_spotlight, firstRoot)
            val firstTarget = Target.Builder()
                .setAnchor(findViewById<ImageView>(R.id.spotlightTarget))
                .setShape(com.takusemba.spotlight.shape.Circle(100f))
                .setOverlay(first)
                .build()

            val spotlight = Spotlight.Builder(this)
                .setTargets(firstTarget)
                .setBackgroundColor(R.color.spotlightBackground)
                .setDuration(1000L)
                .setAnimation(DecelerateInterpolator(2f))
                .build()

            spotlight.start()

            first.column_title.text = getString(R.string.tutorial_changeAccount)
            first.column_description.text = getString(R.string.tutorial_changeAccountDesc)
            first.isClickable = true
            first.setOnClickListener {
                spotlight.finish()
            }
        }
    }

    private fun testFirebaseLogging() {
        fbLog.log("Test_Event",
            Pair("Param1", "Hello, World!"),
            Pair("Param2", "Second Run"),
            Pair("Param3", "Zi be ga go sip da"))
    }

    private fun testBillingActivity() {
        val intent = Intent(this, BillingActivity::class.java)
        startActivity(intent)
    }

    private fun testRewardAd() {
        RewardedAdAdapter.show(this, object: RewardedAdAdapter.RewardInterface {
            override fun onFinished() {
                finish()
            }
        })
    }

    private fun testRecyclerInjector() {
        setContentView(R.layout.full_recycler_view)

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = "테스트 제목"
                view.title_description.text = "제목 설명"
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_image.setOvalColor(Color.RED)
                view.cardbutton_image.setImageResource(R.mipmap.ic_launcher)
                view.cardbutton_text.text = "테스트 버튼"
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = "헤더 제목"
                view.column_description.text = "헤더 설명"
            }
        })

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    // region TextSurface

    private fun testTextSufrace() {

        val robotoBlack = ResourcesCompat.getFont(this, R.font.noto_sans_regular)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.typeface = robotoBlack

        val __text1__ = createText(paint, "bra Anies", null)
        val __text2__ = createText(paint, "bra Anies", __text1__)
        val __text3__ = createText(paint, "hy's n fokken gam bra.", __text2__)
        val __text4__ = createText(paint, "Haai!!", __text3__)
        val __text5__ = createText(paint, "Daai Anies", __text4__)
        val __text7__ = createText(paint, "hy lam innie mang ja.", __text5__)
        val __text8__ = createText(paint, "Throw damn", __text7__)
        val __text9__ = createText(paint, "devilish gang", __text8__)
        val __text10__ = createText(paint, "signs in the air.", __text9__)

        Thread(Runnable {
            val text1Delay: Long = 750 + 600 + 300 + 600 -1000
            runOnUiThread {
                textSurface.play(
                    Sequential(
                        ShapeReveal.create(__text1__, 750, SideCut.show(Side.LEFT), false),
                        Parallel(
                            ShapeReveal.create(__text1__, 600, SideCut.hide(Side.LEFT), false),
                            Sequential(
                                Delay.duration(300),
                                ShapeReveal.create(__text1__, 600, SideCut.show(Side.LEFT), false)
                            )
                        )
                    )
                )
            }
            Thread.sleep(text1Delay)

            val text2Delay: Long = 500+1300+500 -1000
            runOnUiThread {
                textSurface.play(
                    Sequential(
                        Parallel(
                            TransSurface(500, __text2__, Pivot.CENTER),
                            ShapeReveal.create(__text2__, 1300, SideCut.show(Side.LEFT), false)
                        ),
                        Delay.duration(500)
                    )
                )
            }
            Thread.sleep(text2Delay)

            val text3Delay: Long = 750+750+750+500 -1000
            runOnUiThread {
                textSurface.play(
                    Sequential(
                        Parallel(
                            TransSurface(750, __text3__, Pivot.CENTER),
                            Slide.showFrom(Side.LEFT, __text3__, 750),
                            ChangeColor.to(__text3__, 750, Color.WHITE)
                        ),
                        Delay.duration(500)
                    )
                )
            }
            Thread.sleep(text3Delay)

            val text4Delay: Long = 500+750 -1000
            runOnUiThread {
                textSurface.play(
                    Sequential(
                        Parallel(
                            TransSurface.toCenter(__text4__, 500),
                            Rotate3D.showFromSide(__text4__, 750, Pivot.TOP)
                        )))}

            Thread.sleep(text4Delay)

            val text5Delay: Long = 1000 -1000

            runOnUiThread {
                textSurface.play(
                    Sequential(
                        Parallel(
                            TransSurface.toCenter(__text5__, 500),
                            Slide.showFrom(Side.TOP, __text5__, 500)
                        )))}

            Thread.sleep(text5Delay)

            val text6Delay: Long = 750+500 -1000
            runOnUiThread {
                textSurface.play(
                    Sequential(
                        Parallel(
                            TransSurface.toCenter(__text7__, 750),
                            Slide.showFrom(Side.LEFT, __text7__, 500)
                        )))}

            Thread.sleep(text6Delay)

            val text7Delay: Long = 750+750+750+500 -1000
            runOnUiThread {
                textSurface.play(
                    Sequential(
                        Delay.duration(500),
                        Parallel(
                            TransSurface(1500, __text10__, Pivot.CENTER),
                            Sequential(
                                Sequential(
                                    ShapeReveal.create(
                                        __text8__,
                                        500,
                                        Circle.show(Side.CENTER, Direction.OUT),
                                        false
                                    )
                                ),
                                Sequential(
                                    ShapeReveal.create(
                                        __text9__,
                                        500,
                                        Circle.show(Side.CENTER, Direction.OUT),
                                        false
                                    )
                                ),
                                Sequential(
                                    ShapeReveal.create(
                                        __text10__,
                                        500,
                                        Circle.show(Side.CENTER, Direction.OUT),
                                        false
                                    )
                                )
                            )
                        ),
                        Delay.duration(200),
                        Parallel(
                            ShapeReveal.create(__text8__, 1500, SideCut.hide(Side.LEFT), true),
                            Sequential(
                                Delay.duration(250),
                                ShapeReveal.create(__text9__, 1500, SideCut.hide(Side.LEFT), true)
                            ),
                            Sequential(
                                Delay.duration(500),
                                ShapeReveal.create(__text10__, 1500, SideCut.hide(Side.LEFT), true)
                            ),
                            Alpha.hide(__text7__, 1500),
                            Alpha.hide(__text5__, 1500)
                        )
                    )
                )
            }
        }).start()
    }


    private fun createText(paint: Paint, text: String, prev: Text?): Text? {
        val t = TextBuilder
            .create(text)
            .setPaint(paint)
            .setSize(Random.nextFloat() % 20 + 30)
            .setAlpha(0)
            .setColor(randomColor())
        if (prev == null) t.setPosition(SURFACE_CENTER)
        else t.setPosition(randomPosition(), prev)

        return t.build()
    }

    private fun randomColor(): Int {
        return when (Random.nextInt() % 5) {
            0 -> Color.RED
            else -> Color.WHITE
        }
    }

    private fun randomPosition(): Int {
        return when (Random.nextInt() % 3) {
            0 -> RIGHT_OF
            1 -> BOTTOM_OF or CENTER_OF
            else -> BOTTOM_OF
        }
    }


    // endregion
}


