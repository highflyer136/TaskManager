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
import com.taskmanager.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (validateInput(email, password)) {
                viewModel.signInWithEmail(email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            (activity as? AuthActivity)?.launchGoogleSignIn()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.sendPasswordReset(email)
            } else {
                binding.etEmail.error = getString(R.string.error_email_required)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var valid = true
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_required)
            valid = false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_required)
            valid = false
        }
        return valid
    }

    private fun observeState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is AuthState.Loading
            binding.btnLogin.isEnabled = state !is AuthState.Loading
            binding.btnGoogleSignIn.isEnabled = state !is AuthState.Loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
