package com.orconservices.mirrormaker.messagehandlers

import java.util.Properties

import kafka.message.{Message, MessageAndMetadata}
import kafka.serializer.DefaultDecoder
import kafka.utils.VerifiableProperties
import org.scalatest.{FlatSpec, Matchers}

class TopicMutatorTests extends FlatSpec with Matchers {

  val TopicName: String = "test_topic"

  "A prefix mutator" should "return a record with prefixed topic" in {
    val mutator = new PrefixTopicMutatorMessageHandler("prefixed_")
    val bytesDecoder: DefaultDecoder = new DefaultDecoder(new VerifiableProperties(new Properties))
    val message: Message = new Message("message string".getBytes, "key".getBytes)
    val record: MessageAndMetadata[Array[Byte], Array[Byte]] = new MessageAndMetadata[Array[Byte], Array[Byte]](TopicName, 0, message, 0, bytesDecoder, bytesDecoder)

    val mutated = mutator.handle(record)
    mutated.size() shouldEqual  1
    mutated.get(0).topic() shouldEqual("prefixed_test_topic")
  }

  "A suffix mutator" should "return a record with suffixed topic" in {
    val mutator = new SuffixTopicMutatorMessageHandler("_suffixed")
    val bytesDecoder: DefaultDecoder = new DefaultDecoder(new VerifiableProperties(new Properties))
    val message: Message = new Message("message string".getBytes, "key".getBytes)
    val record: MessageAndMetadata[Array[Byte], Array[Byte]] = new MessageAndMetadata[Array[Byte], Array[Byte]](TopicName, 0, message, 0, bytesDecoder, bytesDecoder)

    val mutated = mutator.handle(record)
    mutated.size() shouldEqual 1
    mutated.get(0).topic() shouldEqual("test_topic_suffixed")
  }
}
