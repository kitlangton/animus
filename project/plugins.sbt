logLevel := Level.Warn

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.18.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("com.github.sbt"     % "sbt-ci-release"           % "1.9.3")
addSbtPlugin("com.github.sbt"     % "sbt-github-actions"       % "0.25.0")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"             % "0.13.0")
