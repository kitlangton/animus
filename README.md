# Animus

[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]
[![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots]

An FRP animation library for Laminar

```sbt
// build.sbt
libraryDependencies += "io.github.kitlangton" %%% "animus" % "0.3.6"
```

[Silly Demos](https://animus-examples.surge.sh)

## Example

```scala
import animus._

val $left: Signal[Double] = EventStream.periodic(1000).toSignal(0).mapToValue(Random.nextDouble() * 1000)

val animatedBox =
  div(
    width("100px"),
    height("100px"),
    position.relative,
    left <-- $left.spring.px
  )
```

[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/io.github.kitlangton/animus_2.13.svg "Sonatype Releases"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.kitlangton/animus_2.13.svg "Sonatype Snapshots"
[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/io/github/kitlangton/animus_2.13/ "Sonatype Snapshots"
[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/io/github/kitlangton/animus_2.13/ "Sonatype Releases"
