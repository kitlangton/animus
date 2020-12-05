//package example
//
//import animus._
//import com.raquo.airstream.eventbus.EventBus
//import com.raquo.laminar.api.L._
//import org.scalajs.dom.{document, window}
//
//import scala.util.Random
//
//object OldApp {
//  def render: Unit = {
//    documentEvents.onDomContentLoaded.foreach { _ =>
//      val container = document.getElementById("app-container")
//
//      container.textContent = ""
//
//      val coordBus = new EventBus[(Double, Double)]
//      val mousePos = Var((0.0, 0.0))
//
//      val cx = window.innerWidth / 2
//      val cy = window.innerHeight / 2
//
//      def magicBall: SvgElement = {
//        def ri    = Random.nextDouble() * 300 - 150
//        val $left = coordBus.events.map(_._1 + ri).spring(cx)
//        val $top  = coordBus.events.map(_._2 + ri).spring(cy)
//
//        svg.circle(
//          svg.fill(s"rgb(${Random.nextDouble() * 255}, ${Random.nextDouble() * 255}, ${Random.nextDouble() * 255})"),
//          svg.cx <-- $left.map { s => s"${s}px" },
//          svg.cy <-- $top.map { s => s"${s}px" },
//          onMountBind { el =>
//            val $r = mousePos.signal.map { p =>
//              val rect = el.thisNode.ref.getBoundingClientRect()
//              if (Math.abs(p._1 - rect.left) < 4 || Math.abs(p._2 - rect.top) < 4)
//                30.0
//              else
//                3.0
//            }.spring
//            svg.r <-- $r.map(_.toString)
//          }
//        )
//      }
//
//      val appElement = div(
//        mousePos.signal.changes.debounce(100) --> { _ =>
//          mousePos.set((0, 0))
//        },
//        windowEvents.onMouseMove --> { e =>
//          mousePos.set((e.clientX, e.clientY))
//        },
//        position.fixed,
//        background("black"),
//        top("0"),
//        left("0"),
//        right("0"),
//        bottom("0"),
//        width("100vw"),
//        height("100vh"),
//        display.flex,
//        alignItems.center,
//        justifyContent.center,
//        color("white"),
//        position.absolute,
//        fontFamily("Courier New"),
//        fontSize("80px"),
//        fontWeight.bold,
//        "ANIMUS"
//          .split("")
//          .zipWithIndex
//          .map { case (str, idx) =>
//            def ri    = Random.nextDouble() * 10 - 5
//            val $left = coordBus.events.map(_._1 + ri + idx * 60).spring(cx)
//            val $top  = coordBus.events.map(_._2 + ri).spring(cy)
//            div(
//              str,
//              display.inlineFlex,
//              padding("0 10px"),
//              position.absolute,
//              left <-- $left.map { s => s"${s - 180}px" },
//              top <-- $top.map { s => s"${s - 55}px" },
//              onMountBind { el =>
//                val rect      = el.thisNode.ref.getBoundingClientRect()
//                val $hovering = mousePos.signal.map { case (x, _) => Math.abs(rect.left + (idx * 60) - x) < 60 }
//                transform <-- $hovering.map {
//                  if (_)
//                    (1.5, Random.nextDouble() * 30 - 15)
//                  else
//                    (1.0, Random.nextDouble() * 30 - 15)
//                }.spring
//                  .map { case (d, r) => s"scale($d) rotate(${r}deg)" }
//              }
//            )
//          }
//          .toList,
//        EventStream.periodic(100) --> { _ => coordBus.writer.onNext(cx, cy) },
//        div(
//          position.relative,
//          zIndex(10),
//          svg.svg(
//            svg.width(window.innerWidth.toString + "px"),
//            svg.height(window.innerHeight.toString + "px"),
//            List.fill(100)(magicBall)
//          )
//        )
//      )
//
//      render(container, appElement)
//    }(unsafeWindowOwner)
//  }
//}
