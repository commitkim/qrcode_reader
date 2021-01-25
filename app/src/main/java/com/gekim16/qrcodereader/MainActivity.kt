package com.gekim16.qrcodereader

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*

private const val PERMISSION_REQUEST_CODE = 123

class MainActivity : AppCompatActivity() {

    private val intentIntegrator by lazy { IntentIntegrator(this) }
    private val context by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
    }

    private fun init(){
        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                Toast.makeText(context, "로딩 끝", Toast.LENGTH_SHORT).show()
            }
        }

        go_button.setOnClickListener {
            var address = editText.text.toString()
            if(!address.startsWith("http://")){
                address = StringBuilder("http://").append(address).toString()
            }

            webView.loadUrl(address)
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputMethodManager.hideSoftInputFromWindow(editText.windowToken,0)
        }
        scan_button.setOnClickListener {
            intentIntegrator.initiateScan()
        }

        intentIntegrator.setPrompt("바코드를 사각형 안에 비춰주세요.")
        intentIntegrator.setBeepEnabled(false)  // 인식할때 소리 여부
        intentIntegrator.setBarcodeImageEnabled(false) // 이미지를 캡쳐하기 위함
        intentIntegrator.setOrientationLocked(false) // 세로로 고정하기 위함
    }

    override fun onBackPressed() {
        if(webView.canGoBack()){ // 웹뷰에서 뒤로가기 할 수 있으면
            webView.goBack()  // 뒤로가기
        }
        else{
            super.onBackPressed() // 원래의 뒤로가기 기능 실행
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 현재의 액티비티에서 다른 액티비티로 넘어갔다가 돌아올때 호출되는 메소드

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null){
            if(result.contents != null){
                editText.setText(result.contents)

                showDialog(result.contents)
                Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    private fun showDialog(result : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("result").setMessage(result)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> { // 권한이 승인되어 있다면
                init()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> { // 권한이 승인되어 있지 않을 때 설명이 필요한지 확인
                showRationaleDialog("QR code 인식을 위해 카메라 권한이 필요합니다.") // 설명

            }
            else -> { // 설명이 필요없다고 답했을때

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray //권한 승인, 거절 내역
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> { //요청 코드 확인
                if (grantResults.isNotEmpty()){
                    var result = true
                    permissions.forEach {
                        when(it){
                            android.Manifest.permission.CAMERA -> {
                                result = if(grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_DENIED){
                                    showRationaleDialog("QR code 인식을 위해 카메라 권한이 필요합니다.") // 설명
                                    false
                                } else{
                                    result
                                }
                            }
                        }
                    }
                    if(result) { //권한이 승인 되었다면
                        init()
                    }
                }
            }
            else -> {
                //ignore
            }
        }
    }

    private fun showRationaleDialog(message : String){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("권한 요청")
        dialog.setMessage(message)

        dialog.setPositiveButton("Yes") { _, _ ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE)
        }
        dialog.setNegativeButton("No") { _, _ ->
            Toast.makeText(this, "권한 거절됨", Toast.LENGTH_LONG)
                .show()
        }
        dialog.show()
    }
}
