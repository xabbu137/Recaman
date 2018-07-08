// Visualisation of the Recaman Series https://oeis.org/A005132
// TODO: would be cool to have sound with that ... true piano sounds that is :-)

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


val size   = 1100
val margin =  100

val semicircleSteps = 20

val fixScale = false
val player = Player()

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
    val nElemMax = 200
    var scale = 69.0
    lateinit var reclist : MutableList<Int>

    var nElem = 2
    var t     = 0

    override fun setup() {
//        super.backgroundColor = null
//        extend(ScreenRecorder())
        reclist = recaman(nElemMax)
        println("reclist: $reclist")
//        drawer.background(ColorRGBa.WHITE)
    }

    override fun draw() {
        super.draw()

        // Progress through nElem and t
        if (t < semicircleSteps-1) {
            t += 1
        } else {
            if (nElem < nElemMax) nElem += 1
            val note = 20 + reclist[nElem-1] % 88  // -1 as it is before the drawing
            println("Seq value ${reclist[nElem-1]} - going to play $note ...")
            Player().delayPlay(0, note.toString())
            t = 0
        }

        if (fixScale) {
            val maxVal = reclist.max() ?: 1
            scale = (size - 2 * margin) / maxVal.toDouble()
//            println("nElem: $nElem, maxVal: $maxVal, scale: $scale")
        } else {
            // TODO: still requires correct scaling - not using the real size currently somehow ...
            // TODO: ideally the scaling should also change smoothly
            // TODO: best might be some selected manual scale values ...
            val nElemPlan = Math.min(nElem + 5, nElemMax)
            val maxVal = reclist.take(nElemPlan).max() ?: 1
            val scaleTarget = (size - 2 * margin) / maxVal.toDouble()
            scale = scaleTarget - (scaleTarget-scale) * 0.95
//            println("nElem: $nElem, maxVal: $maxVal, scale: $scale, scaleTarget $scaleTarget")
        }

        drawer.pushTransforms()
        drawer.translate(margin.toDouble(), size - margin.toDouble())
        drawer.scale(0.99, -1.0)
//        if (nElem > 10) {
//            drawer.scale(0.5, -0.5)
//        }

        drawer.background(ColorRGBa.WHITE)
        drawer.fill = null
        drawer.stroke = ColorRGBa.RED

//        println("nElem $nElem, t $t")
        for (i in 0..nElem - 1) {
//            val i = nElem-1
            val c = (reclist[i] + reclist[i + 1]) / 2.0
            val r = abs(reclist[i] - reclist[i + 1]) / 2.0
            val reverse = reclist[i] < reclist[i+1]
//            println("$i, c: $c, r $r")
            if (i % 2 == 1) {
                if (i < nElem -1) {
                    val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0, semicircleSteps, semicircleSteps, reverse)
                    sc.draw(drawer)
                } else { // last semicircle: draw partially
                    val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0, t, semicircleSteps,  reverse)
                    sc.draw(drawer)
                }
            } else {
                if (i < nElem -1) {
                    val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, 225.0, semicircleSteps, semicircleSteps, reverse)
                    sc.draw(drawer)
                } else { // last semicircle: draw partially
                    val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, 225.0, t, semicircleSteps, reverse)
                    sc.draw(drawer)
                }
            }
        }

        drawer.popTransforms()
//        Thread.sleep(50)
    }
}

fun main(args: Array<String>) {

    // Test JFugue

//    player.play("C D E F G A B")
//    player.play("V0 I[Piano] Eq Ch. | Eq Ch. | Dq Eq Dq Cq   V1 I[Flute] Rw | Rw | GmajQQQ CmajQ");
//    val p1 = Pattern("V0 I[Piano] Eq Ch. | Eq Ch. | Dq Eq Dq Cq")
//    val p2 = Pattern("V1 I[Flute] Rw     | Rw     | GmajQQQ  CmajQ")
//    player.play(p1, p2)
//    player.play("1")

//    // Code using midi
//    val midiSynth = MidiSystem.getSynthesizer()
//    midiSynth.open()
//
//    midiSynth.loadAllInstruments(midiSynth.defaultSoundbank)
//    val instr = midiSynth.defaultSoundbank.instruments
//    val mChannels = midiSynth.channels
//    mChannels[0].programChange(14)  // list of instruments eg https://oeis.org/play?seq=A005132 (with 1 offset)
//    mChannels[0].noteOn(64, 100)//On channel 0, play note number 60 with velocity 100
//    try {
//        Thread.sleep(1000) // wait time in milliseconds to control duration
//    } catch (e: InterruptedException) {
//    }
//    mChannels[0].noteOff(64)//turn of the note

//    exitProcess(0)
    application(Recaman(),
            configuration {
                width  = size
                height = size
//                unfocusBehaviour = UnfocusBehaviour.THROTTLE
            })
}