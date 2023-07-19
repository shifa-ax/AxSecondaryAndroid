package com.ax.axsecondaryapp.model

data class TodoResponseModel(
    var completed: Boolean?,
    var id: Int?,
    var title: String?,
    var userId: Int?
)