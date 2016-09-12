package com.orconservices.mirrormaker.messagehandlers

import java.util.Collections

import kafka.message.MessageAndMetadata
import kafka.tools.MirrorMaker
import org.apache.kafka.clients.producer.ProducerRecord

abstract class PrefixTopicMutatorMessageHandler(val topicPrefix: String) extends MirrorMaker.MirrorMakerMessageHandler {
  def handle(record: MessageAndMetadata[Array[Byte], Array[Byte]]): java.util.List[ProducerRecord[Array[Byte], Array[Byte]]] = Collections.singletonList(new ProducerRecord[Array[Byte], Array[Byte]](topicPrefix + record.topic, record.partition, record.key, record.message))
}
