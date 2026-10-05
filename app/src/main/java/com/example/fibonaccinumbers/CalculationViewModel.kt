package com.example.fibonaccinumbers

import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.ViewModel
import kotlin.concurrent.thread

class CalculationViewModel : ViewModel() {

    private var workThread: Thread? = null
    private var activityHandler: Handler? = null
    private val handlerLock = Any()

    private var currentNumber: Int? = null
    private var result: Int? = null
    private var cancelled = false

    fun setHandler(handler: Handler) {
        synchronized(handlerLock) {
            activityHandler = handler
        }
        when {
            cancelled -> {
                handler.sendEmptyMessage(MESSAGE_CANCEL)
            }
            result != null -> {
                sendMessage(MESSAGE_RESULT, result!!)
            }
            currentNumber != null -> {
                sendMessage(MESSAGE_INITIAL, currentNumber!!)
            }
        }
    }

    fun removeHandler() {
        synchronized(handlerLock) {
            activityHandler = null
        }
    }

    fun start(limit: Int) {
        if (workThread?.isAlive == true) {
            return
        }
        cancelled = false
        currentNumber = null
        result = null

        workThread = thread {
            try {
                var calculationResult = 1
                var previousNumber = 1

                for (count in 1..limit) {
                    Thread.sleep(1000)
                    val temp = calculationResult
                    calculationResult += previousNumber
                    previousNumber = temp
                    currentNumber = count
                    sendMessage(
                        MESSAGE_INITIAL,
                        count
                    )
                }

                Thread.sleep(1000)
                result = calculationResult
                sendMessage(
                    MESSAGE_RESULT,
                    calculationResult
                )
            } catch (e: InterruptedException) {
                e.printStackTrace()
                cancelled = true
                currentNumber = null
                result = null

                sendMessage(MESSAGE_CANCEL)
            }
        }
    }

    fun cancel() {
        workThread?.interrupt()
    }

    fun isRunning(): Boolean {
        return workThread?.isAlive == true
    }

    private fun sendMessage(
        what: Int,
        value: Int = -1
    ) {
        synchronized(handlerLock) {
            val handler = activityHandler ?: return

            val message = handler.obtainMessage(what)

            message.data = Bundle().apply {
                putInt(MESSAGE_KEY, value)
            }

            handler.sendMessage(message)
        }
    }
}