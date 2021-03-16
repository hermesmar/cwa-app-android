package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsFragmentBinding
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class CheckInsFragment : Fragment(R.layout.trace_location_attendee_checkins_fragment), AutoInject {

    private val navArgs by navArgs<CheckInsFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CheckInsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as CheckInsViewModel.Factory
            factory.create(savedState = savedState, deepLink = navArgs.uri)
        }
    )
    private val binding: TraceLocationAttendeeCheckinsFragmentBinding by viewBindingLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu(binding.toolbar)

        binding.scanCheckinQrcodeFab.apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_checkInsFragment_to_scanCheckInQrCodeFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(this to transitionName)
                )
            }
            if (CWADebug.isDeviceForTestersBuild) {
                setOnLongClickListener {
                    findNavController().navigate(createCheckInUri(DEBUG_CHECKINS.random()))
                    true
                }
            }
        }

        viewModel.verifyResult.observe2(this) {
            doNavigate(
                CheckInsFragmentDirections
                    .actionCheckInsFragmentToConfirmCheckInFragment(it.toVerifiedTraceLocation())
            )
        }
    }

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_trace_location_my_check_ins)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> {
                    Toast.makeText(requireContext(), "Information // TODO", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_remove_all -> {
                    Toast.makeText(requireContext(), "Remove all // TODO", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }

    companion object {
        fun createCheckInUri(rootUri: String): Uri = "coronawarnapp://check-ins/$rootUri".toUri()

        @Suppress("MaxLineLength")
        private val DEBUG_CHECKINS = listOf(
            "https://e.coronawarn.app/c1/BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESIGBDAEIIARVENF6QT6XZATJ5GSDHL77BCAGR6QKDEUJRP2RDCTKTS7QECWMFAEIIA47MT2EA7MQKGNQU2XCY3Y2ZOZXCILDPC65PBUO4JJHT5LQQWDQSA"
        )
    }
}
