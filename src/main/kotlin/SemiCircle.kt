import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle

//fun toRad(deg: Double): Double {
//    return deg/180* PI
//}

class SemiCircle(private val center: Vector2, private val radius: Double, private val startAngle: Double, private val t: Int, private val nSteps: Int, private val reverse: Boolean, private val upDown: Boolean) {

    fun draw(drawer: Drawer) {
        drawer.pushStyle()
        drawer.pushTransforms()
        drawer.translate(this.center)
        val start: Double = this.startAngle
        drawer.rotate(start)

        if (reverse) {
            drawer.scale(-1.0, 1.0)
        }
        if (upDown) {
            drawer.scale(1.0, -1.0)
        }
        val scCont = Circle(Vector2(0.0, 0.0), radius).contour.sub(0.0, 0.5 * t / nSteps)
        drawer.contour(scCont)

        drawer.popTransforms()
        drawer.popStyle()
    }
}