import org.scalajs.linker.interface.ModuleSplitStyle
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val scala3 = "3.3.5"

inThisBuild(
  List(
    name               := "animus",
    normalizedName     := "animus",
    organization       := "com.kitlangton",
    scalaVersion       := scala3,
    crossScalaVersions := Seq(scala3),
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

////////////////////////
// sbt-github-actions //
////////////////////////
ThisBuild / githubWorkflowJavaVersions += JavaSpec.temurin("17")

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("v")),
    RefPredicate.Equals(Ref.Branch("main"))
  )

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

/////////////////////////
// Project Definitions //
/////////////////////////

lazy val scalacSettings =
  "-unchecked" ::
    "-deprecation" ::
    "-feature" ::
    Nil

val zioVersion     = "2.1.15"
val laminarVersion = "17.2.0"

lazy val commonSettings = Seq(
  scalacOptions ++= scalacSettings
)

lazy val root = project
  .in(file("."))
  .aggregate(
    animusJS,
    example
//    animusJVM
  )
  .settings(commonSettings)
  .settings(
    skip / publish := true
  )

lazy val animus = crossProject(JSPlatform)
  .in(file("modules/core"))
  .settings(
    name := "animus",
    commonSettings,
    libraryDependencies ++= Seq(
      "dev.zio"              %%% "zio-test"  % zioVersion % Test,
      "io.github.kitlangton" %%% "quotidian" % "0.0.18"
    )
  )
  .jsSettings(
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("animus")))
    },
    libraryDependencies ++= Seq("com.raquo" %%% "laminar" % laminarVersion)
  )

lazy val animusJS = animus.js
//lazy val animusJVM = animus.jvm

lazy val example = project
  .in(file("example"))
  .dependsOn(animusJS)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("example")))
    },
    skip / publish := true,
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"      % zioVersion,
      "dev.zio" %%% "zio-json" % "0.7.32"
    )
  )
  .enablePlugins(ScalaJSPlugin)
