import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.Import.Assets
import com.typesafe.sbt.web.SbtWeb
import play.Play.autoImport._
import sbt.Keys._
import sbt._

object ApplicationBuild extends Build {

  val appName = "metafsm-atm"
  val appVersion = "1.0"

  lazy val orgId = "net.imadz"

  lazy val atm = Project(
    id = "atm",
    base = file("atm"),
    settings = Project.defaultSettings ++ Seq(
      organization := orgId,
      version := "0.1.0",
      scalaVersion := "2.10.4",
      compile in Compile <<= compile in Compile dependsOn clean
    )
  )

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "org.specs2" %% "specs2" % "3.3.1" % "test",
    "commons-codec" % "commons-codec" % "1.10",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.11",
    orgId %% "atm" % "0.1.0"
  ) 
  
  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).enablePlugins(SbtWeb).settings(
    version:= appVersion,
    scalaVersion := "2.10.4",
    resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
    includeFilter in (Assets, LessKeys.less) := "*.less",
      // for minified *.min.css files
    LessKeys.compress := false,
    libraryDependencies ++= appDependencies
  ).dependsOn(atm)

}
