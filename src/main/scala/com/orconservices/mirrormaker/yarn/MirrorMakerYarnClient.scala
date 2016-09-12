package com.orconservices.mirrormaker.yarn

import java.util
import java.util.Properties

import com.google.common.collect.Maps
import org.apache.hadoop.yarn.api.ApplicationConstants
import org.apache.hadoop.yarn.api.records.{ContainerLaunchContext, LocalResource}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Records
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.{Map, mutable}

class MirrorMakerYarnClient(val properties: Properties) {


  private val LOG = LoggerFactory.getLogger("mirrormaker.yarn.application.MirrorMakerYarnClient")
  private val JOB_WHITELIST_PROPERTY: String = "job.whitelist"
  private val JOB_TOPICMUTATOR_CLASS_PROPERTY: String = "job.topicmutator.class"
  private val JOB_TOPICMUTATOR_ARGS_PROPERTY: String = "job.topicmutator.args"
  private val JOB_NAME_PROPERTY: String = "job.name"
  private val localResources: Map[String, LocalResource] = Maps.newHashMap[String, LocalResource]

  def run(): Unit = {
    LOG.info("Starting the yarn client")
    verifyProperties(JOB_NAME_PROPERTY, JOB_WHITELIST_PROPERTY)
    val conf = new YarnConfiguration


    val container: ContainerLaunchContext = Records.newRecord(classOf[ContainerLaunchContext])
    container.setLocalResources(mapAsJavaMap[String, LocalResource](localResources))
    container.setEnvironment(ResourceLoader.createEnvironment(conf))
    container.setCommands(MirrorMakerCommandLineBuilder(properties))
  }

  private def verifyProperties(requiredProperties: String*) {
    for (propertyName <- requiredProperties) {
      val property: String = properties.getProperty(propertyName)
      assert(!property.isEmpty)
    }
  }

  object MirrorMakerCommandLineBuilder {
    def apply(properties: Properties): mutable.Buffer[String] = {
      val commandBuilder: util.ArrayList[String] = new util.ArrayList[String]
      val xmx: Int = System.getProperty(YarnConstants.ApplicationMasterMBKey, YarnConstants.ApplicationMasterMBDefault).toInt
      commandBuilder.add(String.format("java -Xmx%sM", xmx.toString))
      commandBuilder.add(String.format("-D%s=%s", YarnConstants.ContainerMBKey, System.getProperty(YarnConstants.ContainerMBKey)))
      commandBuilder.add(String.format("-D%s=%s", YarnConstants.ContainerCoresKey, System.getProperty(YarnConstants.ContainerCoresKey)))
      commandBuilder.add(String.format("-D%s=%s", YarnConstants.ContainerRetryCountKey, System.getProperty(YarnConstants.ContainerRetryCountKey)))
      commandBuilder.add(classOf[MirrorMakerApplicationMaster].getName)
      commandBuilder.add("--producer.config producer.properties")
      commandBuilder.add("--consumer.config consumer.properties")
      commandBuilder.add("--whitelist")
      commandBuilder.add(properties.getProperty(JOB_WHITELIST_PROPERTY))

      val topicMutator: String = properties.getProperty(JOB_TOPICMUTATOR_CLASS_PROPERTY)
      val topicMutatorArgs: String = properties.getProperty(JOB_TOPICMUTATOR_ARGS_PROPERTY)

      if (!topicMutator.isEmpty) commandBuilder.add(String.format("--message.handler %s --message.handler.args %s", topicMutator, topicMutatorArgs))

      val jobConfig: Properties = PropertyFileExtractor(properties, "job.config.")
      import scala.collection.JavaConversions._
      for (configEntry <- jobConfig.entrySet) {
        if (!configEntry.getValue.toString.isEmpty) commandBuilder.add(String.format("--%s %s", configEntry.getKey, configEntry.getValue))
      }

      commandBuilder.add(String.format("1>%s/stdout 2>%s/stderr", ApplicationConstants.LOG_DIR_EXPANSION_VAR, ApplicationConstants.LOG_DIR_EXPANSION_VAR))
      commandBuilder
    }.asScala
  }

}
