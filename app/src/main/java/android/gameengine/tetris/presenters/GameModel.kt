package android.gameengine.tetris.presenters

interface GameModel {

    val FPS: Int
        get() = 60
    val SPEED: Int
        get() = 25


    fun init()
    fun getGameSize() : Int
    fun newGame()
    fun startGame(onGameDrawnListener : PresenterObserver<Array<Array<Point>>>)
    fun pauseGame()
    fun turn(gameTurn: GameTurn)
    fun setGameOverListener(onGameOverListener: PresenterCompletableObserver)
    fun setScoreUpdatedListener(onScoreUpdatedListener: PresenterObserver<Int>)



}