import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile

import sbt._
import sbt.Keys._

object ProjectSetting {

  val pureconfigVersion = "0.17.11"

  def isScala3(scalaVersion: String): Boolean = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((3, _)) => true
      case _            => false
    }
  }

  val conditionalDependencies = Def.setting {
    if (isScala3(scalaVersion.value)) {
      Seq(
        "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureconfigVersion
      )
    } else
      Seq(
        "com.github.pureconfig" %% "pureconfig-generic" % pureconfigVersion
      )
  }

  val conditionalScalacOptions = Def.setting {
    if (isScala3(scalaVersion.value)) {
      Seq(
        "-Yretain-trees",
        "-Xmax-inlines:512"
      )
    } else
      Seq(
      )
  }

  val value: Seq[Def.Setting[?]] = Seq(
    scalacOptions ++= Seq(
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation"
    ) ++ conditionalScalacOptions.value,
    autoAPIMappings          := true,
    version                  := (ThisBuild / version).value,
    Test / parallelExecution := false, // see https://www.scalatest.org/user_guide/async_testing
    Global / cancelable      := true,
    exportJars               := true,
    scalafmtOnCompile        := true
  )

  val noPublish: Seq[Def.Setting[?]] = Seq(
    publish / skip := true
  )

}
