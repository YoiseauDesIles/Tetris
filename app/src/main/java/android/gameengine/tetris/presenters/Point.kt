package android.gameengine.tetris.presenters

class Point (val x: Int, val y: Int) {

    var isFallingPoint : Boolean = false
    var type: PointType = PointType.EMPTY

    constructor(x: Int, y : Int, pointType: PointType, isFallingPoint: Boolean) : this(x, y) {
        this.isFallingPoint = isFallingPoint
        this.type = pointType
    }

    fun isStablePoint() : Boolean? {
        return !isFallingPoint && type == PointType.BOX
    }
}