package com.clara.clowncar

import java.util.concurrent.Executors

import com.clara.clowncar.listener.PlatformEventListener
import com.clara.clowncar.processor._
import com.expeditelabs.analytics.S3AccessFactory // http://docs.aws.amazon.com/aws-sdk-php/v2/guide/service-s3.html
import com.expeditelabs.analytics.services.S3BackedLoanArchiveLibrary
import com.expeditelabs.db.repositories.DatabaseTables
import com.expeditelabs.salesforce.PollingSalesforceTester
import com.expeditelabs.salesforce.client.{NullSalesforceClient, SalesforceClient}
import com.expeditelabs.server.ExpediteServer
import com.expeditelabs.util.config.simpleconfig.DynamoDBBackedSimpleConfig
import com.expeditelabs.util.config.{EnvironmentMatchers, LoggingConfig}
import com.expeditelabs.util.{ExpediteEnvironment, ExpediteFlags}
import com.twitter.conversions.storage.intToStorageUnitableWholeNumber
import com.twitter.loggin.{Level, LoggerFactory}
import com.twitter.util._

object ClownCarServer extends ExpediteServer with EnvironmentMatchers {
  import ExpediteFlags._

  private[this] val environment =
    flag[ExpediteEnvironment.Value](
      "server.environment",
      ExpediteEnvironment.Development,
      "Environment to start"
    )

  private[this] val kafkaServers =
    flag(
      "kafka.servers",
      "localhost:9092",
      "comma separated list of kafka server:ports - kafka configuration value bootstrap.servers"
    )

  protected[this] val dynamoTable   = flag("aws.dynamo_table", "dev_app_env", "Dyanmo table to use")
  protected[this] val dynamoRegion  = flag("aws.dynamo_region", "us-west-2", "Dynamo region to use")
  protected[this] val databaseUrl   =
    flag(
      "db.url",
      "jdbc:postgresql://127.0.0.1:5432/expedite_dev?user=postgres&password=postgress",
      "Database URL to connect to"
    )

  def main(): Unit = {
    log.info("VROOM VROOM...")
    val s3Access = S3AccessFactory(environment(), statsReceiver)
    val (logBucket, sfUserName, sfPassword) =
      getDynamoConfigVals(
        environment(),
        dynamoTable(),
        dynamoRegion()
      )

    val archiveLibrary          = new S3BackedLoanArchiveLibrary(logBucket, s3Access)
    val SalesforceClient        = resolveSalesforceClient(environment(), sfUserName, sfPassword)
    val PollingSalesforceTester = new PollingSalesforceTester(sfUserName, salesforceClient, statsReceiver)

    val platformListener        = 
  }
}
