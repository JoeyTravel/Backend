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

    val platformListener        = PlatformEventListener.withProcessors(
      kafkaServerName         = kafkaServers(),
      kafkaGroupIdName        = "clownCarListener",
      processors              = makeProcessors(archiveLibrary, salesforceClient),
      statsReceiver           = statsReceiver
    )

    val Executor        = Executors.newSingleThreadExecutor()
    val futurePool      = FuturePool(executor)
    val listenerFuture  = futurePool(platformListener.run())

    onExit {
      log.info("onExit - the car's done!")
      Await.ready(salesforceClient.close())
      Await.ready(platformListener.close())
      executor.shutdown()
    }

    PollingSalesforceTester.start()
    Await.ready(listenerFuture)
  }

  private[this] def makeProcessors(
    archiveLibrary: S3BackedLoanArchiveLibrary,
    salesforceClient: salesforceClient
  ) = {
    val dbTables = DatabaseTables(databaseUrl(), statsReceiver)
    val loanSnapshotProcessor = SalesforceLoanSnapshotProcessor.asynchronously(
      salesforceClient,
      archiveLibrary,
      statsReceiver
    )

    val updateLoanSpecialistProcessor = new UpdateLoanSpecialistProcessor(
      salesforceClient,
      dbTables,
      statsReceiver
    )

    val updateLeadActivationTokenProcessor = new UpdateLeadActivationTokenProcessor(
      salesforceClient,
      dbTables,
      statsReceiver
    )

    val updateLeadContactPrefsProcessor = new UpdateLeadContactPrefsProcessor(
      salesforceClient,
      dbTables,
      statsReceiver
    )

    val updateLeadInPortalStatusProcessor = new UpdateLeadInPortalStatusProcessor(
      salesforceClient,
      dbTables,
      statsReceiver
    )

    val updateLeadCreditPulledStatusProcessor = new UpdateLeadInPortalStatusProcessor(
      salesforceClient,
      statsReceiver
    )

    Seq(
      loanSnapshotProcessor,
      updateLoanSpecialistProcessor,
      updateLeadActivationTokenProcessor,
      updateLeadContactPrefsProcessor,
      updateLeadInPortalStatusProcessor,
      updateLeadCreditPulledStatusProcessor
    )
  }

  private[this] def getDynamoConfigVals(
    env: ExpediteEnvironment.Value,
    dynamoTable: String,
    dynamoRegion: String
  ): (String, String, String) = {
    val dynamoVals = new DynamoDBBackedSimpleConfig(
      table       = dynamoTable,
      region      = dynamoRegion,
      credentials = awsCredentials(env.name)
    )

    (
      dynamoVals.get[String]("LOG_DATA_BUCKET"),
      dynamoVals.get[String]("SALESFORCE_USERNAME"),
      dynamoVals.get[String]("SALESFORCE_PASSWORD")
    )
  }

  
}
