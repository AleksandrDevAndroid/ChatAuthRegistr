package ru.netology.nmedia.fagment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegisterBinding
import ru.netology.nmedia.viewmodel.RegisterViewModel
import kotlin.getValue

class RegisterFragment : Fragment() {
    private val viewModelRegister: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.buttonEnter.setOnClickListener {
            val name = binding.enterName.text.toString().trim()
            val login = binding.enterLogin.text.toString().trim()
            val pass = binding.enterPass.text.toString().trim()
            val passAgain = binding.enterPassAgain.toString().trim()

            if (name.isEmpty() || login.isEmpty() || pass.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, R.string.emptyLoginOrPass, LENGTH_LONG).show()
            }

            if(!pass.equals(passAgain)){
                Toast.makeText(context, "Incorrect password", LENGTH_LONG).show()
            }

            viewModelRegister.signUp(login,pass,name,null)

            viewModelRegister.dataState.observe(viewLifecycleOwner) { state ->
                if (state.successes) {
                    findNavController().navigateUp()
                } else Toast.makeText(context, "Something wrong,try again ", LENGTH_LONG).show()
            }
        }


        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root

    }

}