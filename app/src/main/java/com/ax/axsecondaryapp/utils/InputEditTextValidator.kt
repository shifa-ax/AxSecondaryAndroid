package com.ax.axsecondaryapp.utils
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ax.axsecondaryapp.BR

class InputEditTextValidator(
  var type: InputEditTextValidationsEnum,
  isMandatory: Boolean,
  callBack: InputEditTextValidatorCallBack?,
  vararg error_msg: String?
) : BaseObservable() {
  private val errorMsg: Array<String>? = error_msg as Array<String>
  private var value = ""
  private val isMandatory: Boolean

  @get:Bindable
  var isError = true
    private set
  private var editText: AppCompatEditText? = null

  //  private var inputLayout: TextView? = null
  private val callBack: InputEditTextValidatorCallBack?
  private fun reset() {
    setError(false)
//    if (inputLayout != null) inputLayout!!.error = null
//    inputLayout?.visibility = View.GONE
//    setBackground(R.drawable.bg_et_error)
  }

  fun validate(): Boolean {
    reset()
    if (isMandatory) {
      if (value.isEmpty()) {
        isError = true
        errorMsg?.get(0)?.let { setError(it) }
      }
    }

//    when(type){
//      InputEditTextValidationsEnum.EMAIL ->{}
//    }
    if (type == InputEditTextValidationsEnum.EMAIL) {
      if (!ValidationUtils.validateEmailAddress(
          value
        )
      ) {
        isError = true
        errorMsg?.get(0)?.let { setError(it) }
      }
    } else if (type == InputEditTextValidationsEnum.PASSWORD) {
      if (value.length < 5) {
        isError = true
        errorMsg?.get(0)?.let { setError(it) }
      }
    } else if (type == InputEditTextValidationsEnum.USERNAME) {
      if (value.length < 2) {
        isError = true
        errorMsg?.get(0)?.let { setError(it) }
      }
    } else if (type == InputEditTextValidationsEnum.FIELD) {
      if (value.isEmpty()) {
        isError = true

      }
    } else if (type == InputEditTextValidationsEnum.IBT_NUMBER) {
      if (value.isEmpty() || value.length < 6 || value.length > 6) {
        isError = true

      }
    } else if (type == InputEditTextValidationsEnum.ID) {
      if (value.length < 4) {
        isError = true
      }
    } else if (type == InputEditTextValidationsEnum.WEBSITE) {

    } else if (type == InputEditTextValidationsEnum.AMOUNT) {
      if (Utils.isNumberString(value)) {
        if (value.length < 2) {
          isError = true
          errorMsg?.get(0)?.let { setError(it) }
        }
      } else {
        isError = true
        errorMsg?.get(0)?.let { setError(it) }
      }


    }

    return isError
  } // Do nothing.

  // Do nothing.
  @get:Bindable
  val textWatcher: TextWatcher
    get() = object : TextWatcher {
      override fun beforeTextChanged(
        s: CharSequence,
        start: Int,
        count: Int,
        after: Int
      ) {
        // Do nothing.
      }

      override fun onTextChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
      ) {
      }

      override fun afterTextChanged(s: Editable) {
        // Do nothing.
        validate()
        callBack?.onValueChange(this@InputEditTextValidator)
      }
    }

  fun setEditText(editText: AppCompatEditText?) {
    this.editText = editText
//    this.inputLayout = errorField
  }

  fun setError(message: String) {
    errorText = message
    setError(true)

//    inputLayout?.let {
//      inputLayout?.text = errorText
//      inputLayout?.visibility = View.VISIBLE
//    }
//      setBackground(R.drawable.bg_rect_5c)

  }

  @get:Bindable
  var errorText: String?
    get() = errorMsg?.get(0)
    set(errorText) {
      if (errorText != null) {
        errorMsg?.set(0, errorText)
      }
      notifyPropertyChanged(BR.errorText)
    }

  @Bindable
  fun getValue(): String {
    return value
  }

  fun setValue(value: String?) {
    if (value != null) {
      this.value = value
    }
    notifyPropertyChanged(BR._all)
    validate()
  }
  val length: Int
    get() = value.length

  fun setError(error: Boolean) {
    isError = error
    notifyPropertyChanged(BR.errorText)
  }

  init {
    this.isMandatory = isMandatory
    this.callBack = callBack
  }

  private fun setBackground(id: Int) {
    if (editText != null) {
      val pL = editText!!.paddingLeft
      val pT = editText!!.paddingTop
      val pR = editText!!.paddingRight
      val pB = editText!!.paddingBottom
      editText?.let {
        it.setBackgroundResource(id)
        it.setPadding(pL, pT, pR, pB)
      }
    }
  }

  interface InputEditTextValidatorCallBack {
    fun onValueChange(validator: InputEditTextValidator?)
  }

  enum class InputEditTextValidationsEnum(var value: String) {
    EMAIL("email"),
    AMOUNT("amount"),
    NUMBER("number"),
    USERNAME("username"),
    FIELD("field"),
    IBT_NUMBER("ibtNumber"),
    PASSWORD("password"),
    ID("ID"),
    CONFIRM_PASSWORD("password"),
    NON("NON"),
    WEBSITE("website"),
  }
}