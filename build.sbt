name := """rapid-development"""

version := "1.0-SNAPSHOT"
scalaVersion := "2.11.11"

packageName in Universal := "rapid-development"
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings {
    javaOptions in Universal +=  "-Dpidfile.path=/dev/null"
  }

libraryDependencies ++= Seq(
  cache,//?
  ws,
  "com.github.pureconfig" %% "pureconfig" % "0.7.0",
  "org.typelevel" %% "cats" % "0.9.0",
  "io.monix" %% "monix" % "2.3.0",
  "io.monix" %% "monix-cats" % "2.3.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

scalacOptions += "-feature"

resourceGenerators in Compile += (resourceManaged in Compile) map { dir =>
  def Try(command: String) = try { command.!! } catch { case e: Exception => command + " failed: " + e.getMessage }
  try {
    val targetDir = dir.getAbsolutePath + (if (dir.getAbsolutePath.endsWith("/")) "" else "/")
    val targetFile = new File(targetDir + "build-info.conf")
    val content = Seq(
      ("hostname", Try("hostname")),
      ("user", Try("id -u -n")),
      ("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US).format(new java.util.Date())),
      ("gitBranch", Try("git rev-parse --abbrev-ref HEAD")),
      ("gitCommit", Try("git rev-parse HEAD")),
      ("projectName", sys.env.getOrElse("CI_PROJECT_NAME", default = "")),
      ("pipelineId", sys.env.getOrElse("CI_PIPELINE_ID", default = "-1")),
      ("ciInstigator", sys.env.getOrElse("GITLAB_USER_ID", default = ""))
    ) map {case (nm, value) => "%s=\"%s\"".format(nm.trim, value.trim) }
    IO.write(targetFile, "build {\n" + content.mkString("  ", "\n  ", "\n") + "}\n")
    Seq(targetFile)
  } catch {
    case e: Throwable =>
      println("An error occurred in Build.scala while trying to generate build-info.conf")
      throw e // need this otherwise because no valid return value
  }
}