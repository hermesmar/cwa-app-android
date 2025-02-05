package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.TestCertificateCardBinding
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TestCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<TestCertificateCard.Item, TestCertificateCardBinding>(
        layoutRes = R.layout.test_certificate_card,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.certificate, holder.bindingAdapterPosition) }
    }

    override val viewBinding: Lazy<TestCertificateCardBinding> = lazy {
        TestCertificateCardBinding.bind(itemView)
    }
    override val onBindData: TestCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { boundItem, payloads ->

        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: boundItem

        latestItem?.let { item ->
            val certificate = item.certificate
            root.setOnClickListener { item.onClick() }
            certificateDate.text = context.getString(
                R.string.test_certificate_sampled_on,
                certificate.sampleCollectedAt?.toLocalDateTimeUserTz()
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    ?: certificate.rawCertificate.test.sc
            )

            when {
                // PCR Test
                certificate.isPCRTestCertificate -> R.string.test_certificate_pcr_test_type
                // RAT Test
                else -> R.string.test_certificate_rapid_test_type
            }.also { testCertificateType.setText(it) }

            val bookmarkIcon = if (item.certificate.isDisplayValid)
                item.colorShade.bookmarkIcon else R.drawable.ic_bookmark

            currentCertificateGroup.isVisible = item.isCurrentCertificate
            bookmark.setImageResource(bookmarkIcon)
            val color = when {
                item.certificate.isDisplayValid -> item.colorShade
                else -> PersonColorShade.COLOR_INVALID
            }

            when {
                item.certificate.isDisplayValid -> TestCertificate.icon
                else -> R.drawable.ic_certificate_invalid
            }.also { certificateIcon.setImageResource(it) }

            val background = when {
                item.isCurrentCertificate -> color.currentCertificateBg
                else -> color.defaultCertificateBg
            }
            certificateBg.setImageResource(background)
            notificationBadge.isVisible = item.certificate.hasNotificationBadge
            certificateExpiration.displayExpirationState(item.certificate)

            startValidationCheckButton.apply {
                isActive = certificate.isNotScreened
                isLoading = item.isLoading
                setOnClickListener {
                    item.validateCertificate(certificate.containerId)
                }
            }
        }
    }

    data class Item(
        val certificate: TestCertificate,
        val isCurrentCertificate: Boolean,
        val colorShade: PersonColorShade,
        val isLoading: Boolean = false,
        val onClick: () -> Unit,
        val onSwipeItem: (TestCertificate, Int) -> Unit,
        val validateCertificate: (CertificateContainerId) -> Unit,
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
