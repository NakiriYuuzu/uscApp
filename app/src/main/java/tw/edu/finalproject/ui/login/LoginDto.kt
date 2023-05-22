package tw.edu.finalproject.ui.login

data class LoginDto(
    val user_group_id: String,
    val group_id: String,
    val group_name: String,
    val group_number: String,
    val user_name: String,
    val user_group_auth_id: String,
    val user_group_auth_name: String,
)
