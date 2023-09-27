package android.gameengine.tetris.presenters

enum class GameStatus (val mValue: String){

    START("START"),
    PLAYING("PLAYING"),
    OVER("GAME OVER"),
    PAUSED("GAME PAUSED"),
    NOT_INITIALIZED("NOT INITIALIZED");

    fun getValue() = mValue

}