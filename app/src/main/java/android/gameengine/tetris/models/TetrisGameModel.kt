package android.gameengine.tetris.models

import android.gameengine.tetris.presenters.GameModel
import android.gameengine.tetris.presenters.GameTurn
import android.gameengine.tetris.presenters.Point
import android.gameengine.tetris.presenters.PointType
import android.gameengine.tetris.presenters.PresenterCompletableObserver
import android.gameengine.tetris.presenters.PresenterObserver
import android.os.Build
import android.os.Looper
import java.util.LinkedList
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log
import kotlin.math.max

class TetrisGameModel : GameModel {

    companion object{
        private const val GAME_SIZE = 15
        private const val PLAYING_AREA_WIDTH = 10
        private const val PLAYING_AREA_HEIGHT = GAME_SIZE
        private const val UPCOMING_AREA_SIZE = 4

    }

    private lateinit var mPoints : Array<Array<Point>>
    private lateinit var mPlayingPoints : Array<Array<Point>>
    private lateinit var mUpcomingPoints : Array<Array<Point>>

    private var mScore = 0
    private val mIsGamePaused = AtomicBoolean()
    private val mIsTurning = AtomicBoolean()
    private val mFallingPoints = LinkedList<Point>()

    private val mHandler = android.os.Handler(Looper.getMainLooper())

    private lateinit var mGameOverObserver: PresenterCompletableObserver
    private lateinit var mScoreUpdatedObserver: PresenterObserver<Int>


    private enum class BrickType(val mValue: Int) {
        L(0), T(1), CHAIR(2), STICK(3), SQUARE(4);

        companion object{
            fun fromValue(value: Int): BrickType {
                return when(value) {
                    1 -> T
                    2 -> CHAIR
                    3 -> STICK
                    4 -> SQUARE
                    else -> L
                 }
            }

            fun random() : BrickType {
                val random = Random()
                return fromValue(random.nextInt(5))
            }
        }


    }


    override fun init() {
        mPoints = Array(GAME_SIZE) { i ->
            Array(GAME_SIZE) { j ->
                Point(j, i)
            }
        }

        mPlayingPoints = Array(PLAYING_AREA_HEIGHT) { i ->
            mPoints[i].filterNotNull().toTypedArray()
        }
        mUpcomingPoints = Array(UPCOMING_AREA_SIZE) { i ->
            Array(UPCOMING_AREA_SIZE) { j ->
                mPoints[1 + i][PLAYING_AREA_WIDTH + 1 + j]
            }
        }

        for (i in 0 until PLAYING_AREA_HEIGHT) {
            mPoints[i][PLAYING_AREA_WIDTH].type = PointType.VERTICAL_LINE
        }
        newGame()
    }

    override fun getGameSize(): Int {
        return GAME_SIZE
    }

    override fun newGame() {
        mScore = 0
        for (i in 0 until PLAYING_AREA_HEIGHT) {
            for (j in 0 until PLAYING_AREA_WIDTH) {
                mPlayingPoints[i][j].type = PointType.EMPTY
            }
        }
        mFallingPoints.clear()
        generateUpcomingBrick()
    }

    private fun generateUpcomingBrick() {
        val upcomingBrick = BrickType.random()
        for (i in 0 until UPCOMING_AREA_SIZE) {
            for (j in 0 until UPCOMING_AREA_SIZE) {
                mUpcomingPoints[i][j].type = PointType.EMPTY
            }
        }
        when (upcomingBrick) {
            BrickType.L -> {
                mUpcomingPoints[1][1].type = PointType.BOX
                mUpcomingPoints[2][1].type = PointType.BOX
                mUpcomingPoints[3][1].type = PointType.BOX
                mUpcomingPoints[3][2].type = PointType.BOX
            }
            BrickType.T -> {
                mUpcomingPoints[1][1].type = PointType.BOX
                mUpcomingPoints[2][1].type = PointType.BOX
                mUpcomingPoints[3][1].type = PointType.BOX
                mUpcomingPoints[2][2].type = PointType.BOX
            }
            BrickType.CHAIR -> {
                mUpcomingPoints[1][1].type = PointType.BOX
                mUpcomingPoints[2][1].type = PointType.BOX
                mUpcomingPoints[2][2].type = PointType.BOX
                mUpcomingPoints[3][2].type = PointType.BOX
            }
            BrickType.STICK -> {
                mUpcomingPoints[0][1].type = PointType.BOX
                mUpcomingPoints[1][1].type = PointType.BOX
                mUpcomingPoints[2][1].type = PointType.BOX
                mUpcomingPoints[3][1].type = PointType.BOX
            }
            BrickType.SQUARE -> {
                mUpcomingPoints[1][1].type = PointType.BOX
                mUpcomingPoints[1][2].type = PointType.BOX
                mUpcomingPoints[2][1].type = PointType.BOX
                mUpcomingPoints[2][2].type = PointType.BOX
            }
        }
    }


    override fun startGame(onGameDrawnListener: PresenterObserver<Array<Array<Point>>>) {
        mIsGamePaused.set(false)
        val sleepTime: Long = 1000L / FPS.toLong()

        Thread {
            var count: Long = 0
            while (!mIsGamePaused.get()) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                if (count % SPEED == 0L) {
                    if (mIsTurning.get())
                        continue
                    next()
                    mHandler.post { onGameDrawnListener.observe(mPoints) }
                }
                count++
            }
        }.start()
    }

    private fun next() {
        synchronized(this) {

            updateFallingPoints()

            if (isNextMerged()) {
                if (isOutSide()) {
                    if (mGameOverObserver != null) {
                        mHandler.post(mGameOverObserver::observe)
                    }
                    mIsGamePaused.set(true)
                    return
                }

                var y: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mFallingPoints.stream().mapToInt { p -> p.y }.max().orElse(-1)
                } else {
                    -1
                }

                while (y >= 0) {
                    var isScored = true
                    for (i in 0 until PLAYING_AREA_WIDTH) {
                        val point: Point? = getPlayingPoint(i, y)
                        if (point?.type == PointType.EMPTY) {
                            isScored = false
                            break
                        }
                    }
                    if (isScored) {
                        mScore++
                        if (mScoreUpdatedObserver != null) {
                            mHandler.post { mScoreUpdatedObserver.observe(mScore) }
                        }
                        val tmPoints : LinkedList<Point> = LinkedList<Point>()
                        for (i in y downTo 0) {
                            for (j in 0 until PLAYING_AREA_WIDTH) {
                                val point = getPlayingPoint(j, i)
                                if (point?.type == PointType.BOX) {
                                    point.type = PointType.EMPTY
                                    if (i != y) {
                                        tmPoints.add(Point(point.x, point.y + 1,
                                            PointType.BOX, false))
                                    }
                                }
                            }
                        }
                        tmPoints.forEach(this::updatePlayingPoints)
                    }else {
                        y--
                    }
                }
                mFallingPoints.forEach { p-> p.isFallingPoint = false}
                mFallingPoints.clear()
            } else {
                val tmPoints : LinkedList<Point> = LinkedList<Point>()
                for (fallingPoint: Point in mFallingPoints) {
                    fallingPoint.type = PointType.EMPTY
                    fallingPoint.isFallingPoint = false
                    tmPoints.add(
                        Point(fallingPoint.x, fallingPoint.y + 1,
                            PointType.BOX, true)
                    )
                }
                mFallingPoints.clear()
                mFallingPoints.addAll(tmPoints)
                mFallingPoints.forEach(this::updatePlayingPoints)
            }
        }

    }

    private fun updateFallingPoints() {
        if (mFallingPoints.isEmpty()) {
            for (i in 0 until UPCOMING_AREA_SIZE) {
                for (j in 0 until UPCOMING_AREA_SIZE) {
                    if (mUpcomingPoints[i][j].type == PointType.BOX){
                        mFallingPoints.add(Point(j + 3, i - 4,
                            PointType.BOX, true))
                    }

                }
            }
            generateUpcomingBrick()
        }
    }

    private fun isNextMerged(): Boolean {
        for (fallingPoint : Point in mFallingPoints){
            if (fallingPoint.y + 1 >= 0 && (fallingPoint.y == PLAYING_AREA_HEIGHT - 1 ||
                        getPlayingPoint(fallingPoint.x, fallingPoint.y + 1)?.isStablePoint() == true)){
                return true
            }
        }
        return false
    }

    private fun isOutSide(): Boolean {
        for (fallingPoint: Point in mFallingPoints) {
            if (fallingPoint.y < 0) {
                return true
            }
        }
        return false
    }

    private fun updatePlayingPoints(point : Point) {
        if (point.x in 0 until PLAYING_AREA_WIDTH &&
            point.y >= 0 && point.y < PLAYING_AREA_HEIGHT) {
            mPoints[point.y][point.x] = point
            mPlayingPoints[point.y][point.x] = point
        }
    }

    private fun getPlayingPoint(x: Int, y: Int): Point? {
        if (x >= 0 && y >= 0 && x < PLAYING_AREA_HEIGHT && y < PLAYING_AREA_HEIGHT) {
            return mPlayingPoints[y][x]
        }
        return null
    }
    override fun pauseGame() {
        mIsGamePaused.set(true)
    }

    override fun turn(turn: GameTurn) {
        if (mIsGamePaused.get() || mIsTurning.get()) {
            return
        }

        mIsTurning.set(true)
        val tmPoints = LinkedList<Point>()
        var canTurn : Boolean

        when (turn){
            GameTurn.LEFT -> {
                updateFallingPoints()
                canTurn = true
                for (fallingPoint: Point in mFallingPoints) {
                    if (fallingPoint.y >= 0 && (fallingPoint.x == 0) ||
                        getPlayingPoint(fallingPoint.x - 1, fallingPoint.y)?.isStablePoint() == true) {
                        canTurn = false
                        break
                    }
                }
                if (canTurn) {
                    for (fallingPoint in mFallingPoints) {
                        tmPoints.add(Point(fallingPoint.x - 1, fallingPoint.y, PointType.BOX, true))
                        fallingPoint.type = PointType.EMPTY
                        fallingPoint.isFallingPoint = false
                    }
                    mFallingPoints.clear()
                    mFallingPoints.addAll(tmPoints)
                    mFallingPoints.forEach {  this.updatePlayingPoints(it) }
                }
            }
            GameTurn.RIGHT -> {
                updateFallingPoints()
                canTurn = true
                for (fallingPoint: Point in mFallingPoints) {
                    if (fallingPoint.y >= 0 && (fallingPoint.x == PLAYING_AREA_WIDTH - 1) ||
                        getPlayingPoint(fallingPoint.x + 1, fallingPoint.y)?.isStablePoint() == true) {
                        canTurn = false
                        break
                    }
                }
                if (canTurn) {
                    for (fallingPoint in mFallingPoints) {
                        tmPoints.add(Point(fallingPoint.x + 1, fallingPoint.y, PointType.BOX, true))
                        fallingPoint.type = PointType.EMPTY
                        fallingPoint.isFallingPoint = false
                    }
                    mFallingPoints.clear()
                    mFallingPoints.addAll(tmPoints)
                    mFallingPoints.forEach { this.updatePlayingPoints(it) }
                }
            }
            GameTurn.DOWN -> {
                next()
            }
            GameTurn.FIRE -> {
                rotateFallingPoints()
            }
            else -> {}

        }

        mIsTurning.set(false)

    }

    private fun rotateFallingPoints() {
        updateFallingPoints()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val left = mFallingPoints.stream().mapToInt { p -> p.x }.min().orElse(-1)
            val right = mFallingPoints.stream().mapToInt { p -> p.x }.max().orElse(-1)
            val top = mFallingPoints.stream().mapToInt { p -> p.y }.min().orElse(-1)
            val bottom = mFallingPoints.stream().mapToInt { p -> p.y }.max().orElse(-1)
            val size = max(right -left, bottom - top) + 1

            if (rotatePoints(left, top, size))
                return
            if (rotatePoints(right - size + 1, top, size))
                return
            if (rotatePoints(left, bottom - size + 1, size))
                return

            rotatePoints(right - size + 1, bottom - size + 1, size)

        } else {
            println("Error SDK Build version")
        }

    }

    private fun rotatePoints(x: Int, y: Int, size: Int): Boolean {
        if (x + size - 1 < 0 || x + size - 1 >= PLAYING_AREA_WIDTH) {
            return false
        }

        var canRotate : Boolean = true
        val points : Array<Array<Point?>> = Array(size) { arrayOfNulls<Point>(size) }

        for (i in 0 until size){
            for (j in 0 until size){
                var point = getPlayingPoint(j + x, i + y);
                if (point == null) {
                    val tmpX = x + j;
                    val tmpY = y + j;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        point = mFallingPoints.stream()
                            .filter { p -> p.x == tmpX && p.y == tmpY }
                            .findFirst()
                            .orElse(Point(j + x, i + y))
                    } else {
                        println("Error SDK, cannot get point")
                    }
                }
                if (point?.isStablePoint() == true && getPlayingPoint(x + size - 1, y + j )?.isFallingPoint == true){
                    canRotate = false
                    break
                }
                points[i][j] = Point(x + size -1 -i, y + j, point?.type ?: PointType.EMPTY,
                    point?.isFallingPoint ?: false
                )
            }
            if (!canRotate)
                break
        }
        if (!canRotate)
            return false

        for (i in 0 until size){
            for (j in 0 until size){
                val point = getPlayingPoint(i + y, j + x) ?: continue
                point.type = PointType.EMPTY
            }
        }
        mFallingPoints.clear()
        for (i in 0 until size){
            for (j in 0 until size){
                points[i][j]?.let { updatePlayingPoints(it) }
                if (points[i][j]?.isFallingPoint == true) {
                    points[i][j]?.let { mFallingPoints.add(it) }
                }
            }
        }
        return true
    }

    override fun setGameOverListener(onGameOverListener: PresenterCompletableObserver) {
        mGameOverObserver = onGameOverListener
    }

    override fun setScoreUpdatedListener(onScoreUpdatedListener: PresenterObserver<Int>) {
        mScoreUpdatedObserver = onScoreUpdatedListener
    }


}