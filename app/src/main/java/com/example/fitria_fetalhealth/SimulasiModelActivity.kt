package com.example.fitria_fetalhealth

import android.annotation.SuppressLint
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiModelActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "fetalhealth.tflite"

    private lateinit var resultText: TextView
    private lateinit var histogram_width: EditText
    private lateinit var histogram_min: EditText
    private lateinit var histogram_max: EditText
    private lateinit var histogram_mode: EditText
    private lateinit var histogram_mean: EditText
    private lateinit var histogram_median: EditText
    private lateinit var histogram_variance: EditText
    private lateinit var histogram_tendency: EditText
    private lateinit var checkButton : Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi_model)

        resultText = findViewById(R.id.txtResult)
        histogram_width = findViewById(R.id.histogram_width)
        histogram_min = findViewById(R.id.histogram_min)
        histogram_max = findViewById(R.id.histogram_max)
        histogram_mode = findViewById(R.id.histogram_mode)
        histogram_mean = findViewById(R.id.histogram_mean)
        histogram_median = findViewById(R.id.histogram_median)
        histogram_variance = findViewById(R.id.histogram_variance)
        histogram_tendency = findViewById(R.id.histogram_tendency)
        checkButton = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            var result = doInference(
                histogram_width.text.toString(),
                histogram_min.text.toString(),
                histogram_max.text.toString(),
                histogram_mode.text.toString(),
                histogram_mean.text.toString(),
                histogram_median.text.toString(),
                histogram_variance.text.toString(),
                histogram_tendency.text.toString())
            runOnUiThread {
                if (result == 0) {
                    resultText.text = "Normal"
                }else if (result == 1){
                    resultText.text = "Suspect"
                }else if (result == 2){
                    resultText.text = "Pathological"
                }
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = org.tensorflow.lite.Interpreter.Options()
        options.setNumThreads(9)
        options.setUseNNAPI(true)
        interpreter = org.tensorflow.lite.Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(input1: String, input2: String, input3: String, input4: String, input5: String, input6: String, input7: String, input8: String): Int{
        val inputVal = FloatArray(8)
        inputVal[0] = input1.toFloat()
        inputVal[1] = input2.toFloat()
        inputVal[2] = input3.toFloat()
        inputVal[3] = input4.toFloat()
        inputVal[4] = input5.toFloat()
        inputVal[5] = input6.toFloat()
        inputVal[6] = input7.toFloat()
        inputVal[7] = input8.toFloat()
        val output = Array(1) { FloatArray(3) }
        interpreter.run(inputVal, output)

        Log.e("result", (output[0].toList()+" ").toString())

        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer{
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}