import sbtcrossproject.CrossPlugin.autoImport.crossProject

inThisBuild(
  List(
    name               := "animus",
    normalizedName     := "animus",
    organization       := "com.kitlangton",
    scalaVersion       := "2.13.8",
    crossScalaVersions := Seq("2.13.8", "3.1.2"),
    organization       := "io.github.kitlangton",
    homepage           := Some(url("https://github.com/kitlangton/animus")),
    licenses           := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "kitlangton",
        "Kit Langton",
        "kit.langton@gmail.com",
        url("https://github.com/kitlangton")
      )
    )
  )
)

val baseScalacSettings =
  "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-deprecation" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:_" ::
    "-Xfuture" ::
    "-Xlint" ::
    "-Ymacro-annotations" ::
    "-Yno-adapted-args" ::
    "-Ywarn-value-discard" ::
    "-Ywarn-unused" ::
    Nil

lazy val scalacSettings = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) =>
        baseScalacSettings.diff(
          "-Xfuture" ::
            "-Yno-adapted-args" ::
            "-Ywarn-infer-any" ::
            "-Ywarn-nullary-override" ::
            "-Ywarn-nullary-unit" ::
            Nil
        )
      case _ => baseScalacSettings
    }
  }
)

lazy val commonSettings = scalacSettings

lazy val root = project
  .in(file("."))
  .aggregate(animusJS, animusJVM)
  .settings(commonSettings)
  .settings(
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    skip / publish := true
  )

lazy val example = project
  .in(file("example"))
  .dependsOn(animusJS)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    skip / publish := true,
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"      % "2.0.2",
      "dev.zio" %%% "zio-json" % "0.3.0"
    )
  )
  .enablePlugins(ScalaJSPlugin)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val animus = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    commonSettings,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies += "dev.zio" %%% "zio-test" % "2.0.4" % Test
  )
  .jsSettings(
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.14.2"
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) =>
          Seq("com.softwaremill.magnolia1_3" %%% "magnolia" % "1.1.5")
        case _ =>
          Seq(
            "com.propensive"      %%% "magnolia"      % "0.17.0",
            scalaOrganization.value % "scala-reflect" % scalaVersion.value
          )
      }
    }
  )

lazy val animusJS  = animus.js
lazy val animusJVM = animus.jvm
