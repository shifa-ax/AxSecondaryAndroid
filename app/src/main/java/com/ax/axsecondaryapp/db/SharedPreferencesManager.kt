package com.ax.axsecondaryapp.db

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun saveFirebaseToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("firebase", token)
        editor.apply()
    }
    fun saveCallFileDurDiff(dur: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("durdiff", dur)
        editor.apply()
    }

    fun getCallFileDurDiff(): Boolean {
        return sharedPreferences.getBoolean("durdiff", false)
    }

    fun savecallAns(ans: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("ans", ans)
        editor.apply()
    }

    fun getCallAns(): Boolean {
        return sharedPreferences.getBoolean("ans", false)
    }

    fun saveLead(lead: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("lead", lead)
        editor.apply()
    }

    fun getLead(): Boolean {
        return sharedPreferences.getBoolean("lead", false)
    }

    fun getFirebaseToken(): String? {
        return sharedPreferences.getString("firebase", null)
    }

    fun saveLastCallpath(callpath: String) {
        val editor = sharedPreferences.edit()
        editor.putString("callpath", callpath)
        editor.apply()
    }
    fun getLastCallpath(): String? {
        return sharedPreferences.getString("callpath", null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("name", null)
    }
    fun saveUser(name: String) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.apply()
    }
    fun saveUserId(userid: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("userid", userid)
        editor.apply()
    }
    fun getUserId(): Int{
        return sharedPreferences.getInt("userid",-1)
    }


    fun clearToken() {
        val editor = sharedPreferences.edit()
        editor.remove("token")
        editor.apply()
    }
}
