package com.orconservices.mirrormaker.yarn

import java.util.Properties

import scala.collection.JavaConverters._

object PropertyFileExtractor {
  def apply(properties: Properties, prefix: String): Properties = {
    buildFromMap(properties.asScala.filter(p => p._1.startsWith(prefix)).map(p => (p._1.replace(prefix, ""), p._2)))
  }

  def buildFromMap(properties: scala.collection.mutable.Map[String, String]) =
    (new Properties /: properties) {
      case (newProps, (propKey, propVal)) =>
        newProps.put(propKey, propVal)
        newProps
    }
}
