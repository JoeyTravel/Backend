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
    def makeKafkaMessageSource(
      kafkaServerName: String,
      kafkaGroupIdName: String,
      statsReceiver: StatsReceiver
    ): MessageSource[events.EventMessage] = {
      new KafkaMessageSource[EventMessage](
        kafkaServer       = kafkaServerName,
        kafkaGroupId      = kafkaGroupIdName,
        // N. B. This is the kafka topic, which is a different concept from the event topic
        // on the [[com.expeditelabs.thrift.events.EventMessage]]
        topics            = Seq(KafkaTopics.platformEvents),
        valueDeserializer = new KafkaThriftEventDeserializer,
        statsReceiver     = statsReceiver
      )
    }

    /**
      * Creates a PlatformEventListener for the given kafkaServerName and kafkaGroupIdName,
      * along with specified Seq of FilteringProcessor that will actually do the processing
      *
      * @param kafkaServerName - comma separated list of kafka server:port
      * @param kafkaGroupIdName - kafka group identifier
      * @param processors - a Seq of FilteringProcessor that will actually process the MessageSource
      * @param statsReceiver
      * @return
      */
    def withProcessors(
      kafkaServerName: String,
      kafkaGroupIdName:String,
      processors: Seq[FilteringProcessor],
      statsReceiver: statsReceiver
    ) = {
        new PlatformEventListener(
          makeKafkaMessageSource(kafkaServerName, kafkaGroupIdName, statsReceiver),
          processors,
          statsReceiver
        )
    }
}

class PlatformEventListener(
  val MessageSource: MessageSource[events, EventManager],
  processors: Seq[FilteringProcessor],
  statsReceiver: statsReceiver
) extends SynchronousPollingProcessor[events.EventMessage] {
  private[this] val scopedStats     = statsReceiver.scope("platform_event_listener")
  private[this] val processCounter  = scopedStats.counter("process")
  private[this] val batchSizeStat   = scopedStats.stats("batch_size")
  private[this] val processTimeStat = scopedStats.stat("process_time_ms")

  scopedStats.addGauge("num_processors")(processors.size)

  // TODO(vc): Potentially add a Async semaphore here to control concurrency of processing
  override def 

}
