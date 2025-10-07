import ProjectSetting._
import sbt._
import sbt.Keys._
import xerial.sbt.Sonatype.sonatypeCentralHost

val zioVersion             = "2.1.21"
val scala3_Version         = "3.6.3"
val scala2_13Version       = "2.13.16"
val scala2_12Version       = "2.12.20"
val zioConfigVersion       = "4.0.5"
val nebulaClientVersion    = "3.8.4"
val catsVersion            = "2.9.0"
val catsEffectVersion      = "3.6.3"
val fs2Version             = "3.12.2"
val scalaCollectionVersion = "2.14.0"

val logbackVersion              = "1.4.11"
val silencerVersion             = "1.4.2"
val testcontainersNebulaVersion = "0.2.1"

val supportCrossVersionList = Seq(scala3_Version, scala2_13Version, scala2_12Version)

inThisBuild(
  List(
    scalaVersion     := supportCrossVersionList(1),
    homepage         := Some(url("https://github.com/nebula-contrib/nebula4scala")),
    licenses         := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    organization     := "io.github.jxnu-liguobin",
    organizationName := "梦境迷离",
    developers       := List(
      Developer(
        id = "jxnu-liguobin",
        name = "梦境迷离",
        email = "dreamylost@outlook.com",
        url = url("https://github.com/jxnu-liguobin")
      )
    ),
    ThisBuild / sonatypeProfileName    := "io.github.jxnu-liguobin",
    ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val _zioTests = Seq(
  "dev.zio" %% "zio-test-magnolia" % zioVersion,
  "dev.zio" %% "zio-test"          % zioVersion,
  "dev.zio" %% "zio-test-sbt"      % zioVersion
)

lazy val `nebula4scala-core` = project
  .in(file("modules/nebula4scala-core"))
  .settings(
    name               := "nebula4scala-core",
    crossScalaVersions := supportCrossVersionList,
    libraryDependencies ++= Seq(
      "com.vesoft"              % "client"                  % nebulaClientVersion,
      "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionVersion
    ) ++ conditionalDependencies.value
  )
  .settings(ProjectSetting.value)
  .enablePlugins(ScalafmtPlugin)

lazy val `nebula4scala-cats-effect` = project
  .in(file("modules/nebula4scala-cats-effect"))
  .settings(
    name               := "nebula4scala-cats-effect",
    crossScalaVersions := supportCrossVersionList,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2"        %% "fs2-io"      % fs2Version
    )
  )
  .settings(ProjectSetting.value)
  .dependsOn(`nebula4scala-core`)
  .enablePlugins(ScalafmtPlugin)

lazy val `nebula4scala-zio` = project
  .in(file("modules/nebula4scala-zio"))
  .settings(
    name               := "nebula4scala-zio",
    crossScalaVersions := supportCrossVersionList,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
      "dev.zio" %% "zio"                 % zioVersion,
      // see https://github.com/zio/zio-config/issues/1245
      "com.github.ghik" %% "silencer-lib"    % silencerVersion % Provided cross CrossVersion.for3Use2_13,
      "ch.qos.logback"   % "logback-classic" % logbackVersion  % Test,
      "io.github.jxnu-liguobin" %% "testcontainers-nebula" % testcontainersNebulaVersion % Test
    ) ++ _zioTests.map(_ % Test),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(ProjectSetting.value)
  .dependsOn(`nebula4scala-core`)
  .enablePlugins(ScalafmtPlugin)

lazy val examples = project
  .in(file("examples"))
  .settings(
    crossScalaVersions := supportCrossVersionList,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion
    )
  )
  .settings(ProjectSetting.value)
  .settings(ProjectSetting.noPublish)
  .dependsOn(`nebula4scala-zio` % "compile->compile;test->test")
  .dependsOn(`nebula4scala-cats-effect` % "compile->compile;test->test")
  .enablePlugins(ScalafmtPlugin)

lazy val `nebula4scala` = project
  .in(file("."))
  .settings(
    crossScalaVersions := Nil,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies ++= _zioTests.map(_ % Test)
  )
  .settings(ProjectSetting.value)
  .settings(ProjectSetting.noPublish)
  .aggregate(
    `nebula4scala-zio`,
    `nebula4scala-core`,
    `nebula4scala-cats-effect`,
    examples
  )
  .enablePlugins(ScalafmtPlugin)
