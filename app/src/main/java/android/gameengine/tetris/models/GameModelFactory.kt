package android.gameengine.tetris.models

import android.gameengine.tetris.models.GameType
import android.gameengine.tetris.models.TetrisGameModel
import android.gameengine.tetris.presenters.GameModel

class GameModelFactory {
    companion object{
        fun newGameModel(type: GameType): GameModel? {
            return when(type) {
                GameType.TETRIS -> TetrisGameModel()
                else -> null
            }
        }
    }
}