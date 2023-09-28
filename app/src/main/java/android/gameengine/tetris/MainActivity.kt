package android.gameengine.tetris

import android.gameengine.tetris.models.GameType
import android.gameengine.tetris.presenters.GamePresenter
import android.gameengine.tetris.presenters.GameTurn
import android.gameengine.tetris.views.GameFrame
import android.gameengine.tetris.models.GameModelFactory
import android.gameengine.tetris.views.GameViewFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gameFrame = findViewById<GameFrame>(R.id.game_container)
        val gameScoreText = findViewById<TextView>(R.id.game_score)
        val gameStatusText = findViewById<TextView>(R.id.game_status)
        val gameCtlBtn = findViewById<Button>(R.id.game_ctl_button)

        val gamePresenter = GamePresenter()
        GameModelFactory.newGameModel(GameType.TETRIS)?.let {
            gamePresenter.setGameModel(it)
        } ?: println("Error : GameModel is null")
        gamePresenter.setGameView(GameViewFactory.newGameView(
            gameFrame, gameScoreText, gameStatusText, gameCtlBtn ))

        val downBtn = findViewById<Button>(R.id.down_btn)
        val leftBtn = findViewById<Button>(R.id.left_btn)
        val rightBtn = findViewById<Button>(R.id.right_btn)
        val fireBtn = findViewById<Button>(R.id.fire_btn)

        downBtn.setOnClickListener { gamePresenter.turn(GameTurn.DOWN) }
        leftBtn.setOnClickListener { gamePresenter.turn(GameTurn.LEFT) }
        rightBtn.setOnClickListener { gamePresenter.turn(GameTurn.RIGHT) }
        fireBtn.setOnClickListener { gamePresenter.turn(GameTurn.FIRE) }

        gameCtlBtn.setOnClickListener { gamePresenter.changeStatus() }

        gamePresenter.init()

    }
}