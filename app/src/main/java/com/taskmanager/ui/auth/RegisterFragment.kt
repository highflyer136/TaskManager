package com.taskmanager.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.taskmanager.R
import com.taskmanager.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            if (validateInput(name, email, password, confirm)) {
                viewModel.registerWithEmail(email, password, name)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirm: String): Boolean {
        var valid = true
        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.error_name_required)
            valid = false
        }
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_required)
            valid = false
        }
        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.error_password_too_short)
            valid = false
        }
        if (password != confirm) {
            binding.etConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            valid = false
        }
        return valid
    }

    private fun observeState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is AuthState.Loading
            binding.btnRegister.isEnabled = state !is AuthState.Loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
