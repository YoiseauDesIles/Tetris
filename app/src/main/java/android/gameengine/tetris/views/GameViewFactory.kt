package android.gameengine.tetris.views

import android.gameengine.tetris.presenters.GameView
import android.widget.Button
import android.widget.TextView

class GameViewFactory {

    companion object{
        fun newGameView(
            gameFrame: GameFrame,
            gameScoreText: TextView,
            gameStatusText: TextView,
            gameCtlBtn : Button) : GameView {

            return GameViewImpl(gameFrame, gameScoreText, gameStatusText, gameCtlBtn)
        }
    }
}