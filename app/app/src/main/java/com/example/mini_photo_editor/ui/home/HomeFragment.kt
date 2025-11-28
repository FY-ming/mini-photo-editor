package com.example.mini_photo_editor.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.ui.gallery.GalleryFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var btnSelectPhoto: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 使用 findViewById 获取按钮
        btnSelectPhoto = view.findViewById(R.id.btn_select_photo)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnSelectPhoto.setOnClickListener {
            navigateToGallery()
        }
    }

    private fun navigateToGallery() {
        val galleryFragment = GalleryFragment()
        galleryFragment.show(parentFragmentManager, "gallery_dialog")
    }
}