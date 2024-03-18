package com.example.learningkotlinn4

//import android.Manifest
import android.content.Intent
//import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.learningkotlinn4.ui.theme.*

class MainActivity : ComponentActivity() {
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var speakText by mutableStateOf("")
    private var docxFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val resultData = result.data
                val resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                speakText = resultArray?.get(0).toString()

                // Save to TXT when text is recognized
                saveToTxt(speakText)
            }
        }

        setContent {
            Surface(
                color = MaterialTheme.colors.background,
                modifier = Modifier.fillMaxSize(),
            ) {
                SpeechToTextApp()
            }
        }
    }


    @Composable
    fun SpeechToTextApp() {
        val context = LocalContext.current
        val isSystemInDarkTheme = isSystemInDarkTheme()

        val colors = if (isSystemInDarkTheme) {
            darkColors(
                primary = Purple200,
                primaryVariant = Purple700,
                secondary = Teal200
            )
        } else {
            lightColors(
                primary = Purple500,
                primaryVariant = Purple700,
                secondary = Teal200
            )
        }

        MaterialTheme(
            colors = colors,
            typography = Typography1,
            shapes = Shapes1
        ) {
            Scaffold(
//                topBar = {
//                    TopAppBar(
//                        title = { Text("Speech to Text Analyzer") },
//                        backgroundColor = MaterialTheme.colors.primary,
//                        contentColor = MaterialTheme.colors.onPrimary
//                    )
//                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
                    )
                    FloatingActionButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
                            }
                            startForResult.launch(intent)
                        },
                        backgroundColor = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Microphone", tint = Color.White)
                    }
                    // Display recognized text in a Card for an elegant look
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.surface,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = speakText, style = MaterialTheme.typography.h6, modifier = Modifier.padding(16.dp))
                    }
                    docxFile?.let {
                        Text("Word document saved: ${it.absolutePath}", style = MaterialTheme.typography.body1, modifier = Modifier.padding(top = 16.dp))
                    }
                    // Copyright Symbol
                    Text("© 2024 by Hrishikesh", style = MaterialTheme.typography.caption, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }




//    private fun startListening() {
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
//        }
//        startForResult.launch(intent)
//    }

    private fun saveToTxt(text: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            documentsDir.mkdirs()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "recognized_text_$timestamp.txt"
            val txtFile = File(documentsDir, filename)
            txtFile.writeText(text)
            launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Text saved to ${txtFile.absolutePath}", Toast.LENGTH_SHORT).show()
                // Convert the text file to Word document (DOCX)
                convertToDocx(txtFile)
            }
        }
    }

    private fun convertToDocx(txtFile: File) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val filename = "recognized_text_$timestamp.docx"
                val docxFile = File(txtFile.parent, filename)

                // Write the text content to the Word document
                FileOutputStream(docxFile).use { output ->
                    output.write(txtFile.readBytes())
                }

                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Text file copied to Word document: ${docxFile.absolutePath}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error copying text file to Word document: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
//package com.example.learningkotlinn4
//
//import android.content.Intent
//import android.os.Bundle
//import android.speech.RecognizerIntent
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Menu
//import androidx.compose.material.icons.rounded.Mic
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.learningkotlinn4.ui.theme.Learningkotlinn4Theme
//import java.util.Locale
//
//class MainActivity : ComponentActivity() {
//    lateinit var startForResult: ActivityResultLauncher<Intent>
//    var speakText by
//        mutableStateOf("Hrishikesh")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//            if(it.resultCode== RESULT_OK && it.data!=null){
//                var resultData = it.data
//                var resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//
//                speakText=resultArray?.get(0).toString()
//            }
//        }
//        setContent {
//            Learningkotlinn4Theme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                   SpeechToTextApp()
//                }
//            }
//        }
//    }
//
//    @Composable
//    fun SpeechToTextApp() {
//
//        var context = LocalContext.current
//        Box(modifier = Modifier.fillMaxSize()){
//            Column (
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ){
//                IconButton(onClick = {
//                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//                    intent.putExtra(
//                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//                    )
//                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak Now")
//                    startForResult.launch(intent)
//                }) {
//                    Icon(Icons.Rounded.Mic , contentDescription = null )
//
//                }
//                Text(text = speakText)
//            }
//        }
//    }
//}
//package com.example.learningkotlinn4
//
//import android.content.Intent
//import android.os.Bundle
//import android.speech.RecognizerIntent
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Mic
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.learningkotlinn4.ui.theme.Learningkotlinn4Theme
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//    private lateinit var startForResult: ActivityResultLauncher<Intent>
//    private var speakText by mutableStateOf("")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val resultData = result.data
//                val resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//
//                speakText = resultArray?.get(0).toString()
//            }
//        }
//
//        setContent {
//            Learningkotlinn4Theme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    SpeechToTextApp()
//                }
//            }
//        }
//    }
//
//    @Composable
//    fun SpeechToTextApp() {
//        val context = LocalContext.current
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = {
//                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                        putExtra(
//                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//                        )
//                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
//                    }
//                    startForResult.launch(intent)
//                }
//            ) {
//                Icon(Icons.Rounded.Mic, contentDescription = "Microphone")
//            }
//            Text(text = speakText)
//        }
//    }
//}


//speech to text



//package com.example.learningkotlinn4
//
//import android.Manifest
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.os.Environment
//import android.speech.RecognizerIntent
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Mic
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.content.ContextCompat
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//    private lateinit var startForResult: ActivityResultLauncher<Intent>
//    private var speakText by mutableStateOf("")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val resultData = result.data
//                val resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//
//                val recognizedText = resultArray?.get(0).toString()
//                if (recognizedText.isNotBlank()) {
//                    // Save recognized text as a .txt file
//                    saveToTxt(recognizedText)
//                } else {
//                    Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        setContent {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                SpeechToTextApp()
//            }
//        }
//    }
//
//    @Composable
//    fun SpeechToTextApp() {
//        val context = LocalContext.current
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = {
//                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                        putExtra(
//                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//                        )
//                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
//                    }
//                    startForResult.launch(intent)
//                }
//            ) {
//                Icon(Icons.Rounded.Mic, contentDescription = "Microphone")
//            }
//            Text(text = speakText)
//        }
//    }
//
//    private fun saveToTxt(text: String) {
//        // Check if the app has permission to write to external storage
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
////            // Request permission to write to external storage if not granted
////            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
////        } else {
//            GlobalScope.launch(Dispatchers.IO) {
//                // Get the external storage directory
//                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//
//                // Create the "Documents" directory if it doesn't exist
//                documentsDir.mkdirs()
//
//                // Create a file to save the text
//                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//                val filename = "recognized_text_$timestamp.txt"
//                val txtFile = File(documentsDir, filename)
//
//                // Write the text to the file
//                txtFile.writeText(text)
//
//                // Show a toast message indicating that the text was saved successfully
//                launch(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Text saved to $txtFile", Toast.LENGTH_SHORT).show()
////                }
//            }
//        }
//    }
//
//
//
//}
//docx

//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.os.Environment
//import android.speech.RecognizerIntent
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Mic
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//    private lateinit var startForResult: ActivityResultLauncher<Intent>
//    private var speakText by mutableStateOf("")
//    private var docxFile: File? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val resultData = result.data
//                val resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//
//                speakText = resultArray?.get(0).toString()
//
//                // Save to TXT when text is recognized
//                saveToTxt(speakText)
//            }
//        }
//
//        setContent {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                SpeechToTextApp()
//            }
//        }
//    }
//
//    @Composable
//    fun SpeechToTextApp() {
//        val context = LocalContext.current
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = {
//                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                        putExtra(
//                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//                        )
//                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
//                    }
//                    startForResult.launch(intent)
//                }
//            ) {
//                Icon(Icons.Rounded.Mic, contentDescription = "Microphone")
//            }
//            Text(text = speakText)
//            docxFile?.let {
//                Text("Word document saved: ${it.absolutePath}")
//            }
//        }
//    }
//
//    private fun saveToTxt(text: String) {
////        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
////            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
////        } else {
//            GlobalScope.launch(Dispatchers.IO) {
//                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                documentsDir.mkdirs()
//                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//                val filename = "recognized_text_$timestamp.txt"
//                val txtFile = File(documentsDir, filename)
//                txtFile.writeText(text)
//                launch(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Text saved to ${txtFile.absolutePath}", Toast.LENGTH_SHORT).show()
//                    // Convert the text file to Word document (DOCX)
//                    convertToDocx(txtFile)
////                }
//            }
//        }
//    }
//
//    private fun convertToDocx(txtFile: File) {
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//                val filename = "recognized_text_$timestamp.docx"
//                val docxFile = File(txtFile.parent, filename)
//
//                // Write the text content to the Word document
//                FileOutputStream(docxFile).use { output ->
//                    output.write(txtFile.readBytes())
//                }
//
//                launch(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Text file copied to Word document: ${docxFile.absolutePath}", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                launch(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Error copying text file to Word document: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}

//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.os.Environment
//import android.speech.RecognizerIntent
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Mic
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.pdmodel.PDPage
//import org.apache.pdfbox.pdmodel.PDPageContentStream
//import org.apache.pdfbox.pdmodel.font.PDType1Font
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//    private lateinit var startForResult: ActivityResultLauncher<Intent>
//    private var speakText by mutableStateOf("")
//    private var txtFile: File? = null
//    private var pdfFile: File? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val resultData = result.data
//                val resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//
//                speakText = resultArray?.get(0).toString()
//
//                // Save to TXT and PDF when text is recognized
//                saveToTxtAndPdf(speakText)
//            }
//        }
//
//        setContent {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                SpeechToTextApp()
//            }
//        }
//    }
//
//    @Composable
//    fun SpeechToTextApp() {
//        val context = LocalContext.current
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = {
//                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                        putExtra(
//                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//                        )
//                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
//                    }
//                    startForResult.launch(intent)
//                }
//            ) {
//                Icon(Icons.Rounded.Mic, contentDescription = "Microphone")
//            }
//            Text(text = speakText)
//            txtFile?.let {
//                Text("TXT file saved: ${it.absolutePath}")
//            }
//            pdfFile?.let {
//                Text("PDF file saved: ${it.absolutePath}")
//            }
//        }
//    }
//
//    private fun saveToTxtAndPdf(text: String) {
//
//            GlobalScope.launch(Dispatchers.IO) {
//                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                documentsDir.mkdirs()
//                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//
//                // Save to TXT
//                val txtFilename = "recognized_text_$timestamp.txt"
//                txtFile = File(documentsDir, txtFilename)
//                txtFile?.writeText(text)
//
//                // Save to PDF
//                val pdfFilename = "recognized_text_$timestamp.pdf"
//                pdfFile = File(documentsDir, pdfFilename)
//                pdfFile?.let { saveToPdf(text, it) }
//
//                launch(Dispatchers.Main) {
//                    txtFile?.let {
//                        Toast.makeText(this@MainActivity, "Text saved to ${it.absolutePath}", Toast.LENGTH_SHORT).show()
//                    }
//                    pdfFile?.let {
//                        Toast.makeText(this@MainActivity, "PDF saved to ${it.absolutePath}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//
//    }
//
//    private fun saveToPdf(text: String, pdfFile: File) {
//        try {
//            val document = PDDocument()
//            val page = PDPage()
//            document.addPage(page)
//            val contentStream = PDPageContentStream(document, page)
//            contentStream.setFont(PDType1Font.HELVETICA, 12f)
//            contentStream.beginText()
//            contentStream.newLineAtOffset(100f, 700f)
//            contentStream.showText(text)
//            contentStream.endText()
//            contentStream.close()
//            document.save(pdfFile)
//            document.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}

//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.os.Environment
//import android.speech.RecognizerIntent
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Mic
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//    private lateinit var startForResult: ActivityResultLauncher<Intent>
//    private var speakText by mutableStateOf("")
//    private var docxFile: File? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val resultData = result.data
//                val resultArray = resultData?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//
//                speakText = resultArray?.get(0).toString()
//
//                // Save to TXT when text is recognized
//                saveToTxt(speakText)
//            }
//        }
//
//        setContent {
//            Surface(
//                color = MaterialTheme.colors.background,
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                SpeechToTextApp()
//            }
//        }
//    }
//
//    @Composable
//    fun SpeechToTextApp() {
//        val context = LocalContext.current
//
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = {
//                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//                        startListening()
//                    } else {
//                        Toast.makeText(context, "Permission needed to use microphone", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            ) {
//                Icon(Icons.Rounded.Mic, contentDescription = "Microphone", modifier = Modifier.size(48.dp))
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = "Tap on the microphone to start speaking",
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.body1
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = speakText,
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.h6
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            docxFile?.let {
//                Text(
//                    "Word document saved: ${it.absolutePath}",
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.body1
//                )
//            }
//        }
//    }
//
//    private fun startListening() {
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now")
//        }
//        startForResult.launch(intent)
//    }
//
//    private fun saveToTxt(text: String) {
//        GlobalScope.launch(Dispatchers.IO) {
//            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//            documentsDir.mkdirs()
//            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val filename = "recognized_text_$timestamp.txt"
//            val txtFile = File(documentsDir, filename)
//            txtFile.writeText(text)
//            launch(Dispatchers.Main) {
//                Toast.makeText(this@MainActivity, "Text saved to ${txtFile.absolutePath}", Toast.LENGTH_SHORT).show()
//                // Convert the text file to Word document (DOCX)
//                convertToDocx(txtFile)
//            }
//        }
//    }
//
//    private fun convertToDocx(txtFile: File) {
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//                val filename = "recognized_text_$timestamp.docx"
//                val docxFile = File(txtFile.parent, filename)
//
//                // Write the text content to the Word document
//                FileOutputStream(docxFile).use { output ->
//                    output.write(txtFile.readBytes())
//                }
//
//                launch(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Text file copied to Word document: ${docxFile.absolutePath}", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                launch(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Error copying text file to Word document: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}


