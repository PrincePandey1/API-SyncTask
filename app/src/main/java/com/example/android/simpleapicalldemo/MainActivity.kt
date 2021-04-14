package com.example.android.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.core.content.res.TypedArrayUtils.getString
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallAPILoginAsyncTask("Prince" , "123456").execute()
    }

    private inner class CallAPILoginAsyncTask(val user: String , val id: String) : AsyncTask<Any, Void, String>() {

        private lateinit var customProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()

            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {

            var result : String

            var connection: HttpURLConnection? = null

            try{
                val url = URL("https://run.mocky.io/v3/66552d3a-2401-467e-a803-2ed0d0c707b5")
                connection = url.openConnection() as HttpURLConnection
               connection.doInput = true
                connection.doOutput = true

                //To post a Request to the server
                connection.instanceFollowRedirects = false //It is used such tha t No other webpage opens during the start of application
                //Request Method
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type" , "application/json")
                connection.setRequestProperty("charset" , "utf-8")
                connection.setRequestProperty("Accept","application/json")

                connection.useCaches = false

                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonObject = JSONObject()
                jsonObject.put("username" , user)
                jsonObject.put("Password" , id)

                writeDataOutputStream.writeBytes(jsonObject.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?

                    try {
                       while(reader.readLine().also { line = it }!=null){
                             stringBuilder.append(line+"\n")
                       }
                    }catch (e: IOException){  //for something goes wrong
                          e.printStackTrace()
                    }finally { // close input stream
                        try {
                            inputStream.close()
                        }catch (e: IOException){
                            e.printStackTrace()
                        }

                    }
                    result = stringBuilder.toString()
                }else{
                    result = connection.responseMessage
                }

            }catch (e: SocketTimeoutException){
                result = "Connection Timeout"
            }catch (e: Exception){
                result="Error :"+ e.message
            }finally {
                connection?.disconnect()
            }
             return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.i("JSON RESPONSE RESULT", result)

            val jsonObject = JSONObject(result)
            val name = jsonObject.optString("message")
            val Name = jsonObject.optString("name")
            val user = jsonObject.optInt("User")
             Log.i("Name","$name")
            Log.i("Prince","$Name")
            Log.i("UUser", "$user")

            // Fetching Data from another jsonObject
            val profileDetails = jsonObject.optJSONObject("details")
            Log.i("details" , "$profileDetails")
            val isProfileCompleted = profileDetails.optBoolean("completed")
            Log.i("Profile Completed " , "$isProfileCompleted")

            // Fetching Data from jsonArray Which is inside the another jsonObject
            val jsonArray = jsonObject.optJSONArray("data_list")
            Log.i("json Array" , "$jsonArray")

            // As Data stored in Array is as JSonObject
            // So fetch item one by one from JsonArray And Stored in Variable AS a JsonObject

             for(items in 0 until jsonArray.length()) {
                 Log.i("Value $items", "${jsonArray[items]}")

                 val dataItemObject: JSONObject = jsonArray[items] as JSONObject
                 val ID = dataItemObject.optInt("id")
                 Log.i("id", "$ID")

                 val Value = dataItemObject.optString("value")
                 Log.i("Value", "$Value")
             }
        }



        private fun showProgressDialog() {
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()

        }
    }
}