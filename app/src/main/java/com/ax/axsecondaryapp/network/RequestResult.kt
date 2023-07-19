package com.ax.axsecondaryapp.network

data class RequestResult<out T>(
  val status: Boolean,
  val code: Int,
  val message: String?,
  val data: T?
) {
  companion object {
    fun <T> success(data: T): RequestResult<T> {
      return RequestResult(true, 200, null, data)
    }

    fun <T> error(message: String, code: Int, data: T? = null): RequestResult<T> {
      return RequestResult(false, code, message, data)
    }

    fun <T> loading(data: T? = null): RequestResult<T> {
      return RequestResult(false, 0, null, data)
    }
  }
}
