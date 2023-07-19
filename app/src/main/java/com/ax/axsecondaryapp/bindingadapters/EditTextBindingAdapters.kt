package com.ax.axsecondaryapp.bindingadapters

import android.annotation.SuppressLint
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
//for cutome edittext
object EditTextBindingAdapters {
    @JvmStatic
    @BindingAdapter("textChangedListener")
    fun bindTextWatcher(editText: EditText, textWatcher: TextWatcher?) {
        editText.addTextChangedListener(textWatcher)
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter("android:text")
    fun setText(view: TextView, value: Int) {
        view.text = Integer.toString(value)
    }
}