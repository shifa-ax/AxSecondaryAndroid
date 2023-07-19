package com.ax.axsecondaryapp.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.base.BaseDialogFragment
import com.ax.axsecondaryapp.databinding.DailogLeadsBinding

import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class LeadsDialog : BaseDialogFragment<DailogLeadsBinding>(),ClickInterfaceDialog{

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUI()
    observers()
    isCancelable = false


  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NO_FRAME, R.style.FullScreenTransDialogTheme)
  }

  private fun initUI() {
    binding.click = this
    binding.lifecycleOwner = this

  }


  private fun observers() {
  }


  override fun getLayoutRes(): Int {
    return R.layout.dailog_leads
  }

  override fun onClickLater() {
    findNavController().navigateUp()
  }

  override fun onClickClaim() {
    TODO("Not yet implemented")
  }

//  override fun onClickView() {
//    findNavController().navigate(R.id.nav_returns)
//  }

}
