package com.orconservices.mirrormaker.messagehandlers

import java.util
import java.util.Collections

import kafka.consumer.BaseConsumerRecord
import kafka.message.MessageAndMetadata
import kafka.tools.MirrorMaker
import org.apache.kafka.clients.producer.ProducerRecord

class PrefixTopicMutatorMessageHandler(val topicPrefix: String) extends MirrorMaker.MirrorMakerMessageHandler {
  override def handle(record: MessageAndMetadata[Array[Byte], Array[Byte]]): util.List[ProducerRecord[Array[Byte], Array[Byte]]] = {
    Collections.singletonList(new ProducerRecord[Array[Byte], Array[Byte]](topicPrefix + record.topic, record.partition, record.key, record.message))
  }

  override def handle(record: BaseConsumerRecord): util.List[ProducerRecord[Array[Byte], Array[Byte]]] = {
    Collections.singletonList(new ProducerRecord[Array[Byte], Array[Byte]](topicPrefix + record.topic, record.partition, record.key, record.value))
  }
}
