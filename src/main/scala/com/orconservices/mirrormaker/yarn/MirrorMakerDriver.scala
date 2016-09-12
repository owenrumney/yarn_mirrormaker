package com.orconservices.mirrormaker.yarn

import java.io.{File, FileInputStream, FileNotFoundException}
import java.util.Properties

import org.apache.commons.cli.{BasicParser, HelpFormatter, Option, Options}
import org.slf4j.{Logger, LoggerFactory}

object MirrorMakerDriver {

  val ConfigFileArg: String = "config-path"
  val LOG: Logger = LoggerFactory.getLogger("MirrorMakerDriver")

  val OPTIONS = {
    val options = new Options()
    options.addOption(new Option(ConfigFileArg, true, "The supporting mirrormaker config file"))
    options
  }

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      val formatter = new HelpFormatter()
      formatter.printHelp("yarn jar.mirrormaker_yarnapplication-<version>.jar", OPTIONS)
      System.exit(1)
    }
    val cmd = new BasicParser().parse(OPTIONS, args)
    val configFile = new File(cmd.getOptionValue(ConfigFileArg))
    if (!configFile.exists()) {
      throw new FileNotFoundException("Config file not found")
    }
    LOG.info("Loading config file [{}]", configFile.getName)
    val properties = new Properties
    properties.load(new FileInputStream(configFile))
    new MirrorMakerYarnClient(properties).run()
  }

  def configureResourceSettings(props: Properties): Unit = {
    System.setProperty(YarnConstants.ApplicationMasterMBKey, props.getProperty(YarnConstants.ApplicationMasterMBKey, YarnConstants.ApplicationMasterMBDefault))
    System.setProperty(YarnConstants.ApplicationMasterCoresKey, props.getProperty(YarnConstants.ApplicationMasterCoresKey, YarnConstants.ApplicationMasterCoresDefault))
    System.setProperty(YarnConstants.ContainerMBKey, props.getProperty(YarnConstants.ContainerMBKey, YarnConstants.ContainerMBDefault))
    System.setProperty(YarnConstants.ContainerCoresKey, props.getProperty(YarnConstants.ContainerCoresKey, YarnConstants.ContainerCoresDefault))
    System.setProperty(YarnConstants.ContainerRetryCountKey, props.getProperty(YarnConstants.ContainerRetryCountKey, YarnConstants.ContainerRetryCountDefault))
  }
}
