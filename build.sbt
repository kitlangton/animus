import sbtcrossproject.CrossPlugin.autoImport.crossProject

inThisBuild(
  List(
    name               := "animus",
    normalizedName     := "animus",
    organization       := "com.kitlangton",
    scalaVersion       := "3.3.3",
    crossScalaVersions := Seq("3.3.3"),
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

lazy val scalacSettings =
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

val zioVersion     = "2.0.21"
val laminarVersion = "16.0.0"

lazy val commonSettings = Seq(
  scalacOptions ++= scalacSettings
)

lazy val root = project
  .in(file("."))
  .aggregate(animusJS, animusJVM)
  .settings(commonSettings)
  .settings(
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    skip / publish := true
  )

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val animus = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    commonSettings,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies += "dev.zio" %%% "zio-test" % zioVersion % Test
  )
  .jsSettings(
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    libraryDependencies ++= Seq("com.raquo" %%% "laminar" % laminarVersion)
  )

lazy val animusJS  = animus.js
lazy val animusJVM = animus.jvm

lazy val example = project
  .in(file("example"))
  .dependsOn(animusJS)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    skip / publish := true,
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"      % zioVersion,
      "dev.zio" %%% "zio-json" % "0.6.2"
    )
  )
  .enablePlugins(ScalaJSPlugin)
