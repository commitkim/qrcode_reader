package com.gekim16.qrcodereader.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gekim16.qrcodereader.BuildConfig
import com.gekim16.qrcodereader.R
import com.gekim16.qrcodereader.model.Result
import com.gekim16.qrcodereader.Contract
import com.gekim16.qrcodereader.model.Interactor
import com.gekim16.qrcodereader.presenter.Presenter
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), Contract.View, ResultAdapter.OnClickListener {

    private val intentIntegrator by lazy { IntentIntegrator(this) }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var moveSettingPage: ActivityResultLauncher<Intent>
    private var requestPermissionFun: () -> Unit = ::requestPermission

    private lateinit var presenter: Presenter
    private lateinit var adapter: ResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setPresenter()
        setListener()
    }

    private fun setPresenter() {
        presenter = Presenter(Interactor(this))
        presenter.setView(this)
        presenter.getResults {
            setAdapter(it)
        }
    }

    private fun setAdapter(list: MutableList<Result>) {
        adapter = ResultAdapter(list, this, this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
    }

    private fun setListener() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.filterResult(s.toString())
            }
        })

        floatingActionButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                intentIntegrator.initiateScan()
            } else {
                requestPermissionFun()
            }
        }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    init()
                } else {
                    if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        requestPermissionFun = ::showPermissionRationaleDialog
                    }
                }
            }

        moveSettingPage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
        }
    }

    private fun init() {
        intentIntegrator.setPrompt("???????????? ????????? ?????? ???????????????.")
        intentIntegrator.setBeepEnabled(false)  // ???????????? ?????? ??????
        intentIntegrator.setBarcodeImageEnabled(false) // ???????????? ???????????? ??????
        intentIntegrator.setOrientationLocked(false) // ????????? ????????? ?????? ??????, ?????? ????????? ???????????? ??????.
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { // ????????? ?????????????????? ?????? ??????????????? ??????????????? ???????????? ???????????? ?????????
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                presenter.addResult(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> { // ????????? ???????????? ?????????
                init()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> { // 1. ??? ?????????, 2. ?????? ?????? ????????? ????????? ????????? -> false
                showPermissionRationaleDialog(getString(R.string.explanation_camera))
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationaleDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission").setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) // ?????? ??????
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showPermissionRationaleDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Permission")
        dialog.setMessage(getString(R.string.explanation_camera))

        dialog.setPositiveButton("ALLOW") { _, _ ->
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
            moveSettingPage.launch(intent)
        }
        dialog.setNegativeButton("DENY") { _, _ ->
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG)
                .show()
        }
        dialog.show()
    }

    override fun onLongClick(result: Result): Boolean {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete")
        dialog.setMessage("?????????????????????????")

        dialog.setPositiveButton("??????") { _, _ ->
            presenter.deleteResult(result)
        }
        dialog.setNegativeButton("??????") { view, _ ->
            view.dismiss()
        }

        dialog.show()

        return true
    }

    override fun onClick(result: Result) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("?????? ????????? ????????? ??? ????????????.")
        }
    }

    override fun addAdapterList(result: Result) {
        adapter.addResult(result)
    }

    override fun deleteAdapterList(result: Result) {
        adapter.deleteResult(result)
    }


    override fun showFilteredList(str: String) {
        adapter.filter.filter(str)
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}
