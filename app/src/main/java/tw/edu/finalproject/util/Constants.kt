package tw.edu.finalproject.util

object Constants {

    /** API **/
//    private const val API_BASE_URL = "http://120.110.115.146:5000/"
    private const val API_BASE_URL = "http://192.168.100.4:5000/"
    const val API_LOGIN = "${API_BASE_URL}api/login/"
    const val API_REGISTER = "${API_BASE_URL}api/register/"
    const val API_USER_DETAIL = "${API_BASE_URL}api/user/"
    const val API_USER_GROUP = "${API_BASE_URL}api/user_group/"
    const val API_GROUP_LIST = "${API_BASE_URL}api/group_list/"
    const val API_REMIND_ONE = "${API_BASE_URL}api/remind_one/"
    const val API_REMIND = "${API_BASE_URL}api/remind/"
    const val API_SEND_REMIND = "${API_BASE_URL}api/send_remind/"
    const val API_REMIND_FORMAT = "${API_BASE_URL}api/remind_format/"

//    const val WS_NOTIFY = "ws://120.110.115.146:5000/ws/remind/"
    const val WS_NOTIFY = "ws://192.168.100.4:5000/ws/remind/"

//    const val JETSON_UPLOAD = "http://120.110.115.130:8000/upload_file"
    const val JETSON_UPLOAD = "http://192.168.100.3:8000/upload_file"

    const val RTSP_URI = "rtsp://root:awedvhu0808@192.168.100.7:554/axis-media/media.amp"
}