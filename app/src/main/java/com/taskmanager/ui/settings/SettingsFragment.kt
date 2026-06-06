package com.taskmanager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.taskmanager.R
import com.taskmanager.databinding.FragmentSettingsBinding
import com.taskmanager.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var messaging: FirebaseMessaging

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show user info
        auth.currentUser?.let { user ->
            binding.tvUserEmail.text = user.email
            binding.tvUserName.text = user.displayName ?: getString(R.string.default_user_name)
        }

        // Language switch
        binding.switchLanguage.apply {
            val prefs = requireContext().getSharedPreferences("settings", 0)
            val savedLang = prefs.getString("language", "en")
            isChecked = savedLang == "mk"
            text = if (isChecked) getString(R.string.language_macedonian) else getString(R.string.language_english)

            setOnCheckedChangeListener { _, checked ->
                val langCode = if (checked) "mk" else "en"
                prefs.edit().putString("language", langCode).apply()

                val locale = Locale(langCode)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)

                requireActivity().recreate()
            }
        }

        // Dark mode switch
        binding.switchDarkMode.apply {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES

            setOnCheckedChangeListener { _, checked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (checked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // Notifications toggle
        binding.switchNotifications.apply {
            isChecked = true
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    messaging.subscribeToTopic("task_reminders")
                } else {
                    messaging.unsubscribeFromTopic("task_reminders")
                }
            }
        }

        // Subscribe to notifications by default
        messaging.subscribeToTopic("task_updates")

        // Sign out
        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
