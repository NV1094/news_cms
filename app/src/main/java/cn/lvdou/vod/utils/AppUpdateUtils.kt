package cn.lvdou.vod.utils

import android.content.Context
import android.os.Environment
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.download.LogUtils
import cn.lvdou.vod.netservice.VodService
import okhttp3.*
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit


/**
 * Times: 2020/6/9 08:48
 * Author: songjian
 * Description:APP版本更新
 */
object AppUpdateUtils {
    val mHttpClient = OkHttpClient.Builder().hostnameVerifier(AllowAllHostnameVerifier())
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS).build()


    private var isTream: InputStream? = null//输入流;
    private var fos: FileOutputStream? = null//输出流


    fun startDownload2(
            context: Context,
            downloadPath: String
    ) {
        LogUtils.e("下载链接=", downloadPath)

        val build = Retrofit.Builder()
                .client(mHttpClient)
                .baseUrl(ApiConfig.MOGAI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        build.create(VodService::class.java)
                .downloadAPP(downloadPath)
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                        LogUtils.e("下载错误", "错误信息=${t.message}")
                        if (lisenter != null) {
                            lisenter?.downloadFail()
                        }
                    }

                    override fun onResponse(
                            call: retrofit2.Call<ResponseBody>,
                            response: retrofit2.Response<ResponseBody>
                    ) {
                        LogUtils.e("下载开始", "onResponse")
                        //如果response?.body为空，那么就代表下载异常了
                        if (response?.body() == null) {
                            if (lisenter != null) {
                                lisenter?.downloadFail()
                            }
                            return
                        }
                        Thread {
                            kotlin.run {
                                writeResponseBodyToDisk(response?.body()!!, context)
                            }
                        }.start()
                    }

                })
    }


    private fun writeResponseBodyToDisk(
            response: ResponseBody,
            context: Context
    ) {
        try {
            isTream = response.byteStream()//获取输入流
            val total = response.contentLength()//获取文件大小
            LogUtils.e("文件大小=", total.toString() + "KB")
            val file = File(
                    Environment.getExternalStorageDirectory(),
                    "apkDownload.apk"
            )// 设置路径
            if (isTream != null) {


                if (file.exists()) {
                    file.delete()
                }

                fos = FileOutputStream(file)
                val buf = ByteArray(1024 * 4)
                var sum: Long = 0
                var ch = 0
//                        while (isTream!!.read(buf).apply { ch = this } > 0{
//                                fos!!.write(buf, 0, ch)
//                            }
//                            }
                while (isTream!!.read(buf).apply { ch = this } > 0) {
                    fos?.write(buf, 0, ch)
                    sum += ch
                    val progress = (sum * 1.0f / total * 100)
                    if (lisenter != null) {
                        lisenter?.downloading(String.format("%.2f",progress))
                    }
                    LogUtils.i("APK下载进度", "${progress}%")
                }
            }
            fos?.flush()

            // 下载完成
            if (fos != null) {
                fos?.close()
            }
            LogUtils.e("APK=", "下载完成")
            if (lisenter != null) {
                lisenter?.downloadSuc(file)
            }
            //                    view.downSuccess();
        } catch (e: Exception) {
            if (lisenter != null) {
                lisenter?.downloadFail()
            }
            LogUtils.e("Apk下载", "异常${e.message}")
        } finally {
            try {
                if (isTream != null)
                    isTream?.close()
            } catch (e: Exception) {
            }

            try {
                if (fos != null)
                    fos?.close()
            } catch (e: Exception) {
            }

        }
    }

    private var lisenter: DowloadingLisenter? = null

    fun setOnDownloadingLisenter(lisenter: DowloadingLisenter) {
        this.lisenter = lisenter
    }

    interface DowloadingLisenter {
        fun downloading(progress: String)
        fun downloadSuc(path: File)
        fun downloadFail()
    }
}