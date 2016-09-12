name := "yarn_mirrormaker"

version := "1.0"

scalaVersion := "2.11.8"

val hadoopVer: String = "2.7.2"

val kafkaVersion: String = "0.9.0.0"

libraryDependencies ++= Seq(
  "commons-cli" % "commons-cli" % "1.2",
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.slf4j" % "slf4j-parent" % "1.7.21",
  "org.apache.hadoop" % "hadoop-yarn-api" % hadoopVer % "provided",
  "org.apache.hadoop" % "hadoop-yarn-client" % hadoopVer % "provided",
  "org.apache.hadoop" % "hadoop-common" % hadoopVer % "provided",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

