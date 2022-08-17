package com.docter.icare.ui.main.aqi

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.docter.icare.R
import com.docter.icare.databinding.FragmentAirQualityIndexBinding
import com.docter.icare.ui.base.BaseFragment
import org.kodein.di.android.x.closestDI

class AirQualityIndexFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentAirQualityIndexBinding

    private lateinit var viewModel: AirQualityIndexViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAirQualityIndexBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

}