package com.forgblord.iot

import android.icu.text.DateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.forgblord.iot.databinding.ActivityMainBinding
import com.forgblord.iot.models.Config
import com.forgblord.iot.models.ConfigRequest
import com.forgblord.iot.models.Notes
import com.forgblord.iot.models.Response
import com.forgblord.iot.models.Server
import com.forgblord.iot.models.SystemState
import com.forgblord.iot.network.Builder
import com.forgblord.iot.utils.NETWORK_STATE
import com.forgblord.iot.utils.ServerDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var client: Builder
    private lateinit var binding: ActivityMainBinding

    private lateinit var notes: Notes
    private lateinit var systemState: SystemState
    private lateinit var config: Config
    private lateinit var server: Server

    private val editTextOnBackPressed = View.OnKeyListener { v, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            v.clearFocus()
            return@OnKeyListener true
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        notes = Notes(this)
        systemState = SystemState(this)
        config = Config(this)
        server = Server(this)
        client = Builder(server.getURL().toString())

        populateData()

        // ===== PLANT TITLE LISTENERS AND STUFF =====

        binding.etPlantname.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.btnNameSubmit.visibility = View.VISIBLE
                binding.fabWater.visibility = View.GONE
            }
            else {
                binding.btnNameSubmit.visibility = View.GONE
                binding.fabWater.visibility = View.VISIBLE
            }
        }

        binding.etPlantname.setOnKeyListener(editTextOnBackPressed)

        binding.btnNameSubmit.setOnClickListener {
            val plantName = binding.inputPlantname.editText!!.text.toString()
            notes.updatePlantInfo("title", plantName)

            hideKeyboard()
            binding.inputPlantname.clearFocus()
        }

        // ===== NOTE LISTENERS AND STUFF =====

        binding.etNote.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.buttonContainer.visibility = View.VISIBLE
                binding.fabWater.visibility = View.GONE
            }
            else {
                binding.buttonContainer.visibility = View.GONE
                binding.fabWater.visibility = View.VISIBLE
            }
        }

        binding.etNote.setOnKeyListener(editTextOnBackPressed)

        binding.btnSubmit.setOnClickListener {
            val note = binding.inputNote.editText!!.text.toString()
            notes.updatePlantInfo("note", note)

            hideKeyboard()
            binding.etNote.clearFocus()
            binding.buttonContainer.visibility = View.GONE
        }

        binding.btnCancel.setOnClickListener {
            binding.etNote.setText(notes.plantInfo["note"])

            hideKeyboard()
            binding.etNote.clearFocus()
            binding.buttonContainer.visibility = View.GONE
        }

        // ===== FAB =====
        binding.fabWater.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                manageProgress("start")

                try {
                    val data = client.api.requestWater()
                    parseResponse(data)
                    populateFromSensors()
                } catch (exception: Exception) {
                    toastServerError()
                }

                manageProgress("stop")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    manageProgress("start")
                    try {
                        val data = client.api.requestData()
                        parseResponse(data)
                        populateFromSensors()
                    } catch (exception: Exception) {
                        toastServerError()
                    }

                    manageProgress("stop")
                }
                true
            }
            R.id.action_calibrate -> {
                displayCalibrateDialog()
                true
            }
            R.id.action_network -> {
                displayNetworkDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayCalibrateDialog() {
        val dialog = ConfigDialog(this)
        dialog.show()

        val waterLevel = dialog.findViewById<TextInputLayout>(R.id.et_water_level)
        waterLevel.editText!!.setText(config.config.waterMax.toString())
        val soilMin = dialog.findViewById<TextInputLayout>(R.id.et_soil_min)
        soilMin.editText!!.setText(config.config.soilMin.toString())
        val soilMax = dialog.findViewById<TextInputLayout>(R.id.et_soil_max)
        soilMax.editText!!.setText(config.config.soilMax.toString())
        val moistThreshold = dialog.findViewById<TextInputLayout>(R.id.et_moisture)
        moistThreshold.editText!!.setText(config.config.moistThreshold.toString())
        val checkInterval = dialog.findViewById<TextInputLayout>(R.id.et_check_interval)
        checkInterval.editText!!.setText(config.config.checkInterval.toString())
        val duration = dialog.findViewById<TextInputLayout>(R.id.et_duration)
        duration.editText!!.setText(config.config.wateringDuration.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val data = client.api.requestConfig()

                runOnUiThread {
                    waterLevel.editText!!.setText(data.waterMax.toString())
                    soilMin.editText!!.setText(data.soilMin.toString())
                    soilMax.editText!!.setText(data.soilMax.toString())
                    moistThreshold.editText!!.setText(data.moistThreshold.toString())
                    checkInterval.editText!!.setText(data.checkInterval.toString())
                    duration.editText!!.setText(data.wateringDuration.toString())

                    config.updateConfig(data)
                }
            } catch (exception: Exception) {
                toastServerError()
            }
        }

        dialog.findViewById<Button?>(R.id.btn_cancel_calibrate).apply {
            setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.findViewById<Button?>(R.id.btn_submit_calibrate).apply {
            setOnClickListener {
                val body = ConfigRequest(
                    waterMax = waterLevel.editText!!.text.toString().toFloat(),
                    soilMin = soilMin.editText!!.text.toString().toInt(),
                    soilMax = soilMax.editText!!.text.toString().toInt(),
                    moistThreshold = moistThreshold.editText!!.text.toString().toInt(),
                    checkInterval = checkInterval.editText!!.text.toString().toInt(),
                    wateringDuration = duration.editText!!.text.toString().toInt(),
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val message = client.api.postConfig(
                            body.waterMax,
                            body.soilMin,
                            body.soilMax,
                            body.moistThreshold,
                            body.checkInterval,
                            body.wateringDuration * 1000
                        )
                        if (message.message == "success") {
                            config.updateConfig(body)
//                            Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    } catch (exception: Exception) {
                        toastServerError()
                    }
                }
            }
        }

    }

    private fun displayNetworkDialog() {
        val dialog = ServerDialog(this)
        dialog.show()

        val url = dialog.findViewById<TextInputLayout>(R.id.et_url)
        url.editText!!.setText(server.getURL())

        dialog.findViewById<Button>(R.id.btn_cancel_server).apply {
            setOnClickListener {
                dialog.cancel()
            }
        }

        dialog.findViewById<Button>(R.id.btn_submit_server).apply {
            setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val message = client.api.test("${url.editText!!.text}/test")

                        if (message.message == "success") {
                            client = Builder(url.editText!!.text.toString())
                            server.setURL(url.editText!!.text.toString())
                            dialog.dismiss()
                        }
                    } catch (exception: Exception) {
                        toastServerError()
                    }
                }
            }
        }
    }

    private fun parseResponse(data: Response) {
        val curDate = Date()
        val dateString = DateFormat.getPatternInstance("HH:mm dd-MM").format(curDate)

        systemState.updateState(data.soil, data.water, dateString)
    }

    private fun toastServerError() {
        runOnUiThread {
            Toast.makeText(
                this,
                "Couldn't connect to server",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun manageProgress(action: String) {
        runOnUiThread {
            supportActionBar?.title = NETWORK_STATE[action]!!.first
            binding.progress.visibility = NETWORK_STATE[action]!!.second
        }
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
    }

    private fun populateFromSensors() {
        runOnUiThread {
            val soilInfo = systemState.soil
            val soilProgressStr = getString(R.string.soil_percentage, soilInfo.percent)
            val soilSensorStr = getString(R.string.soil_value, soilInfo.value)
            binding.tvSoilPercent.text = soilProgressStr
            binding.progressSoil.progress = soilInfo.percent
//            binding.progressSoil.setIndicatorColor(getSoilColor(this, soilInfo.percent))
            binding.tvSoilValue.text = soilSensorStr

            val waterInfo = systemState.water
            val waterProgressStr = getString(R.string.water_percentage, waterInfo.percent)
            val waterSensorStr = getString(R.string.water_value, waterInfo.level)
            binding.tvWaterPercent.text = waterProgressStr
            binding.progressWater.progress = waterInfo.percent
//            binding.progressWater.setIndicatorColor(getColorByState(this, waterInfo.critical))
            binding.tvWaterValue.text = waterSensorStr

            val lastCheckStr = getString(R.string.last_check, systemState.lastCheck)
            binding.tvLastCheckup.text = lastCheckStr
        }
    }

    private fun populateData() {
        binding.etPlantname.setText(notes.plantInfo["title"])
        binding.etNote.setText(notes.plantInfo["note"])

        populateFromSensors()
    }
}