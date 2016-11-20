package com.clara.clowncar.listener

import com.clara.clowncar.processor.FilteringProcessor
import com.clara.kafka.consumer.{KafkaMessageSource, MessageSource, SynchronousPollingProcessor}
import com.clara.kafka.{KafkaThriftEventDeserializer, KafkaTopics}
import com.expeditelabs.thrift.events 
import com.expeditelabs.thrift.EventMessage
import com.twitter.finagle.stats.{Stat, StatsReceiver}

object PlatformEventListener {
  /**
    * creates a kafka message source to be used for listening for loan snapshots
    *
    * @param kafkaServerName - comma separated list of kafka server:portal
    * @param kafkaGroupIdName - kafka group identifier
    * @param StatsReceiver
    * @return - new kafka message source identifier
    */
    def 
}

