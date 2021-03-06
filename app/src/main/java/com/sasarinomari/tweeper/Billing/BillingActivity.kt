package com.sasarinomari.tweeper.Billing

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.TwitterAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_sasarinomari.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_default.view.*
import kotlinx.android.synthetic.main.item_sku.view.*
import java.util.*


open class BillingActivity : BaseActivity(), BillingProcessor.IBillingHandler {
    private lateinit var bp: BillingProcessor

    private enum class DonationItems {
        donate1000, donate2000, donate5000, donate8000, donate13000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bp = BillingProcessor(this, getString(R.string.gp_license), this)
        bp.initialize()
    }

    // Region Billing Interface
    override fun onBillingInitialized() {
        setContentView(R.layout.full_recycler_view)

        var purchaseListingDetails: ArrayList<SkuDetails>? = null
        try {
            purchaseListingDetails = ArrayList(bp.getPurchaseListingDetails(arrayListOf(
                DonationItems.donate1000.name,
                DonationItems.donate2000.name,
                DonationItems.donate5000.name,
                DonationItems.donate8000.name
            )).sortedWith(compareBy { it.priceLong }))
        } catch (ie: IllegalStateException) {
            ie.printStackTrace()
            da.error(getString(R.string.Error), getString(R.string.BillingInfoFetchFailed)) {
                finish()
            }.show()
        }
        if(purchaseListingDetails == null) return

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.Donate)
                view.title_description.text = getString(R.string.DonateDesc)
            }
       })
//        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_sasarinomari) {
//            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
//                view.sasarino_profileImage.background = ShapeDrawable(OvalShape())
//                view.sasarino_profileImage.clipToOutline = true
//                view.sasarino_id.setOnClickListener {
//                    TwitterAdapter.showProfile(this@BillingActivity, "SasarinoMARi")
//                }
//                lookupSelf(view.sasarino_profileImage)
//            }
//        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_sku, purchaseListingDetails) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as SkuDetails
                view.skuitem_title.text = item.title.removeSuffix(getString(R.string.skuPostfix))
                view.skuitem_description.text = item.description
                view.skuitem_price.text = item.priceText
                if(bp.isPurchased(item.productId)) {
                    view.skuitem_paid.visibility = View.VISIBLE
                    view.setBackgroundColor(ContextCompat.getColor(this@BillingActivity, R.color.grayBackground))
                }
            }

            override fun onClickListItem(item: Any?) {
                item as SkuDetails
                if(!bp.isPurchased(item.productId)) {
                    bp.purchase(this@BillingActivity, item.productId)
                }
            }
        })
        adapter.addSpace(5)
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.defaultitem_title.text = getString(R.string.RestorePayment)
                view.defaultitem_description.text = getString(R.string.RestorePaymentDesc)

                view.setOnClickListener {
                    restore()
                }
            }
        })
        adapter.addSpace(5)

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    private fun lookupSelf(view: ImageView) {
        Thread {
            TwitterAdapter().initialize(this, AuthData.Recorder(this@BillingActivity).getFocusedUser()!!.token!!).lookup("SasarinoMARi", object : TwitterAdapter.FoundObjectInterface {
                override fun onStart() { }

                override fun onFinished(obj: Any) {
                    val me = obj as twitter4j.User
                    runOnUiThread {
                        Picasso.get()
                            .load(me.profileImageURLHttps.replace("normal.jpg", "200x200.jpg"))
                            .into(view.sasarino_profileImage)
                    }
                }

                override fun onNotFound() { }

                override fun onRateLimit() {
                    this@BillingActivity.onRateLimit("lookup")
                }

                override fun onUncaughtError() {
                    this@BillingActivity.onUncaughtError()
                }

                override fun onNetworkError(retry: () -> Unit) {
                    this@BillingActivity.onNetworkError{
                        lookupSelf(view)
                    }
                }
            })
        }.start()
    }

    /**
     * 이 함수를 호출하는게 효과가 있을지는 모르겠으나
     * 만약의 경우에 대비해서 있는게 좋을 거라 판단함.
     */
    private fun restore() {
        val items = arrayListOf(
            DonationItems.donate1000.name,
            DonationItems.donate2000.name,
            DonationItems.donate5000.name,
            DonationItems.donate8000.name,
            DonationItems.donate13000.name
        )

        for(skuId in items) {
            if(bp.isPurchased(skuId)) {
                onPurchaseHistoryRestored()
                return
            }
        }
    }

    override fun onPurchaseHistoryRestored() {
        /*
        * Called when purchase history was restored and the list of all owned PRODUCT ID's
        * was loaded from Google Play
        */
        AdRemover(this).removeAd()
        setResult(RESULT_OK)
        da.message(getString(R.string.Notice), getString(R.string.PurchaseHistoryRestored)).show()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        /*
        * Called when requested PRODUCT ID was successfully purchased
        */
        AdRemover(this).removeAd()
        setResult(RESULT_OK)
        da.message(getString(R.string.Thanks), getString(R.string.Purchased)).show()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        /*
        * Called when some error occurred. See Constants class for more details
        *
        * Note - this includes handling the case where the user canceled the buy dialog:
        * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
        */
        if(errorCode != 1) {
            da.message(getString(R.string.Error), getString(R.string.PaymentFailed)).show()
        }
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