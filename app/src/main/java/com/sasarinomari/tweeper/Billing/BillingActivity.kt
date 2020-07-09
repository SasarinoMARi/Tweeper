package com.sasarinomari.tweeper.Billing

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*

open class BillingActivity : BaseActivity(), BillingProcessor.IBillingHandler {
    private lateinit var bp: BillingProcessor

    private enum class DonationItems {
        donate1000, donate2000, donate5000, donate8000, donate13000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bp = BillingProcessor(this, "YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE", this)
        bp.initialize()
    }

    // Region Billing Interface
    override fun onBillingInitialized() {
        setContentView(R.layout.full_recycler_view)

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.Donate)
                view.title_description.text = getString(R.string.DonateDesc)

                view.setOnClickListener {
                    bp.purchase(this@BillingActivity, DonationItems.donate1000.name)
                }
            }
       })
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    override fun onPurchaseHistoryRestored() {
        /*
        * Called when purchase history was restored and the list of all owned PRODUCT ID's
        * was loaded from Google Play
        */
        da.message("개발 모드 알림", "onPurchaseHistoryRestored")
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        /*
        * Called when requested PRODUCT ID was successfully purchased
        */
        da.message("개발 모드 알림", "onProductPurchased\nproductId: $productId\ndetails: ${details.toString()}")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        /*
        * Called when some error occurred. See Constants class for more details
        *
        * Note - this includes handling the case where the user canceled the buy dialog:
        * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
        */
        da.message("개발 모드 알림", "onBillingError: $errorCode")
    }
    // endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Unit {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    override fun onDestroy() {
        bp.release()
        super.onDestroy()
    }
}