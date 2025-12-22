package com.example.booknpay.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.booknpay.data.local.TicketDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UploadTicketWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ticketDao: TicketDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingTickets = ticketDao.getPendingSyncTickets()
            if (pendingTickets.isNotEmpty()) {
                // In a real app, you would make a network call to your server here.
                // For this MVP, we'll just simulate a successful upload.
                println("Uploading ${pendingTickets.size} tickets...")
                Thread.sleep(2000) // Simulate network latency

                // After successful upload, update the sync status in the local database.
                pendingTickets.forEach { ticket ->
                    ticketDao.updateTicketStatus(
                        ticketId = ticket.ticketId,
                        paymentStatus = ticket.paymentStatus, // Keep the payment status as is
                        syncStatus = "SYNCED"
                    )
                }
                println("Upload complete!")
            }
            Result.success()
        } catch (e: Exception) {
            println("Upload failed: ${e.message}")
            Result.retry()
        }
    }
}
