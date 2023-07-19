package com.ax.axsecondaryapp.network
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

abstract class BaseDataSource {
  protected suspend fun <T> getResult(
    call: suspend () -> Response<T?>
  ): RequestResult<T?> {
    try {
      val response = call()
      if (response.isSuccessful && response.code() < 400) {
        val body = response.body()
        return RequestResult.success(body)
      }

      val errorBody = JSONObject(
        response.errorBody()!!.charStream().readText()
      )

      val errorMessage = if (errorBody.has("message")) {
        errorBody.getString("message")
      } else {
        "Something went wrong. Please try again later."
      }

      return RequestResult.error(errorMessage, response.code())

    } catch (e: Exception) {
      return RequestResult.error(e.message ?: e.toString(), 0)
    }
  }

  private fun <T> error(message: String): RequestResult<T> {
    return RequestResult.error(message, 0)
  }


  protected suspend fun <T> getResultNew(
    call: suspend () -> Response<T>
  ): RequestResult<T?> {
    try {
      val response = call()

      if (response.isSuccessful && response.code() < 400) {
        val body = response.body()
        return RequestResult.success(body)
      } else {
        val errorBody = response.errorBody()?.charStream()?.readText()
        val errorMessage = if (errorBody != null) {
          try {
            val errorJson = JSONObject(errorBody)
            if (errorJson.has("message")) {
              errorJson.getString("message")
            } else {
              "Something went wrong. Please try again later."
            }
          } catch (e: JSONException) {
            "Something went wrong. Please try again later."
          }
        } else {
          "Something went wrong. Please try again later."
        }

        return RequestResult.error(errorMessage, response.code())
      }
    } catch (e: Exception) {
      return RequestResult.error(e.message ?: e.toString(), 0)
    }
  }


}

