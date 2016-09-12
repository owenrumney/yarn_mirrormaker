package com.orconservices.mirrormaker.yarn

import java.io.File
import java.util

import com.google.common.collect.Maps
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.hadoop.yarn.api.ApplicationConstants
import org.apache.hadoop.yarn.api.records.{LocalResource, LocalResourceType, LocalResourceVisibility}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.{Apps, ConverterUtils, Records}
import org.slf4j.LoggerFactory

object ResourceLoader {

  private val LOG = LoggerFactory.getLogger("ResourceLoader")

  def createLocalResource(src: Path, conf: Configuration): LocalResource = {
    val fs: FileSystem = FileSystem.get(conf)
    val dest: Path = new Path(fs.getHomeDirectory, src.getName)
    LOG.info("Copying source from {} to {}", src.getName, dest.getName: Any)
    fs.copyFromLocalFile(src, dest)
    val sourceStat: FileStatus = FileSystem.get(conf).getFileStatus(dest)
    val localResource: LocalResource = Records.newRecord(classOf[LocalResource])
    localResource.setResource(ConverterUtils.getYarnUrlFromPath(dest))
    localResource.setSize(sourceStat.getLen)
    localResource.setTimestamp(sourceStat.getModificationTime)
    localResource.setType(LocalResourceType.FILE)
    localResource.setVisibility(LocalResourceVisibility.APPLICATION)
    localResource
  }

  def createEnvironment(conf: Configuration): util.HashMap[String, String] = {
    val appEnv: util.HashMap[String, String] = Maps.newHashMap[String, String]

    conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH: _*).foreach(c => {
      Apps addToEnvironment(appEnv, ApplicationConstants.Environment.CLASSPATH.name(), c.trim, File.pathSeparator)
    })

    Apps.addToEnvironment(appEnv, ApplicationConstants.Environment.CLASSPATH.name, ApplicationConstants.Environment.PWD.$ + File.separator + "*", File.pathSeparator)
    appEnv
  }
}
