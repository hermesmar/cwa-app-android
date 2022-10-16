package de.rki.coronawarnapp.ui.settings.start

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.util.Locale
import javax.inject.Inject

/**
 * This is the setting overview page.
 */
class SettingsFragment : Fragment(R.layout.fragment_settings), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingState.observe2(this) {
            binding.tracingState = it
        }
        vm.notificationSettingsState.observe2(this) {
            binding.notificationState = it
        }
        vm.backgroundPriorityState.observe2(this) {
            binding.backgroundState = it
        }

        vm.analyticsState.observe2(this) {
            binding.analyticsState = it
        }

        val languages = BuildConfig.SUPPORTED_LOCALES.map { it.displayName }.toTypedArray()
        binding.settingsAppLanguage.root.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_app_language)
                .setCancelable(false)
                .setSingleChoiceItems(
                    languages,
                    BuildConfig.SUPPORTED_LOCALES.indexOf(getApplicationLocales().toLanguageTags().langTag)
                ) { _, which ->
                    setApplicationLocales(LocaleListCompat.forLanguageTags(BuildConfig.SUPPORTED_LOCALES[which]))
                    binding.settingsAppLanguage.statusText = getApplicationLocales().toLanguageTags().displayName
                }
                .setNegativeButton(R.string.onboarding_button_cancel) { _, _ -> }
                .show()
        }

        binding.settingsAppLanguage.statusText = getApplicationLocales().toLanguageTags().displayName

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()

        binding.settingsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        val tracingRow = binding.settingsTracing.settingsRow
        val notificationRow = binding.settingsNotifications.settingsRow
        val backgroundPriorityRow = binding.settingsBackgroundPriority.settingsRow
        val privacyPreservingAnalyticsRow = binding.settingsPrivacyPreservingAnalytics.settingsRow
        val resetRow = binding.settingsReset
        resetRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsResetFragment()
            )
        }
        tracingRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsTracingFragment()
            )
        }
        notificationRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsNotificationFragment()
            )
        }
        backgroundPriorityRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsBackgroundPriorityFragment()
            )
        }

        privacyPreservingAnalyticsRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsPrivacyPreservingAnalyticsFragment()
            )
        }

        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }

    private val String.displayName get() = Locale(langTag).let { it.getDisplayLanguage(it) }

    private val String.langTag get() = split("-").getOrElse(0) { "de" }
}
