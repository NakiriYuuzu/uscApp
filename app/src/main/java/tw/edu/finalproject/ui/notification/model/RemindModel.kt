package tw.edu.finalproject.ui.notification.model

data class RemindModel(
    val careReceiverList: List<CareReceiver>?,
    val remindFormat: List<String>
)