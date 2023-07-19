package com.ax.axsecondaryapp.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.base.BaseFragment
import com.ax.axsecondaryapp.databinding.Fragment2Binding
import com.ax.axsecondaryapp.db.SharedPreferencesManager
import com.ax.axsecondaryapp.viewmodel.UserViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Fragment2 : BaseFragment(), CLickFragment2 {
    private val viewModel: UserViewModel by activityViewModels()
    lateinit var binding: Fragment2Binding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_2, container, false
        )
        iniUI()
        return binding.root
    }

    private fun iniUI() {
        binding.clicka = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        binding.emailTextObserver = viewModel.emailTextObserver
        binding.passwordTextObserver = viewModel.passwordTextObserver
        viewModel.emailTextObserver.setEditText(binding.edEmail)
        viewModel.passwordTextObserver.setEditText(binding.edPass)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
//        firebaseAnalytics = (requireActivity() as MainActivity).firebaseAnalytics



    }

//   override fun onClickGotoFragment1() {
//        findNavController().navigateUp()
////    }

    override fun onClickContinue() {
        val email = binding.edEmail.text.toString().trim()
        val password = binding.edPass.text.toString().trim()
        Log.e(TAG, "onClickContinue: $email $password")

        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= 6
        viewModel.getNetworkStatusLiveData().observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                // Internet connection is available
                // Perform your actions here
                if (isEmailValid && isPasswordValid) {
                    viewModel.getEmail(email, password).observe(this) { result ->
                        result?.let {
                            when (result.status) {
                                true -> {
                                    viewModel.loading.value = false
                                    viewModel.text.value = "Success"
                                    // Handle the successful response
                                    val data = result.data
                                    sharedPreferencesManager.saveToken(result.data?.data?.token.toString())
                                    sharedPreferencesManager.saveUser(result.data?.data?.user?.name.toString())
                                    result.data?.data?.user?.id?.let { it1 ->
                                        sharedPreferencesManager.saveUserId(
                                            it1
                                        )
                                    }
                               var userName = sharedPreferencesManager.getUserName()
                               var userId = sharedPreferencesManager.getUserId()
                               //custom
                                    val loginbundle = Bundle().apply {
                                        putInt(FirebaseAnalytics.Param.ITEM_ID, userId)
                                        putString(FirebaseAnalytics.Param.ITEM_NAME, userName)
                                        // Add more parameters if needed
                                    }

                                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, loginbundle)

                                    //1.Firebase Analytics
                                    val loginBundle = Bundle().apply {
                                        putInt("USER ID", userId)
                                        putString("USER NAME", userName)
                                    }
                                    firebaseAnalytics.logEvent(FirebaseTags.LOGGED_IN.name, loginBundle)

                                    viewModel.agentName = result.data?.data?.user?.name.toString()

                                    if (!result.data?.data?.token.isNullOrEmpty()) {
                                        findNavController().navigate(R.id.actionFragment2ToNavHome)

                                    }

                                }
                                false -> {
                                    viewModel.loading.value = false
                                    viewModel.text.value = "Error"
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "Error Loging In",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                        viewModel.textObservable.setValue("Error")
                                    // Handle the error response
                                    // ...
                                }
                            }
                        }
                    }
                } else {
                    if (!isEmailValid) {
                        binding.edEmail.error = "Invalid email address"
                        Toast.makeText(
                            requireContext(),
                            "Invalid email address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (!isPasswordValid) {
                        binding.edPass.error = "Password must be at least 6 characters long"
                        Toast.makeText(
                            requireContext(),
                            "Password Incorrect",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}