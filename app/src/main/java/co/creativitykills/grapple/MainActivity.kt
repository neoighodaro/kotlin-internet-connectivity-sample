package co.creativitykills.grapple

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainActivity : AppCompatActivity() {

    private val arrayList = ArrayList<String>()
    private val adapter = RecyclerAdapter()
    private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.reddit.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notConnected = intent.getBooleanExtra(ConnectivityManager
                    .EXTRA_NO_CONNECTIVITY, false)
            if (notConnected) {
                disconnected()
            } else {
                connected()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    private fun setupRecyclerView() {
        with(recyclerView){
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun disconnected() {
        recyclerView.visibility = View.INVISIBLE
        imageView.visibility = View.VISIBLE
    }

    private fun connected() {
        recyclerView.visibility = View.VISIBLE
        imageView.visibility = View.INVISIBLE
        fetchFeeds()
    }

    private fun fetchFeeds() {
        retrofit.create(ApiService::class.java)
                .getFeeds()
                .enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("MainActivityTag", t.message)
                    }

                    override fun onResponse(call: Call<String>?, response: Response<String>) {
                        addTitleToList(response.body()!!)
                    }

                })
    }

    private fun addTitleToList(response: String) {
        val jsonObject = JSONObject(response).getJSONObject("data")
        val children = jsonObject.getJSONArray("children")

        for (i in 0..(children.length()-1)) {
            val item = children.getJSONObject(i).getJSONObject("data").getString("title")
            arrayList.add(item)
            adapter.setItems(arrayList)
        }
    }
}

