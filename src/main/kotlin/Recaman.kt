// Visualisation of the Recaman Series https://oeis.org/A005132
//
// play piano note for new values

import org.jfugue.player.Player
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.configuration
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import kotlin.math.abs
import kotlin.math.round

const val size   = 1000
const val margin =  70

const val nElemMax        = 200
const val semicircleSteps = 20
const val fixScale        = false
const val scaleSteps      = 80

const val variation = 3

var drawNoteAtXY1   = false     // This is a fixed point just changing size
var drawNoteAtXY2   = false     // This is a bouncing point
var vibratingAxis1  = false     // This is like plucked string
var vibratingAxis2  = false     // This is like piano keys
var runAnimation    = true      // Can be used to stop the animation

fun manualScale(n: Int): Double {
    return when {
        n <=  16 ->  20.930232558139537
        n <=  31 ->  11.39240506329114
        n <=  64 ->   6.732484076433121
        n <=  99 ->   3.3962264150943398
        n <= 112 ->   2.37467018469657
        n <= 170 ->   1.8181818181818181
        n <= 187 ->   1.3996889580093312
        else ->       1.0804321728691477
    }
}

fun recaman(nmax: Int): MutableList<Int> {
    val list = mutableListOf<Int>()
    list.add(0)
    for (n in 1..nmax) {
        val next = list[n-1] - n
        if ( (next > 0) && ! list.contains(next)) {
            list.add(next)
        } else {
            list.add( list[n-1] + n )
        }
    }
    return list
}

class Recaman: Program() {
    private var scale   = manualScale(0)
    private var scaleTarget     = scale
    private var scaleTargetPrev = scale
    private var scaleStepCount = -1
    private lateinit var reclist : MutableList<Int>

    private var nElem = 0
    private var t     = semicircleSteps + 1

    override fun setup() {
        setupKeyEvents()
        reclist = recaman(nElemMax)
        println("reclist: $reclist")

        when (variation) {
            0 -> {
                drawNoteAtXY1 = false
                drawNoteAtXY2 = false
                vibratingAxis1 = false
                vibratingAxis2 = false
            }
            1 -> {
                drawNoteAtXY1 = true
                drawNoteAtXY2 = false
                vibratingAxis1 = false
                vibratingAxis2 = false
            }
            2 -> {
                drawNoteAtXY1 = false
                drawNoteAtXY2 = true
                vibratingAxis1 = true
                vibratingAxis2 = false
            }
            3 -> {
                drawNoteAtXY1 = false
                drawNoteAtXY2 = true
                vibratingAxis1 = false
                vibratingAxis2 = true
            }
            else -> {}
        }
    }

    override fun draw() {

        super.draw()

        drawer.pushTransforms()
        drawer.translate(margin.toDouble(), size - margin.toDouble())
        drawer.scale(1.0, -1.0)

        drawer.background(ColorRGBa.WHITE)
        drawer.fill   = ColorRGBa(0.84, 0.07, 0.13, 1.0)
        drawer.stroke = ColorRGBa(0.84, 0.07, 0.13, 1.0)

        if (! runAnimation) return

        // Progress through nElem and t
        if (t < semicircleSteps-1) {
            t += 1
        } else {
            if (nElem <= nElemMax) {
                nElem += 1
                val note = val2note(reclist[nElem-1])
                Player().delayPlay(0, note.toString())
                println("nElem: $nElem Seq value ${reclist[nElem-1]} - going to play $note ...")
                t = 0
            }
        }

        if (fixScale) {
            val maxVal = reclist.max() ?: 1
            scale = (size - 2 * margin) / maxVal.toDouble()
//            println("nElem: $nElem, maxVal: $maxVal, scale: $scale")
        } else {
            scaleTarget = manualScale(nElem) * 5/6
            // Scale adjustment via easing function
            if (scaleTarget != scaleTargetPrev) {
                // Ease the scale ...
                if (scaleStepCount < 0) {
                    // currently not scaling ...
                    scaleStepCount = 0
                }
                scaleStepCount += 1
                scale = scaleTargetPrev + (scaleTarget - scaleTargetPrev) * ease(scaleStepCount/scaleSteps.toDouble(), 2.0, 2.0)
                if (scaleStepCount == scaleSteps) {
                    // end of scaling
                    scaleStepCount = -1
                    scaleTargetPrev = scaleTarget
                }
            }
        }

        for (i in 0 until nElem) {

            val c = if (i < nElemMax) (reclist[i] + reclist[i + 1]) / 2.0 else 0.0
            val r = if (i < nElemMax) abs(reclist[i] - reclist[i + 1]) / 2.0 else 0.0
            val reverse = if (i < nElemMax) reclist[i] > reclist[i+1] else false
            val upDown = i%2 == 1

            if (i < nElem -1) {
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, semicircleSteps, semicircleSteps, reverse, upDown)
                sc.draw(drawer)
            } else { // last semicircle: draw partially
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, t, semicircleSteps, reverse, upDown)
                sc.draw(drawer)

                // Draw note event at xy-axis
                if (drawNoteAtXY1) {
                    drawer.pushStyle()
                    drawer.stroke = null
                    val (dx, dy, w) = dotParamsA(t, semicircleSteps, reclist[i], scale)
                    drawer.fill = ColorRGBa(1.0, 0.0, 0.0, 1.0)
                    drawer.circle(Vector2(dx, dy), w)
                    drawer.popStyle()
                }
                // Draw note event at xy-axis
                if (drawNoteAtXY2) {
                    drawer.pushStyle()
                    drawer.stroke = null
                    val (dx, dy, w) = dotParamsB(t, semicircleSteps, reclist[i], scale)
                    drawer.fill = ColorRGBa(1.0, 0.0, 0.0, 1.0)
                    drawer.circle(Vector2(dx, dy), w)
                    drawer.popStyle()
                }

                // Draw axis somehow
                if (vibratingAxis1) {
                    vibratingLine1(drawer, t, semicircleSteps, reclist[i], scale)
                }
                if (vibratingAxis2) {
                    val currRLmax = round( (size - 2*margin) / scale).toInt()
                    vibratingLine2(drawer, t, semicircleSteps, reclist[i], scale, currRLmax)
                }
            }
        }

        drawer.popTransforms()
    }

    private fun setupKeyEvents() {
        println("Interaction: SPACE to stop animation, p to toggle image")
        keyboard.keyDown.listen {
            // -- it refers to a KeyEvent instance here
            if (it.key == 32) {             // SPACE
                runAnimation = ! runAnimation
                println("SPACE pressed - runAnimation now $runAnimation")
            }
        }
    }
}

fun dotParamsA(t: Int, semicircleSteps: Int, rli: Int, scale: Double) : Triple<Double, Double, Double> {
    // coordinates fixed
    val dx = rli * scale
    val dy = rli * scale
    val w = Math.max(scale/2, 3.0)  * (1-Math.pow(t/semicircleSteps.toDouble(), 2.0)) //* (1 + 0.2* Math.sin(t/2.5*Math.PI))
    return Triple(dx, dy, w)
}

fun dotParamsB(t: Int, semicircleSteps: Int, rli: Int, scale: Double) : Triple<Double, Double, Double> {
    // ccordinates move
    val dx = rli * scale - 10* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val dy = rli * scale + 10* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val w = Math.max(scale/2, 5.0)  * (1-Math.sqrt(t/semicircleSteps.toDouble())) //* (1 + 0.2* Math.sin(t/2.5*Math.PI))
    return Triple(dx, dy, w)
}


fun dotParams1(t: Int, semicircleSteps: Int, rli: Int, scale: Double) : Triple<Double, Double, Double> {
    val dx = rli * scale - Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val dy = rli * scale + Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val w = 0.0
    return Triple(dx, dy, w)
}

fun vibratingLine1(drawer: Drawer, t: Int, semicircleSteps: Int, rli: Int, scale: Double) {
    val (dx, dy, w) = dotParams1(t, semicircleSteps, rli, scale)
    drawer.pushStyle()
    drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.3)
    drawer.lineSegment(0.0, 0.0, dx -scale/2, dy-scale/2)
    drawer.lineSegment(dx -scale/2, dy-scale/2, dx+scale/2, dy+scale/2)
    drawer.lineSegment(dx+scale/2, dy+scale/2, 1000.0, 1000.0)
    drawer.popStyle()
}


fun dotParams2(t: Int, semicircleSteps: Int, rli: Int, scale: Double) : Triple<Double, Double, Double> {
    val dx = rli * scale - Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val dy = rli * scale + Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val w = 0.0
    return Triple(dx, dy, w)
}

fun vibratingLine2(drawer: Drawer, t: Int, semicircleSteps: Int, rli: Int, scale: Double, currRLmax: Int) {
    val (dx, dy, w) = dotParams2(t, semicircleSteps, rli, scale)
    drawer.pushStyle()
    drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.2)
    for (i in 0 until rli) {
        drawer.lineSegment((i-0.35)*scale, (i-0.35)*scale, (i+0.35)*scale, (i+0.35)*scale)
    }
    drawer.lineSegment(dx - 0.35*scale, dy- 0.35*scale, dx+ 0.35*scale, dy+ 0.35*scale)
    for (i in rli+1..currRLmax) {
        drawer.lineSegment((i-0.35)*scale, (i-0.35)*scale, (i+0.35)*scale, (i+0.35)*scale)
    }
    drawer.popStyle()
}

fun val2note(rli: Int): Int {
    // translate from integers to possible piano notes
    return 20 + rli % 88
}

fun ease(t: Double, g1: Double, g2: Double): Double {
    return if (t < 0.5) {
        0.5 * Math.pow(2 * t, g1)
    } else {
        1 - 0.5 * Math.pow(2 * (1 - t), g2)
    }
}

fun main(args: Array<String>) {

    application(Recaman(),
            configuration {
                width  = size
                height = size
//                unfocusBehaviour = UnfocusBehaviour.THROTTLE
            })
}