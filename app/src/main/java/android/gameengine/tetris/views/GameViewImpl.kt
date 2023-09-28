package android.gameengine.tetris.views

import android.gameengine.tetris.presenters.GameStatus
import android.gameengine.tetris.presenters.GameView
import android.gameengine.tetris.presenters.Point
import android.view.View
import android.widget.Button
import android.widget.TextView

class GameViewImpl(
    private val mGameFrame: GameFrame,
    private val mGameScoreText: TextView,
    private val mGameStatusText: TextView,
    private val mGameCtlBtn : Button) : GameView {

    override fun init(gameSize: Int) {
        mGameFrame.init(gameSize)
    }

    override fun draw(points: Array<Array<Point>>) {
        mGameFrame.setPoints(points)
        mGameFrame.invalidate()
    }

    override fun setScore(score: Int) {
        mGameScoreText.text = "Score : $score"
    }

    override fun setStatus(status: GameStatus) {
        mGameStatusText.text = status.mValue
        mGameStatusText.visibility = if (status == GameStatus.PLAYING)
            View.INVISIBLE else View.VISIBLE
        mGameCtlBtn.text = if (status == GameStatus.PLAYING) "Pause" else "Start"
    }

}