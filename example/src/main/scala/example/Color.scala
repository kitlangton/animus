package example

import animus.{Animatable, DeriveAnimatable}

import scala.util.Random

case class Color(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 1.0) {

  def blend(that: Color, percent: Double): Color = {
    val inverted = 1.0 - percent

    Color(
      (this.red * inverted) + (that.red * percent),
      (this.green * inverted) + (that.green * percent),
      (this.blue * inverted) + (that.blue * percent),
      (this.alpha * inverted) + (that.alpha * percent)
    )
  }

  def average(that: Color): Color =
    Color(
      this.red + that.red / 2.0,
      this.green + that.green / 2.0,
      this.blue + that.blue / 2.0,
      this.alpha + that.alpha / 2.0
    )

  def toCss: String       = s"rgba($red, $green, $blue, $alpha)"
  def toHexString: String = s"#${Color.toHexValue(red)}${Color.toHexValue(green)}${Color.toHexValue(blue)}"
}

object Color {
  val red: Color    = Color(red = 255.0)
  val yellow: Color = Color(red = 255.0, green = 255.0)
  val green: Color  = Color(green = 255.0)
  val cyan: Color   = Color(green = 255.0, blue = 255.0)
  val blue: Color   = Color(blue = 255.0)
  val purple: Color = Color(red = 128.0, blue = 128.0)
  val black: Color  = Color()
  val white: Color  = Color(red = 255.0, green = 255.0, blue = 255.0)

  private def toHexValue(double: Double): String =
    double.toInt.toHexString.reverse.padTo(2, '0').reverse

  def fromHexValue(string0: String): Color = {
    val string = if (string0.startsWith("#")) string0.drop(1) else string0

    val Seq(r, g, b) = string
      .grouped(if (string.length == 6) 2 else 1)
      .map { s0 =>
        val s = if (s0.length == 1) s0 + s0 else s0
        Integer.parseInt(s, 16);
      }
      .toSeq

    Color(r, g, b)
  }

  def random: Color = Color(Random.nextDouble() * 255.0, Random.nextDouble() * 255.0, Random.nextDouble() * 255.0)

  implicit val animatable: Animatable[Color] = DeriveAnimatable.gen[Color]
}
