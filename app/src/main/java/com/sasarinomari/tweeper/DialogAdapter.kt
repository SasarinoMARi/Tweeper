package com.sasarinomari.tweeper

import android.app.Activity
import android.content.DialogInterface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog

class DialogAdapter(private val activity: Activity) {
    fun message(title: String?, content: String, callback: (()->Unit)? = null): SweetAlertDialog {
        val d = SweetAlertDialog(activity, SweetAlertDialog.NORMAL_TYPE)
        initialize(d, title, content, callback)
        return d
    }

    fun error(title: String?, content: String, callback: (()->Unit)? = null): SweetAlertDialog {
        val d = SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
        initialize(d, title, content, callback)
        return d
    }

    fun warning(title: String?, content: String, callback: (()->Unit)? = null): SweetAlertDialog {
        val d = SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
        initialize(d, title, content, callback)
        return d
    }

    fun progress(title: String?, content: String, callback: (()->Unit)? = null): SweetAlertDialog {
        val d = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        initialize(d, title, content, callback)
        d.setCancelable(false)
        return d
    }

    fun success(title: String?, content: String, callback: (()->Unit)? = null): SweetAlertDialog {
        val d = SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
        initialize(d, title, content, callback)
        return d
    }

    private fun initialize(d: SweetAlertDialog, title: String?, content: String, callback: (()->Unit)? = null) {
        if(title != null) d.titleText = title
        d.contentText = content
        d.setOnShowListener { dialog ->
            dialog as SweetAlertDialog
            val titleView: TextView = dialog.findViewById(R.id.title_text) as TextView
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
            val contentView: TextView = dialog.findViewById(R.id.content_text) as TextView
            contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
//            val confirmView: TextView = dialog.findViewById(R.id.confirm_button) as TextView
//            confirmView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
//            val cancelView: TextView = dialog.findViewById(R.id.cancel_button) as TextView
//            cancelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        }
        d.setOnDismissListener {
            if(callback!=null) callback()
        }
    }

}