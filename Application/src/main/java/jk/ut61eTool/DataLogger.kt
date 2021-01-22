package jk.ut61eTool

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jake.UT61e_decoder
import kotlinx.android.synthetic.main.log_activity.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class DataLogger(private val context : Activity) {

    private var fWriter: FileWriter? = null
    private var logFile: File? = null
    lateinit var log_dir : String

    private var filename: EditText = context.filename
    private var fileInfo: TextView = context.fileInfo
    private var logRunning: ProgressBar = context.logRunning
    private val switch = context.switch1
    @JvmField var lineCount = 0


    private fun createFolder(): Boolean {
        val folder = File(Environment.getExternalStorageDirectory().toString() + File.separator + log_dir)
        return if (!folder.exists()) {
            Toast.makeText(context, context.getString(R.string.new_folder, folder.name), Toast.LENGTH_LONG).show()
            folder.mkdirs()
        } else false
    }

    fun startLog() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 7)
            setRunning(false)
            return
        }

        createFolder()

        logFile = File(Environment.getExternalStorageDirectory().toString() + File.separator + log_dir + File.separator + filename.text)

        try {
            fWriter = FileWriter(logFile, true)
            fWriter?.write("# " + Calendar.getInstance().time.toString() + "\n")
            fWriter?.write(UT61e_decoder.csvHeader + "\n")
            fWriter?.flush()
            setRunning(true)
            lineCount = 0
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.storage_exp) + e.message, Toast.LENGTH_LONG).show()
            setRunning(false)
        }

    }

    fun stopLog() {
        if (fWriter == null) return
        try {
            fWriter?.flush()
            fWriter?.close()
            fWriter = null
            setRunning(false)
            (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(1)
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.storage_exp) + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setRunning(running: Boolean) {
        filename.isEnabled = !running
        logRunning.isIndeterminate = running
        switch.isChecked = running
    }

    fun isRunning(): Boolean {
        return fWriter != null
    }

    fun logData(data: String) {
        if (fWriter == null) return

        try {
            fWriter?.write(data + "\n")
            fWriter?.flush()
            lineCount++
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.storage_exp) + e.message, Toast.LENGTH_LONG).show()
        }
        fileInfo.text = context.getString(R.string.logfile_info, logFile?.path, logFile?.length()?.div(1000.0), lineCount)
    }
}