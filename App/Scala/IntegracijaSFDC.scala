package com.clara.clowncar

import java.util.concurrent.Executors

import com.clara.clowncar.listener.PlatformEventListener
import com.clara.clowncar.processor._
import com.expeditelabs.analytics.S3AccessFactory // http://docs.aws.amazon.com/aws-sdk-php/v2/guide/service-s3.html
import com.expeditelabs.analytics.services.S3BackedLoanArchiveLibrary
import com.expeditelabs.db.repositories.DatabaseTables
import com.expeditelabs.salesforce.PollingSalesforceTester
