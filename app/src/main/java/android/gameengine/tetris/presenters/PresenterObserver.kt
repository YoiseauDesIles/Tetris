package android.gameengine.tetris.presenters

interface PresenterObserver<T> {
    fun observe(t: T)
}