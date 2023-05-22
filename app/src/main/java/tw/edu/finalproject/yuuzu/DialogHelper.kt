package tw.edu.finalproject.yuuzu

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tw.edu.finalproject.R

class DialogHelper(
    private val activity: Activity
) {
    private val isNightMode = Configuration.UI_MODE_NIGHT_MASK and activity.resources.configuration.uiMode

    private var isLoading = false
    private var loadingSweet: SweetAlertDialog? = null

    @SuppressLint("MissingInflatedId")
    fun inputDialog(title: String, hint: String, customTextListener: CustomTextListener) {
        val textInputView = activity.layoutInflater.inflate(R.layout.dialog_text_input_1, null, false)
        val outline = textInputView.findViewById<TextInputLayout>(R.id.textOutline_01)
        val inputText = textInputView.findViewById<TextInputEditText>(R.id.textInput_01)
        val textInputDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(textInputView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton("確認", null)
            .setNegativeButton("取消", null)
            .create()

        outline.hint = hint

        textInputDialog.setOnShowListener {
            val positiveButton = textInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = textInputDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setOnClickListener {
                customTextListener.onPositiveTextClick(it, textInputDialog, inputText)
            }
            negativeButton.setOnClickListener {
                customTextListener.onNegativeTextClick(it, textInputDialog)
            }
        }

        textInputDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun inputDialog(title: String, hint1: String, hint2: String, customText2Listener: CustomText2Listener) {
        val textInputView = activity.layoutInflater.inflate(R.layout.dialog_text_input_1, null, false)
        val outline01 = textInputView.findViewById<TextInputLayout>(R.id.textOutline_01_01)
        val inputText01 = textInputView.findViewById<TextInputEditText>(R.id.textInput_01_01)
        val outline02 = textInputView.findViewById<TextInputLayout>(R.id.textOutline_01_02)
        val inputText02 = textInputView.findViewById<TextInputEditText>(R.id.textInput_01_02)
        val textInputDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(textInputView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton("確認", null)
            .setNegativeButton("取消", null)
            .create()

        outline01.hint = hint1
        outline02.hint = hint2

        textInputDialog.setOnShowListener {
            val positiveButton = textInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = textInputDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setOnClickListener {
                customText2Listener.onPositiveTextClick(it, textInputDialog, inputText01, inputText02)
            }
            negativeButton.setOnClickListener {
                customText2Listener.onNegativeTextClick(it, textInputDialog)
            }
        }

        textInputDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun dropDownDialog(title: String, hint: String, autoTextAdapter: ArrayAdapter<String>, customDropInputListener: CustomDropInputListener, onItemClickListener: OnItemClickListener) {
        val dropdownView = activity.layoutInflater.inflate(R.layout.dialog_input_dropdown, null, false)
        val autoText = dropdownView.findViewById<AutoCompleteTextView>(R.id.dropDownInput)
        val outline = dropdownView.findViewById<TextInputLayout>(R.id.DropInputOutline)
        val inputText = dropdownView.findViewById<TextInputEditText>(R.id.dropInputText)
        val dropDownDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(dropdownView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton("確認", null)
            .setNegativeButton("取消") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .create()

        outline.hint = hint

        autoText.setAdapter(autoTextAdapter)
        autoText.setOnItemClickListener{ adapterView, view, i, l ->
            onItemClickListener.onItemClick(adapterView, view, i, l)
        }

        dropDownDialog.setOnShowListener {
            val positiveButton = dropDownDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener{
                customDropInputListener.onPositiveClick(it, dropDownDialog, inputText)
            }
        }

        dropDownDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun dropDownDialog(title: String, autoTextAdapter: ArrayAdapter<String>, customPositiveListener: CustomPositiveListener, onItemClickListener: OnItemClickListener) {
        val dropdownView = activity.layoutInflater.inflate(R.layout.dialog_dropdown, null, false)
        val autoText = dropdownView.findViewById<AutoCompleteTextView>(R.id.customDropDown)
        val dropDownDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(dropdownView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton("確認", null)
            .setNegativeButton("取消") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .create()

        autoText.setAdapter(autoTextAdapter)
        autoText.setOnItemClickListener{ adapterView, view, i, l ->
            onItemClickListener.onItemClick(adapterView, view, i, l)
        }

        dropDownDialog.setOnShowListener {
            val positiveButton = dropDownDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener{
                customPositiveListener.onPositiveClick(it, dropDownDialog)
            }
        }

        dropDownDialog.show()
    }

    fun showLoadingDialog(text: String, cancelable: Boolean) {
        if (isLoading) return
        isLoading = true
        loadingSweet = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE).apply {
            when (isNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    progressHelper.barColor = context.getColor(R.color.md_theme_light_primary)
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    progressHelper.barColor = context.getColor(R.color.md_theme_dark_primary)
                }
            }
            titleText = text
            setCancelable(cancelable)
            show()
        }
    }

    fun hideLoadingDialog() {
        if (!isLoading) return
        isLoading = false
        loadingSweet?.dismiss()
    }

    /**
     * @param alertType
     * NORMAL_TYPE = 0
     * ERROR_TYPE = 1
     * SUCCESS_TYPE = 2
     * WARNING_TYPE = 3
     * CUSTOM_IMAGE_TYPE = 4
     * PROGRESS_TYPE = 5
     */
    fun alertDialog(title: String, message: String, alertType: Int, cancelable: Boolean) {
        SweetAlertDialog(activity, alertType).apply {
            titleText = title
            contentText = message
            setCancelable(cancelable)
            show()
        }
    }

    /**
     * @param alertType
     * NORMAL_TYPE = 0
     * ERROR_TYPE = 1
     * SUCCESS_TYPE = 2
     * WARNING_TYPE = 3
     * CUSTOM_IMAGE_TYPE = 4
     * PROGRESS_TYPE = 5
     */
    fun alertDialog(title: String, message: String, alertType: Int, cancelable: Boolean, posBtn: String, dialogPositiveListener: DialogPositiveListener) {
        SweetAlertDialog(activity, alertType).apply {
            titleText = title
            contentText = message
            setCancelable(cancelable)
            setConfirmButton(posBtn) { sDialog ->
                dialogPositiveListener.onPositive(sDialog)
            }
            show()
        }
    }

    /**
     * @param alertType
     * NORMAL_TYPE = 0
     * ERROR_TYPE = 1
     * SUCCESS_TYPE = 2
     * WARNING_TYPE = 3
     * CUSTOM_IMAGE_TYPE = 4
     * PROGRESS_TYPE = 5
     */
    fun alertDialog(title: String, message: String, alertType: Int, cancelable: Boolean, posBtn: String, negBtn: String, dialogListener: DialogListener) {
        SweetAlertDialog(activity, alertType).apply {
            titleText = title
            contentText = message
            setCancelable(cancelable)
            setConfirmButton(posBtn) { sDialog ->
                dialogListener.onPositive(sDialog)
            }
            setCancelButton(negBtn) { sDialog ->
                dialogListener.onNegative(sDialog)
            }
            show()
        }
    }

    interface CustomPositiveListener {
        fun onPositiveClick(var1: View, dropDownDialog: androidx.appcompat.app.AlertDialog)
    }

    interface OnItemClickListener {
        fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long)
    }

    interface CustomDropInputListener {
        fun onPositiveClick(var1: View, dropDownDialog: androidx.appcompat.app.AlertDialog, inputText: TextInputEditText)
    }

    interface CustomTextListener {
        fun onPositiveTextClick(
            var1: View,
            dialog: androidx.appcompat.app.AlertDialog,
            inputText: TextInputEditText
        )

        fun onNegativeTextClick(
            var1: View,
            dialog: androidx.appcompat.app.AlertDialog,
        )
    }

    interface CustomText2Listener {
        fun onPositiveTextClick(
            var1: View,
            dialog: androidx.appcompat.app.AlertDialog,
            inputText1: TextInputEditText,
            inputText2: TextInputEditText
        )

        fun onNegativeTextClick(
            var1: View,
            dialog: androidx.appcompat.app.AlertDialog,
        )
    }

    interface DialogPositiveListener {
        fun onPositive(dialog: SweetAlertDialog)
    }

    interface DialogListener {
        fun onPositive(dialog: SweetAlertDialog)
        fun onNegative(dialog: SweetAlertDialog)
    }
}