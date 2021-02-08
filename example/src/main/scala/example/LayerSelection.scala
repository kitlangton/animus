package example

import animus.{ObservableOps, SignalOps}
import com.raquo.laminar.api.L._
import example.Docs.flexCenter
import zio._
import zio.clock.Clock

import scala.scalajs.js.timers.setTimeout

case class LayerSelection(layers: List[VisualLayer[_, _]]) {

  val highlightedOutputs = Var(Set.empty[VisualLayer[_, _]])
  val highlightedInputs  = Var(Set.empty[VisualLayer[_, _]])
  val highlightedNames   = Var(Set.empty[VisualLayer[_, _]])

  val graphVar = Var(Map.empty[VisualLayer[_, _], Set[VisualLayer[_, _]]])
  val topLayer = Var(Option.empty[VisualLayer[_, _]])

  val queue: Queue[Int]              = zio.Runtime.default.unsafeRun(Queue.unbounded[Int])
  def advance: Unit                  = zio.Runtime.default.unsafeRunAsync_(queue.offer(1))
  val await: ZIO[Any, Nothing, Unit] = queue.take.unit

  def buildLayer(layer: VisualLayer[_, _]): ZIO[Clock, Nothing, Unit] =
    for {
      _ <- UIO(highlightedOutputs.update(_ + layer)) *> await
      _ <- UIO(highlightedInputs.update(_ + layer)) *> await
      next = layer.inputs
        .flatMap { input => layers.find(_.outputs.contains(input)) }
      _ <- ZIO.foreach(next) { child =>
        graphVar.update { graph =>
          val children = graph.getOrElse(layer, Set.empty)
          graph.updated(layer, children + child)
        }
        buildLayer(child).when(!highlightedNames.now().contains(child))
      }
      _ <- UIO(highlightedNames.update(_ + layer)) *> await
    } yield ()

  def renderChildren(layer: VisualLayer[_, _]): Div = {
    val isVisible = Var(false)
    val $children = graphVar.signal.map(_.getOrElse(layer, Set.empty).toList)
    div(
      fontFamily("'Source Code Pro', monospace"),
      div(
        flexCenter,
        flexDirection.column,
        div(
          layer.render.amend(
            inContext { el =>
              maxWidth <-- isVisible.signal
                .map { if (_) el.ref.firstElementChild.scrollWidth.toDouble + 12 else 0.0 }
                .spring
                .px
            }
          ),
          opacity <-- isVisible.signal.map {
            if (_) 1.0 else 0.0
          }.spring,
          overflow.hidden,
          inContext { el =>
            maxHeight <-- isVisible.signal.map { if (_) el.ref.scrollHeight.toDouble else 0.0 }.spring.px
          },
          onMountCallback { _ =>
            setTimeout(10) {
              isVisible.set(true)
            }
          }
        ),
        child.maybe <-- $children.map(c => Option.when(c.nonEmpty)(1)).split(identity) { (_, _, _) =>
          val isVisible = Var(false)
          div(
            ">>>",
            opacity <-- isVisible.signal.map {
              if (_) 0.4 else 0.0
            }.spring,
            overflow.hidden,
            onMountCallback { _ =>
              setTimeout(10) {
                isVisible.set(true)
              }
            },
            inContext { el =>
              maxHeight <-- isVisible.signal.map { if (_) el.ref.scrollHeight.toDouble else 0.0 }.spring.px
            }
          )
        },
        div(
          flexCenter,
          alignItems.flexStart,
          children <-- $children.split(identity) { (_, child, _) =>
            renderChildren(child)
          }
            .map { divs =>
              divs.headOption.toList ++ divs.tail.flatMap {
                List(
                  div(
                    fontFamily("'Source Code Pro', monospace"),
                    fontSize("20px"),
                    "++",
                    opacity(0.4),
                    paddingTop("10px")
                  ),
                  _
                )
              }
            }
        )
      )
    )
  }

  def graph: Div = div(
    flexCenter,
    child.maybe <-- topLayer.signal.split(identity) { (_, layer, _) =>
      renderChildren(layer)
    }
  )

  def allLayers: Div =
    div(
      table(
        windowEvents.onKeyDown.filter(_.key == "r") --> { _ => advance },
        thead(opacity(0.4), fontSize("14px"), th(""), th("NAME"), th(""), th("INPUT"), th(""), th("OUTPUT")),
        tbody(
          layers.map { layer =>
            layer
              .renderWithDependencies(
                highlightedNames.signal.map(_.contains(layer)),
                highlightedInputs.signal.map(_.contains(layer)),
                highlightedOutputs.signal.map(_.contains(layer))
              )
              .amend(
                onClick --> { _ =>
                  zio.Runtime.default.unsafeRunAsync_(buildLayer(layer))
                  topLayer.set(Some(layer))
                }
              )
          }
        )
      ),
      div(
        paddingTop("40px"),
        flexCenter,
        flexDirection.column,
        graph
      )
    )
}
