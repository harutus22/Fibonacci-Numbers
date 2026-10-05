package com.example.fibonaccinumbers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fibonaccinumbers.databinding.ActivityMainBinding

const val MESSAGE_INITIAL = 101
const val MESSAGE_CANCEL = 102
const val MESSAGE_RESULT = 103
const val MESSAGE_KEY = "message_key"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var handler: Handler
    private val calculationViewModel by viewModels<CalculationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initHandler()

        binding.start.setOnClickListener {

            if (binding.start.text == getString(R.string.start)) {
                startClicked()
            } else {
                calculationViewModel.cancel()
            }
        }
    }

    private fun initHandler() {
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_INITIAL -> {
                        messageReceived(msg, R.string.current_is)
                    }

                    MESSAGE_RESULT -> {
                        binding.start.text = getString(R.string.start)
                        messageReceived(msg, R.string.result_is)
                    }

                    MESSAGE_CANCEL -> {
                        binding.result.text = ""
                        binding.start.text = getString(R.string.start)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        calculationViewModel.setHandler(handler)

        binding.start.text =
            if (calculationViewModel.isRunning()) {
                getString(R.string.cancel)
            } else {
                getString(R.string.start)
            }
    }

    override fun onStop() {
        calculationViewModel.removeHandler()
        super.onStop()
    }

    private fun startClicked() {
        val text = binding.inputNumber.text.toString()
        if (text.isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.please_enter_the_number),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        binding.start.text = getString(R.string.cancel)

        calculationViewModel.start(text.toInt())
    }

    private fun messageReceived(msg: Message, textResource: Int) {
        val bundle = msg.data
        val number = bundle.getInt(MESSAGE_KEY)
        binding.result.text = getString(textResource, number)
    }
}