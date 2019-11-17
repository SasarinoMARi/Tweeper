package com.sasarinomari.tweetcleaner

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_uitest.*
import su.levenetc.android.textsurface.TextBuilder
import su.levenetc.android.textsurface.contants.Side
import androidx.core.content.res.ResourcesCompat
import su.levenetc.android.textsurface.Text
import su.levenetc.android.textsurface.animations.*
import su.levenetc.android.textsurface.contants.Pivot
import su.levenetc.android.textsurface.contants.Align.*
import su.levenetc.android.textsurface.contants.Direction
import kotlin.random.Random
import cn.pedant.SweetAlert.SweetAlertDialog


class UITestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uitest)

        // testTextSufrace()
        //testSweetDialog()
    }

    private fun testSweetDialog() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()

        Thread(Runnable { Thread.sleep(5000)
            runOnUiThread{
                pDialog.titleText="Changed Text"
            }}).start()
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


