package com.orconservices.mirrormaker.yarn

object YarnConstants {
  val ApplicationMasterMBKey: String = "yarn.master.mb"
  val ApplicationMasterCoresKey: String = "yarn.master.cores"
  val ContainerMBKey: String = "yarn.container.mb"
  val ContainerCoresKey: String = "yarn.container.cores"
  val ContainerRetryCountKey: String = "yarn.container.retry"
  val ApplicationMasterMBDefault: String = "256"
  val ApplicationMasterCoresDefault: String = "1"
  val ContainerMBDefault: String = "128"
  val ContainerCoresDefault: String = "1"
  val ContainerRetryCountDefault: String = "8"
  val ContainerAllocationSleepSeconds: Long = 2
}
