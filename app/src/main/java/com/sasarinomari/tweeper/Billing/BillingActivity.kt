package com.sasarinomari.tweeper.Billing

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_default.view.*
import kotlinx.android.synthetic.main.item_sku.view.*
import java.text.SimpleDateFormat
import java.util.*


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


        val purchaseListingDetails: ArrayList<SkuDetails> = ArrayList(bp.getPurchaseListingDetails(arrayListOf(
            DonationItems.donate1000.name,
            DonationItems.donate2000.name,
            DonationItems.donate5000.name,
            DonationItems.donate8000.name,
            DonationItems.donate13000.name
        )).sortedWith(compareBy { it.priceLong }))


        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.Donate)
                view.title_description.text = getString(R.string.DonateDesc)

            }
       })

        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_sku, purchaseListingDetails) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as SkuDetails
                view.skuitem_title.text = item.title.removeSuffix(getString(R.string.skuPostfix))
                view.skuitem_description.text = item.description
                view.skuitem_price.text = item.priceText

                view.setOnClickListener {
                    bp.purchase(this@BillingActivity, item.productId)
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.defaultitem_title.text = "구매 내역 복원하기"
                view.defaultitem_description.text = "기존 구매 이력을 불러와요. 추가 과금은 되지 않아요!"

                view.setOnClickListener {
                    restore()
                }
            }
        })

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    private fun restore() {
        TODO("Not yet implemented")
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