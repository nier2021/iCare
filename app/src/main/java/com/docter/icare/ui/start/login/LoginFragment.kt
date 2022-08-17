package com.docter.icare.ui.start.login

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.docter.icare.ui.main.MainActivity
import com.docter.icare.R
import com.docter.icare.databinding.FragmentLoginBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.utils.snackbar
import com.docter.icare.utils.toast
import com.docter.icare.view.dialog.CustomProgressDialog
import kotlinx.coroutines.launch
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.clearStack

class LoginFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentLoginBinding

    private val factory: LoginViewModelFactory by instance()
    private val viewModel: LoginViewModel by lazy { ViewModelProvider(this, factory)[LoginViewModel::class.java] }

    private val progressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity(), R.string.signing) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            click = this@LoginFragment
            entity = viewModel.entity
        }

        return binding.root
    }

    override fun onClick(view: View) {
        super.onClick(view)
        when(view){
            binding.btnLogin -> checkInput()
            binding.tvRegister -> register()
        }
    }

    private fun checkInput(){
        runCatching {
            viewModel.checkInput()
        }.onSuccess {
            login()
        }.onFailure {
            it.printStackTrace()
            binding.root.snackbar(it)
        }
    }

    private fun login(){
//        Log.i("LoginFragment","login account=>${viewModel.entity.account},password=>${viewModel.entity.password} ")
        progressDialog.show()

        lifecycleScope.launch {
            runCatching {
                viewModel.login()
            }.onSuccess {
                progressDialog.dismiss()
                requireContext().toast(getString(R.string.login_success))
                main {
                    val intent = Intent()
                    intent.setClass(requireActivity(), MainActivity::class.java).clearStack()
                    startActivity(intent, makeSceneTransitionAnimation(requireActivity()).toBundle())
//                    startActivity(Intent(requireActivity().applicationContext, MainActivity::class.java), makeSceneTransitionAnimation(requireActivity()).toBundle())
                }
            }.onFailure {
                progressDialog.dismiss()
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }

    private fun register(){
       findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

}