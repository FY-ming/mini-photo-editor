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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
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

        // 适配刘海 / 状态栏安全触控区域
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.fitsSystemWindows = true
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topBar, 0, 0)
            insets
        }

        // 设置返回按钮
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
        val bitmap = currentBitmap ?: return

        progressBar.visibility = View.VISIBLE
        btnExport.isEnabled = false

        launch {
            val uri = saveBitmapToGallery(bitmap)

            progressBar.visibility = View.GONE
            btnExport.isEnabled = true
            Toast.makeText(requireContext(), "导出成功 ✅", Toast.LENGTH_SHORT).show()

            dismiss()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) : Uri? {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "MiniEditor_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MiniPhotoEditor")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
        return uri
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        currentBitmap?.recycle()
        currentBitmap = null
    }
}