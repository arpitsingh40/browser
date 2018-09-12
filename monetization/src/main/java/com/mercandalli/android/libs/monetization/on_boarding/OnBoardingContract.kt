package com.mercandalli.android.libs.monetization.on_boarding

import androidx.annotation.IntRange
import com.mercandalli.android.libs.monetization.in_app.InAppManager

internal interface OnBoardingContract {

    interface UserAction {

        fun onAttached()

        fun onDetached()

        fun onPageChanged()

        fun onNextClicked()

        fun onStoreBuyClicked(activityContainer: InAppManager.ActivityContainer)

        fun onStoreSkipClicked()
    }

    interface Screen {

        @IntRange(from = 0)
        fun getPage(): Int

        fun setPage(@IntRange(from = 0) page: Int)

        @IntRange(from = 0)
        fun getPageCount(): Int

        fun enableStorePage()

        fun disableStorePage()

        fun showNextButton()

        fun hideNextButton()

        fun showStoreButtons()

        fun hideStoreButtons()

        fun closeOnBoarding()

        fun startFistActivity()
    }
}