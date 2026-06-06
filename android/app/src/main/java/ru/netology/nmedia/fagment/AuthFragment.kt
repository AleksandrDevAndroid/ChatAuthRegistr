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
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class AuthFragment : Fragment() {
    private val viewModelAuth: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.buttonEnter.setOnClickListener {
            val login = binding.enterLogin.text.toString().trim()
            val pass = binding.enterPass.text.toString().trim()

            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, R.string.emptyLoginOrPass, LENGTH_LONG).show()
            }
            viewModelAuth.signIn(login, pass)

            viewModelAuth.dataState.observe(viewLifecycleOwner) { state ->
                if (state.successes) {
                    findNavController().navigateUp()
                } else Toast.makeText(context, "Incorrect login or pass", LENGTH_LONG).show()
            }

        }
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}