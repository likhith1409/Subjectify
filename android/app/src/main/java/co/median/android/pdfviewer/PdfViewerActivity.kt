package co.median.android.pdfviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import co.median.android.R
import co.median.android.databinding.ActivityPdfViewBinding
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class PdfViewerActivity : AppCompatActivity() {

    private val TAG = PdfViewerActivity::class.simpleName
    private lateinit var binding: ActivityPdfViewBinding
    private var pdfUrl: String? = null
    private var tempFile: File? = null
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // top menu
        with(binding.toolbar) {
            setSupportActionBar(this)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_share -> {
                        tempFile?.let { sharePdfFile(it) }
                        true
                    }

                    R.id.action_print -> {
                        tempFile?.let { printPdfFile(this@PdfViewerActivity, it) }
                        true
                    }

                    else -> false
                }
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        pdfUrl = intent.getStringExtra("url")
        pdfUrl?.let { downloadAndLoadPDFUrl(it) }
    }

    private fun downloadAndLoadPDFUrl(pdfUrl: String) {
        backgroundExecutor.execute {
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    Toast.makeText(this, "Failed to load PDF file.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@execute
                }

                // check if the content type is PDF
                val contentType = connection.contentType
                if (!contentType.contains("application/pdf")) {
                    connection.disconnect()
                    mainThreadHandler.post {
                        Log.e(TAG, "The file is not a PDF.")
                        finish()
                    }
                    return@execute
                }

                var fileName = url.path.substringAfterLast("/", "downloaded_pdf")

                val contentDisposition = connection.getHeaderField("Content-Disposition")

                if (contentDisposition != null && contentDisposition.contains("filename=")) {
                    fileName = contentDisposition.substringAfter("filename=")
                    fileName = fileName.trim('"')
                }

                mainThreadHandler.post {
                    binding.toolbar.title = fileName
                }

                // create cache "downloads" directory if it doesn't exist
                // note: must save on "downloads" subdirectory or as specified in filepaths.xml
                val downloadsDir = File(cacheDir, "downloads")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                // create file
                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { fileOutput ->
                    connection.inputStream.use { inputStream ->
                        inputStream.copyTo(fileOutput)
                    }
                }

                connection.disconnect()


                tempFile = file

                mainThreadHandler.post {
                    binding.progress.visibility = View.INVISIBLE
                    loadUrlToPDFViewer(file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download PDF.", e)
                mainThreadHandler.post {
                    Toast.makeText(this, "Failed to load PDF file.", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }

    private fun loadUrlToPDFViewer(file: File) {
        binding.pdfView.initWithFile(file)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pdfviewer_toolbar_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        tempFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }
    }

    private fun sharePdfFile(pdfFile: File) {
        val fileUri = FileProvider.getUriForFile(
            this, applicationContext.packageName + ".fileprovider",
            pdfFile
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/pdf"
        }
        startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    }

    private fun printPdfFile(context: Context, file: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${context.getString(R.string.app_name)} Document"
        val printAdapter: PrintDocumentAdapter = PdfPrintDocumentAdapter(file)
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}