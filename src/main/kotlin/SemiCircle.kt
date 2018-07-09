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
//        if (reverse) {
//            start = this.endAngle
//            end = this.startAngle
//        } else {
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

//        // Try with a moving note dot ... not so nice somehow
//        // debugging/understanding
//        println(t)
//        val startpoint1 = scCont.segments[0].start
//        val endpoint1 = scCont.segments[0].end
////        drawer.circle(startpoint1,  4.0)
//        val r = Math.max(15.0 * (1 - t*2.0/nSteps.toDouble()), 0.0)
//        drawer.circle(endpoint1,    r)
////        if (scCont.segments.size > 1) {
////            val startpoint2 = scCont.segments[1].start
////            val endpoint2 = scCont.segments[1].end
////            drawer.circle(startpoint2, 12.0)
////            drawer.circle(endpoint2, 16.0)
////        }

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