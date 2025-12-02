package com.example.mini_photo_editor.ui.export

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class ExportFragment : DialogFragment(), CoroutineScope {

    // Coroutine scope管理
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnExport: Button

    private var bitmapPath: String? = null
    private var currentBitmap: Bitmap? = null

    companion object {
        private const val ARG_BITMAP_PATH = "bitmap_path"

        fun newInstance(bitmapPath: String): ExportFragment {
            return ExportFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BITMAP_PATH, bitmapPath)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        // 初始化视图
        imageView = view.findViewById(R.id.iv_preview)
        progressBar = view.findViewById(R.id.progressBar)
        btnExport = view.findViewById(R.id.btn_export)

        // 设置导出按钮点击事件
        btnExport.setOnClickListener {
            exportImage()
        }

        // 设置取消按钮
        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }

        // 加载预览
        loadPreview()
    }

    private fun loadPreview() {
        bitmapPath = arguments?.getString(ARG_BITMAP_PATH)
        launch(Dispatchers.IO) {
            bitmapPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                withContext(Dispatchers.Main) {
                    currentBitmap = bitmap
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun exportImage() {
        currentBitmap?.let { bitmap ->
            progressBar.visibility = View.VISIBLE
            btnExport.isEnabled = false

            launch(Dispatchers.IO) {
                try {
                    val savedUri = saveBitmapToGallery(bitmap)

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnExport.isEnabled = true

                        if (savedUri != null) {
                            Toast.makeText(requireContext(), "图片已保存到相册", Toast.LENGTH_LONG).show()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "保存失败，请检查权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnExport.isEnabled = true
                        Toast.makeText(requireContext(), "保存异常: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "没有图片可导出", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val resolver = requireContext().contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val fileName = generateFileName()

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MiniPhotoEditor")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        return try {
            val uri = resolver.insert(imageCollection, contentValues)
            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                println("✅ 图片保存成功: $uri")
                uri
            }
        } catch (e: Exception) {
            println("❌ 保存失败: ${e.message}")
            null
        }
    }

    private fun generateFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "MiniEditor_${timeStamp}.jpg"
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        currentBitmap?.recycle()
        currentBitmap = null
    }
}