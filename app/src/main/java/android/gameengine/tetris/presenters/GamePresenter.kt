package android.gameengine.tetris.presenters

import android.widget.Toast

class GamePresenter {

    private lateinit var mGameView :GameView
    private lateinit var mGameModel: GameModel
    private var mStatus: GameStatus? = null


    fun setGameView(gameView: GameView) {
        mGameView = gameView
    }

    fun setGameModel(gameModel: GameModel) {
        mGameModel = gameModel
    }

    fun init() {
        mGameView.init(mGameModel.getGameSize())
        mGameModel.init()

        mGameModel.setGameOverListener(object : PresenterCompletableObserver {
             override fun observe() {
                setStatus(GameStatus.OVER)
            }
        })
        mGameModel.setScoreUpdatedListener(object : PresenterObserver<Int> {
            override fun observe(t: Int) {
                mGameView.setScore(t)
            }
        })
        setStatus(GameStatus.START)
    }

    fun turn(turn: GameTurn) {
        mGameModel.turn(turn)
    }

    fun changeStatus() {
        if (mStatus == GameStatus.PLAYING) {
            pauseGame()
        } else {
            startGame()
        }
    }

    private fun pauseGame() {
        setStatus(GameStatus.PAUSED)
        mGameModel.pauseGame()
    }

    private fun startGame() {
        setStatus(GameStatus.PLAYING)
        mGameModel.startGame(object : PresenterObserver<Array<Array<Point>>> {
            override fun observe(grid: Array<Array<Point>>) {
                mGameView.draw(grid)
            }
        } )

    }
    private fun setStatus(status: GameStatus)  {
        if (mStatus == GameStatus.OVER || status == GameStatus.START) {
            mGameModel.newGame()
        }
        mStatus = status
        mGameView.setStatus(status)
    }
}