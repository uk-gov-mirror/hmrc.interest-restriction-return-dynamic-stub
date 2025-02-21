import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {
  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-27"  % "3.0.0",
    "com.github.fge"          %  "json-schema-validator"      % "2.2.6"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "3.0.0" % Test,
    "org.scalatest"           %% "scalatest"                % "3.1.2"                 % Test,
    "com.typesafe.play"       %% "play-test"                % current                 % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10"               % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"                 % Test,
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.22.0"                % Test
  )
}
