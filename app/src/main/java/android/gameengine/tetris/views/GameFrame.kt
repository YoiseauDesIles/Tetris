package android.gameengine.tetris.views

import android.content.Context
import android.gameengine.tetris.presenters.Point
import android.gameengine.tetris.presenters.PointType
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GameFrame : View {

    constructor(context: Context) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var mPoints : Array<Array<Point>>
    private var mBoxSize: Int = 0
    private var mBoxPadding: Int = 0
    private var mGameSize: Int = 0
    private val mPaint: Paint = Paint()

    fun init(gamesize: Int) {
        mGameSize = gamesize
        viewTreeObserver.addOnGlobalLayoutListener {
            mBoxSize = Math.min(width, height) / mGameSize
            mBoxPadding = mBoxSize / 10
        }
    }

    fun setPoints(points: Array<Array<Point>>){
        mPoints = points
    }

    private fun getPoint(x: Int, y: Int) = mPoints[x][y]

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPaint.color = Color.BLACK
        canvas?.drawRect(0.0F, 0.0F, mGameSize.toFloat(), mGameSize.toFloat(), mPaint)
        if (mPoints == null)
            return
        for (i in 0 until mGameSize) {
            for (j in 0 until mGameSize) {
                var point: Point = getPoint(i, j)
                var left: Int = 0
                var right: Int = 0
                var top: Int = 0
                var bottom: Int = 0
                mPaint.color = Color.WHITE
                when(point.type) {
                    PointType.BOX -> {
                        left = mBoxSize * point.x + mBoxPadding;
                        right = left + mBoxSize - mBoxPadding
                        top = mBoxSize * point.y + mBoxPadding
                        bottom = top + mBoxSize - mBoxPadding
                    }
                    PointType.VERTICAL_LINE -> {
                        left = mBoxSize * point.x
                        right = left + mBoxPadding
                        top = mBoxSize * point.y
                        bottom = top + mBoxSize
                    }
                    PointType.HORIZONTAL_LINE -> {
                        left = mBoxSize * point.y
                        right = left + mBoxSize
                        top = mBoxSize * point.y
                        bottom = top + mBoxPadding
                    }
                    PointType.EMPTY -> {

                    }
                    else -> {
                        left = mBoxSize * point.x
                        right = left + mBoxSize
                        top = mBoxSize * point.y
                        bottom = top + mBoxSize
                        mPaint.color = Color.BLACK
                    }
                }
                canvas?.drawRect(left.toFloat(), top.toFloat(), right.toFloat(),
                    bottom.toFloat(), mPaint)
            }
        }


    }

}