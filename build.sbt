import sbtcrossproject.CrossPlugin.autoImport.crossProject

inThisBuild(
  Seq(
    name := "animus",
    normalizedName := "animus",
    organization := "com.kitlangton",
    scalaVersion := "2.13.4",
    crossScalaVersions := Seq("2.13.4")
  )
)

releaseCrossBuild := true

lazy val releaseSettings = Seq(
  homepage := Some(url("https://github.com/kitlangton/animus")),
  licenses += ("MIT", url("https://github.com/kitlangton/animus/blob/master/LICENSE.md")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/kitlangton/animus"),
      "scm:git@github.com/kitlangton/animus.git"
    )
  ),
  developers := List(
    Developer(
      id = "kitlangton",
      name = "Kit Langton",
      email = "kit.langton@gmail.com",
      url = url("https://kitlangton.com")
    )
  ),
  sonatypeProfileName := "com.kitlangton",
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := sonatypePublishTo.value,
  releaseCrossBuild := true,
  pomIncludeRepository := { _ => false },
  useGpg := false,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value
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

lazy val commonSettings = releaseSettings ++ scalacSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.1.1" % Test
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(animusJS, animusJVM)
  .settings(commonSettings)
  .settings(
    skip in publish := true
  )

lazy val example = project
  .in(file("example"))
  .dependsOn(animusJS)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    skip in publish := true,
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"      % "1.0.4-2",
      "dev.zio" %%% "zio-json" % "0.1"
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val animus = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(commonSettings)
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    libraryDependencies ++= Seq(
      "com.raquo"           %%% "laminar"       % "0.11.0",
      "com.raquo"           %%% "airstream"     % "0.11.1",
      "com.propensive"      %%% "magnolia"      % "0.17.0",
      scalaOrganization.value % "scala-reflect" % scalaVersion.value
    )
  )

lazy val animusJS  = animus.js
lazy val animusJVM = animus.jvm
