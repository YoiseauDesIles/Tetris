package android.gameengine.tetris.presenters

enum class GameStatus (val value: String){

    START("START"),
    PLAYING("PLAYING"),
    OVER("GAME OVER"),
    PAUSED("GAME PAUSED");

    fun getValue() = value

}