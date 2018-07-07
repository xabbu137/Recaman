import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.abs
import kotlin.math.max

val size   = 1100
val margin =  100

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

    val nElem = 100
    var scale = 1.0
    lateinit var reclist : MutableList<Int>

    override fun setup() {
        reclist = recaman(nElem)
        val maxVal = reclist.max() ?: 1
        scale = (size - 2*margin)/maxVal.toDouble()
        println("reclist: $reclist")
        println("scale: $scale")

    }

    override fun draw() {
        drawer.pushTransforms()
        drawer.translate(margin.toDouble(),size - margin.toDouble())
        drawer.scale(1.0, -1.0)

        drawer.background(ColorRGBa.WHITE)
        drawer.fill = null
        drawer.stroke = ColorRGBa.RED

        for (i in 0..nElem-1) {
            val c = (reclist[i] + reclist[i+1])/2.0
            val r = abs(reclist[i] - reclist[i+1]) /2.0
//            println("$i, c: $c, r $r")
            if (i % 2 == 1) {
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, -135.0)
                sc.draw(drawer)
            } else {
                val sc = SemiCircle(Vector2(c * scale, c * scale), r * scale * Math.sqrt(2.0), 45.0, 225.0)
                sc.draw(drawer)
            }
        }

        drawer.popTransforms()
    }
}

fun main(args: Array<String>) {
    application(Recaman(),
            configuration {
                width  = size
                height = size
            })
}