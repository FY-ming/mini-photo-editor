package com.example.mini_photo_editor.ui.home

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.databinding.FragmentHomeBinding
import com.example.mini_photo_editor.databinding.LayoutMainToolsBinding
import com.example.mini_photo_editor.databinding.LayoutQuickToolsBinding
import com.example.mini_photo_editor.databinding.LayoutRecentEditsBinding
import com.example.mini_photo_editor.ui.gallery.GalleryFragment
import com.example.mini_photo_editor.ui.home.adapter.BannerAdapter
import com.example.mini_photo_editor.ui.home.data.BannerItem
import com.example.mini_photo_editor.ui.home.data.MediaType
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class HomeFragment : Fragment(R.layout.fragment_home) {

    // 为每个include的布局创建绑定
    // 轮播页
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bannerAdapter: BannerAdapter
    // 主工具栏
    private lateinit var mainToolsBinding: LayoutMainToolsBinding
    // 快速功能
    private lateinit var quickToolsBinding: LayoutQuickToolsBinding
    // 最近编辑
    private lateinit var recentEditsBinding: LayoutRecentEditsBinding


    private var bannerTimer: Timer? = null
    private var currentBannerPosition = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 绑定include的布局
        _binding = FragmentHomeBinding.bind(view)
        mainToolsBinding = LayoutMainToolsBinding.bind(binding.mainTools.root)
        quickToolsBinding = LayoutQuickToolsBinding.bind(binding.quickTools.root)
        recentEditsBinding = LayoutRecentEditsBinding.bind(binding.recentEdits.root)

        setupBanner()


        setupClickListeners()
    }

    private fun setupBanner() {
        // 创建适配器
        bannerAdapter = BannerAdapter()

        // 设置ViewPager2
        val viewPager = requireView().findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = bannerAdapter
        viewPager.offscreenPageLimit = 3

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentBannerPosition = position
                updateIndicators()
            }
        })

        // 加载数据
        val bannerItems = BannerItem.createTutorialItems()
        bannerAdapter.submitList(bannerItems)


        // 设置指示器
        setupIndicators(bannerItems.size)

        // 设置点击事件
        bannerAdapter.onItemClick = { item ->
            when (item.mediaType) {
                MediaType.IMAGE -> {
                    Toast.makeText(requireContext(), "查看图片: ${item.title}", Toast.LENGTH_SHORT).show()
                }
                MediaType.GIF -> {
                    Toast.makeText(requireContext(), "查看GIF演示: ${item.title}", Toast.LENGTH_SHORT).show()
                }
                MediaType.VIDEO -> {
                    Toast.makeText(requireContext(), "播放视频教程: ${item.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 6. 启动自动轮播（可选）
        startAutoScroll()
    }


    private fun setupIndicators(count: Int) {
        val indicatorContainer = binding.banner.indicatorContainer
        indicatorContainer.removeAllViews()

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        for (i in 0 until count) {
            val imageView = ImageView(requireContext()).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (i == 0) R.drawable.indicator_selected else R.drawable.indicator_default
                    )
                )
                setLayoutParams(layoutParams)
            }
            indicatorContainer.addView(imageView)
        }
    }

    private fun updateIndicators() {
        val childCount = binding.banner.indicatorContainer.childCount
        for (i in 0 until childCount) {
            val imageView = binding.banner.indicatorContainer.getChildAt(i) as ImageView
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (i == currentBannerPosition) R.drawable.indicator_selected
                    else R.drawable.indicator_default
                )
            )
        }
    }

    private fun startAutoScroll() {
        bannerTimer = Timer()
        bannerTimer?.scheduleAtFixedRate(4000, 4000) {
            requireActivity().runOnUiThread {
                val nextPosition = (currentBannerPosition + 1) % bannerAdapter.itemCount
                binding.banner.viewPager.currentItem = nextPosition
            }
        }
    }


    private fun setupClickListeners() {
        mainToolsBinding.btnImportPhoto.setOnClickListener {
            navigateToGallery()
        }

        mainToolsBinding.btnCamera.setOnClickListener {
            Toast.makeText(requireContext(), "相机功能开发中", Toast.LENGTH_SHORT).show()
        }

        mainToolsBinding.btnCollage.setOnClickListener {
            Toast.makeText(requireContext(), "拼图功能开发中", Toast.LENGTH_SHORT).show()
        }

        // 快捷工具栏按钮
        quickToolsBinding.btnBatchEdit.setOnClickListener {
            Toast.makeText(requireContext(), "批量修图功能开发中", Toast.LENGTH_SHORT).show()
        }

        quickToolsBinding.btnSuperQuality.setOnClickListener {
            Toast.makeText(requireContext(), "超清画质功能开发中", Toast.LENGTH_SHORT).show()
        }

        quickToolsBinding.btnSmartCutout.setOnClickListener {
            Toast.makeText(requireContext(), "智能抠图功能开发中", Toast.LENGTH_SHORT).show()
        }

        quickToolsBinding.btnMagicErase.setOnClickListener {
            Toast.makeText(requireContext(), "魔法消除功能开发中", Toast.LENGTH_SHORT).show()
        }

        // 查看全部
        recentEditsBinding.tvSeeAll.setOnClickListener {
            Toast.makeText(requireContext(), "查看全部功能开发中", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToGallery() {
        val galleryFragment = GalleryFragment()
        galleryFragment.show(parentFragmentManager, "gallery_dialog")
    }
}