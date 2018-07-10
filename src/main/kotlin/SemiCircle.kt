import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.PI
import kotlin.math.round

fun toRad(deg: Double): Double {
    return deg/180* PI
}

class SemiCircle(val center: Vector2, val radius: Double, val startAngle: Double, val endAngle: Double, val t: Int, val nSteps: Int, val reverse: Boolean, val upDown: Boolean) {

    fun draw(drawer: Drawer) {
        drawer.pushStyle()
        drawer.pushTransforms()
        drawer.translate(this.center)
//        val start = min(this.startAngle, this.endAngle)
//        val end = max(this.startAngle, this.endAngle)
        var start: Double
        var end: Double
            start = this.startAngle
            end = this.endAngle
//        }
        drawer.rotate(start)

        val step = (end - start) / nSteps
        val endStep = t

        if (reverse) {
            drawer.scale(-1.0, 1.0)
        }
        if (upDown) {
            drawer.scale(1.0, -1.0)
        }
        val scCont = Circle(Vector2(0.0, 0.0), radius).contour.sub(0.0, 0.5 * t / nSteps)
        drawer.contour(scCont)

//      This is much slower!!!
//        for (i in 0..t-1) {
//            val angle = i * step
//            val p0 = Vector2(Math.cos(toRad(angle)) * this.radius, Math.sin(toRad(angle)) * this.radius)
////            drawer.circle(p0, 5.0)
//            val p1 = Vector2(Math.cos(toRad(angle+step)) * this.radius, Math.sin(toRad(angle+step)) * this.radius)
//            drawer.lineSegment(p0, p1)
//        }
//

        drawer.popTransforms()
        drawer.popStyle()
    }
}