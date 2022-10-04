package com.docter.icare.ui.main.aqi

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.docter.icare.R
import com.docter.icare.databinding.FragmentAirQualityIndexBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.ui.main.ToolbarClickListener
import org.kodein.di.android.x.closestDI

class AirQualityIndexFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentAirQualityIndexBinding

    private lateinit var viewModel: AirQualityIndexViewModel

    private val activityViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAirQualityIndexBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        setHasOptionsMenu(true)//設置右上角(Fragment)
        activityViewModel.toolbarClickListener = toolbarClickListener

        return binding.root
    }

    private val toolbarClickListener = object : ToolbarClickListener {
        override fun onExceptionClick() {
            super.onExceptionClick()
            Log.i("AirQualityIndexFragment","onExceptionClick go to Exception")
        }
    }

}