package android.gameengine.tetris.presenters

interface PresenterObserver<T> {
    fun onNext(t: T)
}