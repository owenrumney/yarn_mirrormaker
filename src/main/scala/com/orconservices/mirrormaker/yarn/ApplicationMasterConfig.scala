package com.orconservices.mirrormaker.yarn

import java.util

import kafka.tools.MirrorMaker
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.util.ClassUtil
import org.apache.hadoop.yarn.api.records.LocalResource
import org.apache.hadoop.yarn.client.api.{AMRMClient, NMClient}
import org.apache.hadoop.yarn.conf.YarnConfiguration

object ApplicationMasterConfig {

  def apply(args: Array[String]): Config = {
    val conf: Configuration = new YarnConfiguration
    val rmClient = AMRMClient.createAMRMClient[AMRMClient.ContainerRequest]
    rmClient.init(conf)
    rmClient.start()

    val nmClient = NMClient.createNMClient
    nmClient.init(conf)
    nmClient.start()

    rmClient.registerApplicationMaster("", 0, "")

    val argString: String = args.mkString(" ")
    val cmd = String.format("java %s %s", MirrorMaker.getClass.getName, argString)

    val localResource = loadLocalResources(conf)
    val appEnv = ResourceLoader.createEnvironment(conf)
    Config(rmClient, nmClient, cmd, appEnv, localResource)
  }

  private def loadLocalResources(conf: Configuration): Map[String, LocalResource] = {
    val jar: String = ClassUtil.findContainingJar(classOf[MirrorMakerApplicationMaster])
    val src: Path = new Path(jar)
    val appMasterJar: LocalResource = ResourceLoader.createLocalResource(src, conf)

    ResourceLoader.createLocalResource(new Path("producer.properties"), conf)

    val localResource = Map(
      "AppMaster.jar" -> appMasterJar,
      "producer.properties" -> ResourceLoader.createLocalResource(new Path("producer.properties"), conf),
      "consumer.properties" -> ResourceLoader.createLocalResource(new Path("consumer.properties"), conf)
    )
    localResource
  }

  case class Config(rmClient: AMRMClient[AMRMClient.ContainerRequest], nmClient: NMClient, cmd: String, appEnv: util.HashMap[String, String], localResource: Map[String, LocalResource])

}
