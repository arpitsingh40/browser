package com.mercandalli.android.libs.monetization.on_boarding

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.mercandalli.android.libs.monetization.MonetizationManager
import com.mercandalli.android.libs.monetization.in_app.InAppManager

internal class OnBoardingPresenter(
        private val screen: OnBoardingContract.Screen,
        private val onBoardingRepository: OnBoardingRepository,
        private val monetizationManager: MonetizationManager,
        private val inAppManager: InAppManager,
        private val subscriptionSku: String
) : OnBoardingContract.UserAction {

    private val monetizationEnabledListener = createMonetizationEnabledListener()
    private val inAppManagerListener = createInAppManagerListener()

    override fun onAttached() {
        monetizationManager.registerMonetizationListener(monetizationEnabledListener)
        syncScreen()
    }

    override fun onDetached() {
        monetizationManager.unregisterMonetizationListener(monetizationEnabledListener)
        inAppManager.unregisterListener(inAppManagerListener)
    }

    override fun onPageChanged() {
        syncScreen()
    }

    override fun onNextClicked() {
        val lastPage = isLastPage()
        if (lastPage) {
            onBoardingRepository.markOnBoardingEnded()
            screen.closeOnBoarding()
            screen.startFistActivity()
            return
        }
        val page = screen.getPage()
        screen.setPage(page + 1)
    }

    override fun onStoreBuyClicked(activityContainer: InAppManager.ActivityContainer) {
        inAppManager.registerListener(inAppManagerListener)
        inAppManager.purchase(activityContainer, subscriptionSku, BillingClient.SkuType.SUBS)
    }

    override fun onStoreSkipClicked() {
        onBoardingRepository.markOnBoardingStorePageSkipped()
        onBoardingRepository.markOnBoardingEnded()
        screen.closeOnBoarding()
        screen.startFistActivity()
    }

    private fun syncScreen(
            onBoardingStorePageAvailable: Boolean = monetizationManager.isOnBoardingStorePageAvailable()
    ) {
        if (onBoardingStorePageAvailable) {
            screen.enableStorePage()
        } else {
            screen.disableStorePage()
        }
        val lastPage = isLastPage()
        if (lastPage && onBoardingStorePageAvailable) {
            inAppManager.initialize()
            screen.hideNextButton()
            screen.showStoreButtons()
        } else {
            screen.showNextButton()
            screen.hideStoreButtons()
        }
    }

    private fun isLastPage(): Boolean {
        val page = screen.getPage()
        val pageCount = screen.getPageCount()
        return page == pageCount - 1
    }

    private fun createMonetizationEnabledListener() = object : MonetizationManager.MonetizationListener {
        override fun onOnBoardingStorePageAvailableChanged() {
            syncScreen()
        }
    }

    private fun createInAppManagerListener() = object : InAppManager.Listener {
        override fun onSkuDetailsChanged(skuDetails: SkuDetails) {}
        override fun onPurchasedChanged() {
            if (inAppManager.isPurchased(subscriptionSku)) {
                onBoardingRepository.markOnBoardingEnded()
                screen.closeOnBoarding()
                screen.startFistActivity()
            }
        }
    }
}