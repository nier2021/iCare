package com.docter.icare.ui.personInfo.edit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.docter.icare.R

class PersonInfoEditFragment : Fragment() {

    private lateinit var viewModel: PersonInfoEditViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_person_info_edit, container, false)
    }


}