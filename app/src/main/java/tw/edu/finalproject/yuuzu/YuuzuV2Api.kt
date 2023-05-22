package tw.edu.finalproject.yuuzu

import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.AsyncHttpPost
import com.koushikdutta.async.http.AsyncHttpResponse
import com.koushikdutta.async.http.body.MultipartFormDataBody
import java.lang.Exception

class YuuzuV2Api {
    fun api(url: String, getPostListener: GetPostListener) {
        val post = AsyncHttpPost(url)
        val body = getPostListener.body
        post.body = body

        AsyncHttpClient.getDefaultInstance().executeString(post, object : AsyncHttpClient.StringCallback() {
            override fun onCompleted(
                e: Exception?,
                source: AsyncHttpResponse?,
                result: String?
            ) {
                getPostListener.response(e, source, result)
            }
        })
    }


    interface GetPostListener {
        val body: MultipartFormDataBody
        fun response(exception: Exception?, source: AsyncHttpResponse?, result: String?)
    }
}