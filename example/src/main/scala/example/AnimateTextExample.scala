//package example
//
//import animus.DeriveAnimatable.Typeclass
//import animus._
//import com.raquo.laminar.api.L._
//import example.components.FadeInWords
//
//final case class MyDouble(d1: Double, d2: Double)
//
//object MyDouble {
//  implicit val animatable: Animatable[MyDouble] = DeriveAnimatable.gen[MyDouble]
//}
//
//case class AnimateTextExample() extends Component {
//  val textVar                                  = Var("")
//  val $characters: Signal[List[(String, Int)]] = textVar.signal.map(_.split("").zipWithIndex.toList)
//  val $isEmpty: Signal[Boolean]                = textVar.signal.map(_.isEmpty)
//
//  val keyEvents = windowEvents(_.onKeyDown).map(_.key) --> {
//    case "Backspace"       => textVar.update(_.dropRight(1))
//    case " "               => textVar.update(_ + "•")
//    case "Enter"           => textVar.update(_ + "¶")
//    case x if x.length > 1 => ()
//    case x                 => textVar.update(_ + x.toUpperCase)
//    case _                 => ()
//  }
//
//  def body: Div =
//    div(
//      keyEvents,
//      div(
//        fontWeight.bold,
//        div(
//          opacity <-- $isEmpty.map {
//            if (_) 1.0 else 0.6
//          }.spring,
//          transformOrigin("bottom left"),
//          transform <-- $isEmpty.map {
//            if (_) 1.0 else 0.6
//          }.spring.map(s => s"scale($s)"),
//          FadeInWords("TYPE SOMETHING.", 3500)
//        ),
//        children <-- $characters.splitTransition(identity) { case (_, (char, _), _, transition) =>
//          div(
//            char,
//            Option.when(Set("•", "¶")(char))(opacity(0.4)),
//            display.inlineFlex,
//            color("orange"),
//            transition.width,
//            transition.height
//          )
//        }
//      ),
//      codeExample
//    )
//
//  def codeExample: Div = {
//    val showSource = Var(false)
//    div(
//      marginTop("12px"),
//      div(
//        Transitions.height(showSource.signal),
//        Transitions.opacity(showSource.signal),
//        codeBlock(
//          """
//def animatedText: Div = {
//  val $characters: Signal[List[(String, Int)]] =
//    textVar.signal.map(_.split("").zipWithIndex.toList)
//
//  div(
//    children <-- $characters.splitTransition(identity) {
//      case (_, (character, _), _, transition) =>
//        div(
//          character,
//          display.inlineFlex,
//          color("orange"),
//          transition.width,
//          transition.height
//        )
//      }
//  )
//}
//            """.trim
//        ),
//        div(height("12px"))
//      ),
//      div(
//        Transitions.height($isEmpty.map(!_)),
//        Transitions.opacity($isEmpty.map(!_)),
//        cursor.pointer,
//        opacity(0.8),
//        fontSize("16px"),
//        code("SOURCE 👀"),
//        onClick --> { _ => showSource.update(!_) }
//      )
//    )
//  }
//}
