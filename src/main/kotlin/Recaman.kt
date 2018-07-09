// Visualisation of the Recaman Series https://oeis.org/A005132
// TODO: add animated dots when playing note / crossing 45deg line
// play piano note for new values
// use drawer.contour( Circle(Vector2(100.0, 100.0), 100.0).contour.sub(0.0, 0.5) on http://guide.openrndr.org/#/Tutorial_DrawingComplexShapes as it is much faster

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
import org.openrndr.shape.Circle
import org.openrndr.shape.Color


val size   = 1100
val margin =  100

val nElemMax = 200
val semicircleSteps = 20
val fixScale     = false
val drawNoteAtXY = true

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
                n <=  64 ->   5.732484076433121
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
    var scale = manualScale(0)
    lateinit var reclist : MutableList<Int>

    var nElem = 0
    var t     = semicircleSteps + 1

    override fun setup() {
//        super.backgroundColor = null
//        extend(ScreenRecorder())
        reclist = recaman(nElemMax)
        println("reclist: $reclist")
//        drawer.background(ColorRGBa.WHITE)
//        Thread.sleep(200)
    }

    override fun draw() {
        super.draw()

        drawer.pushTransforms()
        drawer.translate(margin.toDouble(), size - margin.toDouble())
        drawer.scale(1.0, -1.0)

        drawer.background(ColorRGBa.WHITE)
        drawer.fill   = ColorRGBa.RED
        drawer.stroke = ColorRGBa.RED

        // TODO: note at 0 misses now ...

        // Progress through nElem and t
        if (t < semicircleSteps-1) {
//            if (t == semicircleSteps - 3) {
//                val note = 20 + reclist[nElem] % 88
//                println("nElem: $nElem Seq value ${reclist[nElem]} - going to play $note ...")
//                Player().delayPlay(0, note.toString())
//            }
            t += 1
        } else {
            if (nElem < nElemMax) nElem += 1
            val note = 20 + reclist[nElem-1] % 88
//            println("nElem: $nElem Seq value ${reclist[nElem-1]} - going to play $note ...")
            Player().delayPlay(0, note.toString())
//            println("nElem: $nElem Seq value ${reclist[nElem-1]} - going to play $note ...")
            t = 0
        }

        if (fixScale) {
            val maxVal = reclist.max() ?: 1
            scale = (size - 2 * margin) / maxVal.toDouble()
//            println("nElem: $nElem, maxVal: $maxVal, scale: $scale")
        } else {
            val nElemPlan = Math.min(nElem + 5, nElemMax)
            val maxVal = reclist.take(nElemPlan).max() ?: 1
//            val scaleTarget = (size - 2 * margin) / maxVal.toDouble()
            val scaleTarget = manualScale(nElem)
            // TODO: use easing function?
            scale = scaleTarget - (scaleTarget-scale) * 0.97

//            if (t == 0) println("t: $t, nElem: $nElem, maxVal: $maxVal, scale: $scale, scaleTarget $scaleTarget")
        }

//        println("nElem $nElem, t $t")
        for (i in 0..nElem - 1) {
//            val i = nElem-1
            val c = (reclist[i] + reclist[i + 1]) / 2.0
            val r = abs(reclist[i] - reclist[i + 1]) / 2.0
            val reverse = reclist[i] > reclist[i+1]
            val upDown = i%2 == 1

//            println("$i, c: $c, r $r")
            if (i < nElem -1) {
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0, semicircleSteps, semicircleSteps, reverse, upDown)
                sc.draw(drawer)
            } else { // last semicircle: draw partially
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0, t, semicircleSteps,  reverse, upDown)
                sc.draw(drawer)

                // Draw note event at xy-axis
                if (drawNoteAtXY) {
                    drawer.pushStyle()
                    drawer.stroke = null
                    // TODO: grow a bit in beginning?
                    // TODO: wobble with frequency?
                    // TODO: use animation instead?
                    val weight = 1 - t / semicircleSteps.toDouble()
                    drawer.fill = ColorRGBa(1.0, 0.0, 0.0, weight)
                    drawer.circle(Vector2(reclist[i] * scale, reclist[i] * scale), 10.0 * weight)
                    drawer.popStyle()
                }
            }
        }

        drawer.popTransforms()
//        Thread.sleep(200)
    }
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