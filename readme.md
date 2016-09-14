# Kafka Mirror-Maker Supporting Code

## Overview
Kafka's mirroring feature makes it possible to maintain a replica of an existing Kafka cluster.

The tool is essentially a consumer and producer chained together and executed as a script deployed with Kafka `bin/kafka-mirror-maker.sh` passing a `consumer.properties` file, a `producer.properties` file and details of the topics to replicate as the `whitelist` parameter.
In cases where it would be more practical to list the topics not to include, the `blacklist` parameter can be used.

Out of the box, Mirror-Maker is intended to be use to replicate between two distinct clusters, there is no provision to mutate the name of the topic that is being replicated to - this is where message handlers come into play.

## Mirror-Maker Message Handlers
Mirror-Maker provides the functionality to intercept messages in flight through the consumer->producer flow and make modifications to the record.

The following message handlers are provided in the project, by implementing the `MirrorMaker.MirrorMakerMessageHandler` interface you can create custom handlers.

Handlers that have been provided are in this project are;

### PrefixTopicMutatorMessageHandler
This message handler will mutate the topic name of the record with a prefix which is passed in the in the `message.handler.args` parameter when calling the Mirror-Maker class.

For example, if the topic being consumed is `test_topic` and the prefix parameter is `mirrored_`, the producer will write to the topic `mirrored_test_topic`

### SuffixTopicMutatorMessageHandler
This message handler will mutate the topic name of the record with a suffix which is passed in the in the `message.handler.args` parameter when calling the Mirror-Maker class.

For example, if the topic being consumed is `test_topic` and the prefix parameter is `mirrored_`, the producer will write to the topic `mirrored_test_topic`

### TopicNameMapperMutatorMessageHandler
This message handler will completely replace the topic with the mapped topic name passed in to the `message.handler.args` parameter.

For example, if the message handler is initialised with the parameter `test_topic:modified_test_topic` then any records coming through the message handler originating on the `test_topic` topic will be produced on the `modified_test_topic`.

**Cautionary Note** It should be noted the that if a topic is being replicated while using this message handler then there must be a mapping provided for this topic. If a mapping is not present for a record then an error will be logged and the message will be dropped on the floor.
The risk to assuming that the mapping should be to the same topic is to create a circular publish with the record endlessly being replicated by itself.

In situations where you want to replicate to the same topic name, but the producer config is pointing to a different cluster then there is unlikely to be a need for the a message handler to be used.

## Mirror-Maker in Yarn
Mirror-Maker out of the box runs as a script which ultimately runs opaquely on a server - potentially this can be monitored at a process level but doesn't give the visibility and durability of running it Yarn.

The Mirror-Maker yarn application wraps Mirror-Maker in a yarn application master which is then submitted and run in the cluster with all the benefits provided by yarn. 
