package com.molysulfur.application.rxretryapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Flowable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    companion object {
        private val RETRIES_LIMIT = 3L
    }

    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         *  retry
         */
        val retry = Flowable.interval(0, 1, TimeUnit.SECONDS)
            .flatMap {result ->
                if (result >= 2) Flowable.error<IOException> { IOException("Something Wrong!!") }
                else Flowable.just(result)
            }
            .doOnNext {
                Log.e("doOnNext", it.toString())
            }.doOnError {
                Log.e("doOnError", it.message)
            }.retry(2)
            .doOnComplete {
                Log.e("doOnComplete", "Completed")
            }

        /**
         *  retry until
         */

        val retryUntil = Flowable.interval(0, 1, TimeUnit.SECONDS)
            .flatMap {result ->
                if (result >= 2) Flowable.error<IOException> { IOException("Something Wrong!!") }
                else Flowable.just(result)
            }
            .doOnNext {
                Log.e("doOnNext", it.toString())
            }.doOnError {
                Log.e("doOnError", it.message)
            }.retryUntil {
                counter += 1
                counter > 2
            }
            .doOnComplete {
                counter = 0
                Log.e("doOnComplete", "Completed")
            }

        /**
         *  retry when
         */
        val retryWhen = Flowable.interval(0, 1, TimeUnit.SECONDS)
            .flatMap {
                if (it >= 2) Flowable.error<IOException> { IOException("Something Wrong!!") }
                else Flowable.just(it)
            }.retryWhen { error ->
                error.map {
                    it.message
                }.takeWhile {
                    it == "Something Wrong!!"
                }.doOnNext {
                    Log.e("Error doOnNext", "Something wrong!!")
                }.doOnError{
                    Log.e("Error doOnError", "Something wrong!!")
                }
            }
            .doOnNext {
                Log.e("doOnNext", it.toString())
            }.doOnError {
                Log.e("doOnError", it.message)

            }.doOnComplete {
                counter = 0
                Log.e("doOnComplete", "Completed")
            }
        // Subscribe retryWhen()
        btn_retrywhen.setOnClickListener {
            retryWhen.subscribe()
        }
        btn_retry.setOnClickListener {
            retry.subscribe()
        }
        btn_retryuntil.setOnClickListener {
            retryUntil.subscribe()
        }
    }

}
