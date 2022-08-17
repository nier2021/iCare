package com.docter.icare.ui.start.register

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.docter.icare.R
import com.docter.icare.databinding.FragmentRegisterBinding
import com.docter.icare.ui.base.BaseFragment
import org.kodein.di.android.x.closestDI

class RegisterFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            click = this@RegisterFragment
        }
        return binding.root
    }


}