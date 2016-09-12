#Example
The example configuration attached will mirror a local kafka topic called test topic to mirrored_test_topic.

Below are the available configurations - if you want to consume from another cluster then update the `job.consumer.zookeeper.connect` to the relevant zookeeper.

##Configuration

| Setting                                      | Default Value | Description                                                                                     |
|:---------------------------------------------|:--------------|:------------------------------------------------------------------------------------------------|
| job.name                                     |               | The name of the mirror maker job as appears in yarn                                             |
| job.producer.serializer.class                |               | Serialiser class for the producer                                                               |
| job.producer.key.serializer                  |               | Serialiser class for the key                                                                    |
| job.producer.value.serializer                |               | Serialiser class for the value                                                                  |
| job.consumer.zookeeper.connect               |               | The connection details for consumer zookeeper                                                   |
| job.consumer.zookeeper.connection.timeout.ms |               | timeoutInMilliseconds for the Zookeeper connection                                              |
| job.consumer.group.id                        |               | Consumer group for the job                                                                      |
| job.config.num.streams                       | 1             | Number of consumers to use                                                                      |
| job.whitelist                                |               | Comma separated list of topics to replicate                                                     |
| job.topicmutator.class                       |               | The fully qualified class name of the topic mutator to use                                      |
| job.topicmutator.args                        |               | Any arguments required by the topic mutator                                                     |
| job.producer.bootstrap.servers               |               | The Kafka bootstrap servers for the produce                                                     |
| yarn.master.mb                               | 256           | Memory to allocate the application                                                              |
| yarn.master.cores                            | 1             | Containers to allocate the application                                                          |
| yarn.container.mb                            | 128           | Memory to allocate the container - will be overridden by yarn.scheduler.minimum-allocation-mb   |
| yarn.container.cores                         | 1             | Cores to allocate to container - will be overridden by yarn.scheduler.minimum-allocation-vcores |
| yarn.container.retry                         |               | Number of times to try restarting the container on failure                                      |

##Running

```
yarn jar mirrormaker-yarn-0.1.jar --config-path mirrormaker.properties
```
