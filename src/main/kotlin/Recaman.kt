// Visualisation of the Recaman Series https://oeis.org/A005132
//
// play piano note for new values
// use drawer.contour( Circle(Vector2(100.0, 100.0), 100.0).contour.sub(0.0, 0.5) on http://guide.openrndr.org/#/Tutorial_DrawingComplexShapes as it is much faster

// TODO things to show
// - variation 0 & 3 as main movies
// - variation 0 images with different length, eg. 51, 98, 169, 200
// - explain series with link, aurelisation/graphs done, recreate here animated ...
// - mention openrndr, jfugue, FluidR3_GM.sf2

import org.jfugue.pattern.Pattern
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import kotlin.math.abs
import javax.sound.midi.MidiChannel
import javax.sound.midi.Instrument
import javax.sound.midi.MidiSystem
import javax.sound.midi.Synthesizer
import kotlin.system.exitProcess
import org.jfugue.player.Player
import org.openrndr.draw.Drawer
import org.openrndr.shape.Circle
import org.openrndr.shape.Color
import org.openrndr.shape.LineSegment
import kotlin.math.round
//import org.openrndr.ffmpeg.ScreenRecorder


val size   = 1000
val margin =  100

val nElemMax        = 169
val semicircleSteps = 20
val fixScale        = false
val scaleSteps      = 80

const val variation = 3

var drawNoteAtXY1   = false     // This is a fixed point just changing size
var drawNoteAtXY2   = false     // This is a bouncing point
var vibratingAxis1  = false     // This is like plucked string
var vibratingAxis2  = false     // This is like piano keys
var runAnimation    = false

val player = Player()

fun manualScale(n: Int): Double {
//    val scaleTarget =
//            when {
//                n <=  14 ->  20.930232558139537
//                n <=  29 ->  11.39240506329114
//                n <=  62 ->   5.732484076433121
//                n <=  97 ->   3.3962264150943398
//                n <= 110 ->   2.37467018469657
//                n <= 168 ->   1.8181818181818181
//                n <= 185 ->   1.3996889580093312
//                else ->       1.0804321728691477
//            }
    val scaleTarget =
            when {
                n <=  16 ->  20.930232558139537
                n <=  31 ->  11.39240506329114
                n <=  64 ->   6.732484076433121
                n <=  99 ->   3.3962264150943398
                n <= 112 ->   2.37467018469657
                n <= 170 ->   1.8181818181818181
                n <= 187 ->   1.3996889580093312
                else ->       1.0804321728691477
            }
//    nElem: 14, maxVal: 43, scale: 35.840503477731005, scaleTarget 20.930232558139537
//    nElem: 29, maxVal: 79, scale: 14.141056432493597, scaleTarget 11.39240506329114
//    nElem: 62, maxVal: 157, scale: 7.786624203821682, scaleTarget 5.732484076433121
//    nElem: 97, maxVal: 265, scale: 3.919811320754768, scaleTarget 3.3962264150943398
//    nElem: 110, maxVal: 379, scale: 2.429564435687911, scaleTarget 2.37467018469657
//    nElem: 168, maxVal: 495, scale: 1.8181818181818201, scaleTarget 1.8181818181818181
//    nElem: 185, maxVal: 643, scale: 1.4143292563796677, scaleTarget 1.3996889580093312
//    nElem: 200, maxVal: 833, scale: 1.0804321732482505, scaleTarget 1.0804321728691477
    return scaleTarget
}

fun recaman(nmax: Int): MutableList<Int> {
    var list = mutableListOf<Int>()
    list.add(0)
    for (n in 1..nmax) {
        var next = list[n-1] - n
        if ( (next > 0) && ! list.contains(next)) {
            list.add(next)
        } else {
            list.add( list[n-1] + n )
        }
    }
    return list
}

class Recaman: Program() {
    var scale   = manualScale(0)
    var scaleTarget     = scale
    var scaleTargetPrev = scale
    var scaleStepCount = -1
    lateinit var reclist : MutableList<Int>

    var nElem = 0
    var t     = semicircleSteps + 1

    override fun setup() {
//        super.backgroundColor = null
        setupKeyEvents()
//        extend(ScreenRecorder())
        reclist = recaman(nElemMax)
        println("reclist: $reclist")
//        drawer.background(ColorRGBa.WHITE)
//        Thread.sleep(200)

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
            val nElemPlan = Math.min(nElem + 5, nElemMax)
            val maxVal = reclist.take(nElemPlan).max() ?: 1
//            val scaleTarget = (size - 2 * margin) / maxVal.toDouble()
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
//            scale = scaleTarget - (scaleTarget-scale) * 0.97

//            if (t == 0) println("t: $t, nElem: $nElem, maxVal: $maxVal, scale: $scale, scaleTarget $scaleTarget")
        }

//        println("nElem $nElem, t $t")
        for (i in 0..nElem - 1) {

            val c = if (i < nElemMax) (reclist[i] + reclist[i + 1]) / 2.0 else 0.0
            val r = if (i < nElemMax) abs(reclist[i] - reclist[i + 1]) / 2.0 else 0.0
            val reverse = if (i < nElemMax) reclist[i] > reclist[i+1] else false
            val upDown = i%2 == 1

//            println("$i, c: $c, r $r")
            if (i < nElem -1) {
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0, semicircleSteps, semicircleSteps, reverse, upDown)
                sc.draw(drawer)
            } else { // last semicircle: draw partially
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0, t, semicircleSteps,  reverse, upDown)
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
//                    val currRLmax = reclist.take(i).max() ?: 1      // only draw up to current max occurred value
                    val currRLmax = round( (size - 2*margin) / scale).toInt()
                    vibratingLine2(drawer, t, semicircleSteps, reclist[i], scale, currRLmax)
                }
            }
        }

        drawer.popTransforms()
//        Thread.sleep(20)
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
    val note = val2note(rli)
    val dx = rli * scale
    val dy = rli * scale
    val w = Math.max(scale/2, 3.0)  * (1-Math.pow(t/semicircleSteps.toDouble(), 2.0)) //* (1 + 0.2* Math.sin(t/2.5*Math.PI))
    var triple = Triple<Double, Double, Double>(dx, dy, w)
    return triple
}

fun dotParamsB(t: Int, semicircleSteps: Int, rli: Int, scale: Double) : Triple<Double, Double, Double> {
    // ccordinates move
    val note = val2note(rli)
    val dx = rli * scale - 10* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val dy = rli * scale + 10* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val w = Math.max(scale/2, 5.0)  * (1-Math.sqrt(t/semicircleSteps.toDouble())) //* (1 + 0.2* Math.sin(t/2.5*Math.PI))
    var triple = Triple<Double, Double, Double>(dx, dy, w)
    return triple
}


fun dotParams1(t: Int, semicircleSteps: Int, rli: Int, scale: Double) : Triple<Double, Double, Double> {
    val note = val2note(rli)
    val dx = rli * scale - Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val dy = rli * scale + Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val w = 0.0
    var triple = Triple<Double, Double, Double>(dx, dy, w)
    return triple
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
    val note = val2note(rli)
    val dx = rli * scale - Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val dy = rli * scale + Math.max(scale/2, 5.0)* Math.cos(t/3.0*Math.PI)* (1-Math.sqrt(t/semicircleSteps.toDouble()))
    val w = 0.0
    var triple = Triple<Double, Double, Double>(dx, dy, w)
    return triple
}

fun vibratingLine2(drawer: Drawer, t: Int, semicircleSteps: Int, rli: Int, scale: Double, currRLmax: Int) {
    val (dx, dy, w) = dotParams2(t, semicircleSteps, rli, scale)
    drawer.pushStyle()
    drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.2)
    for (i in 0..rli-1) {
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
    if (t < 0.5) {
        return 0.5 * Math.pow(2 * t, g1)
    } else {
        return 1 - 0.5 * Math.pow(2 * (1 - t), g2)
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