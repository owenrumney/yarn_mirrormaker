package com.orconservices.mirrormaker.yarn

import java.util.Collections
import java.util.concurrent.TimeUnit

import com.orconservices.mirrormaker.yarn.ApplicationMasterConfig.Config
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse
import org.apache.hadoop.yarn.api.records._
import org.apache.hadoop.yarn.client.api.AMRMClient
import org.apache.hadoop.yarn.util.Records
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


class MirrorMakerApplicationMaster {

  private val LOG = LoggerFactory.getLogger(classOf[MirrorMakerApplicationMaster])

  def main(args: Array[String]) {
    val amConf = ApplicationMasterConfig(args)
    val timesToRetry: Int = System.getProperty(YarnConstants.ContainerRetryCountKey, YarnConstants.ContainerRetryCountDefault).toInt
    val attempt: Int = 0
    var completedSuccessfully: Boolean = false
    while (attempt < timesToRetry) {
      addContainerAttempt(attempt, amConf.rmClient)
      if (startAndWaitForCompletion(attempt + 1, amConf)) {
        completedSuccessfully = true
      }
      LOG.info("Container failed on attempt {} of {}", attempt, timesToRetry)
    }
    LOG.debug("unregister")
    if (completedSuccessfully) amConf.rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "")
    else amConf.rmClient.unregisterApplicationMaster(FinalApplicationStatus.FAILED, "", "")
    LOG.debug("exiting")
  }

  private def startAndWaitForCompletion(attempt: Int, amConf: Config): Boolean = {
    var allocatedContainer: Boolean = false
    while (!allocatedContainer) {
      LOG.info("Requesting containers for attempt {}", attempt)
      val response: AllocateResponse = amConf.rmClient.allocate(0)
      for (container <- response.getAllocatedContainers) {
        allocatedContainer = true
        val ctx: ContainerLaunchContext = Records.newRecord(classOf[ContainerLaunchContext])
        ctx.setCommands(Collections.singletonList(amConf.cmd))
        ctx.setLocalResources(mapAsJavaMap(amConf.localResource))
        ctx.setEnvironment(amConf.appEnv)
        LOG.info("Launching container {} with command {} ", container, amConf.cmd: Any)
        amConf.nmClient.startContainer(container, ctx)
      }
      TimeUnit.SECONDS.sleep(YarnConstants.ContainerAllocationSleepSeconds)
    }
    LOG.info("Monitoring container")
    var completedContainer: Boolean = false
    var exitStatus: Int = 0
    while (!completedContainer) {
      val response: AllocateResponse = amConf.rmClient.allocate(0)
      for (status <- response.getCompletedContainersStatuses) {
        LOG.info(status.getDiagnostics)
        completedContainer = status.getState == ContainerState.COMPLETE
        exitStatus = status.getExitStatus
      }
      TimeUnit.SECONDS.sleep(YarnConstants.ContainerAllocationSleepSeconds)
    }
    exitStatus == 0
  }

  def addContainerAttempt(attempt: Int, rmClient: AMRMClient[AMRMClient.ContainerRequest]) {
    val capability: Resource = Records.newRecord(classOf[Resource])
    capability.setMemory(System.getProperty(YarnConstants.ContainerMBKey, YarnConstants.ContainerMBDefault).toInt)
    capability.setVirtualCores(System.getProperty(YarnConstants.ContainerCoresKey, YarnConstants.ContainerCoresDefault).toInt)
    LOG.info("Provided container memory setting is {}", System.getProperty(YarnConstants.ContainerMBKey))
    LOG.info("Configured container with {}mb and {} cores", capability.getMemory, capability.getVirtualCores)
    val priority: Priority = Records.newRecord(classOf[Priority])
    priority.setPriority(0)
    val containerAsk: AMRMClient.ContainerRequest = new AMRMClient.ContainerRequest(capability, null, null, priority)
    LOG.info("adding container {} ask: {}", attempt, containerAsk)
    rmClient.addContainerRequest(containerAsk)
  }


}
