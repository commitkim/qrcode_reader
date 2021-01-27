package com.gekim16.qrcodereader

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val intentIntegrator by lazy { IntentIntegrator(this) }
    private val context by lazy { this }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var moveSettingPage : ActivityResultLauncher<Intent>
    private var requestPermissionFun : () -> Unit = ::requestPermission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setListener()
    }

    private fun setListener(){
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

        /**
         *   1. 버튼을 누름 (첫 번째 시도) -> 권한 요청 (requestPermission())
         *   4. 다시 버튼을 누름 (한번 거절 후 시도) -> 권한 요청 (requestPermission())
         *   8. 다시 버튼을 누름 (다시보지 않기 선택 후 시도) -> 설정창으로 이동하는 dialog 띄워야함 (showRequestPermissionDialog())
         */
        scan_button.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED){
                intentIntegrator.initiateScan()
            }
            else{
                requestPermissionFun()
            }
        }

        /**
         *  3. callback else 분기 실행 (한번 요청하고 난 뒤기 때문에 else 안의 if 문은 실행이 되지 않는다.)
         *  7. 다시보지 않기 선택 후 거절 -> else 분기 실행 (다시보지 않기를 선택했기 때문에 else 안의 if 문이 실행된다.)
         */
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            if(isGranted){
                init()
            }
            else{
                if(!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    requestPermissionFun = ::showPermissionRationaleDialog
                }
            }
        }

        moveSettingPage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.d("jungbong",it.data.toString())
        }
    }

    private fun init(){
        intentIntegrator.setPrompt("바코드를 사각형 안에 비춰주세요.")
        intentIntegrator.setBeepEnabled(false)  // 인식할때 소리 여부
        intentIntegrator.setBarcodeImageEnabled(false) // 이미지를 캡쳐하기 위함
        intentIntegrator.setOrientationLocked(false) // 휴대폰 방향에 따라 세로, 가로 모드를 변경하기 위함.
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
                Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    /**
     *  2. 권한 요청 (1,2 -> false) 3 실행
     *  5. 권한 재요청 (1 -> false, 한번 거절했기 때문에 2 -> true) 2 실행 (설명 dialog(확인) 후 권한 요청)
     *
     */
    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> { // 권한이 승인되어 있다면
                init()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> { // 1. 첫 요청시, 2. 다시 묻지 않음을 선택을 했다면 -> false
                showPermissionRationaleDialog(getString(R.string.explanation_camera))
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    /**
     *  6. 권한 설명 후 권한 요청
     */
    private fun showPermissionRationaleDialog(message : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission").setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) // 권한 요청
            dialog.dismiss()
        }
        builder.create().show()
    }

    /**
     *  9. 권한 재재요청 ALLOW 선택시 설정창으로 이동
     */
    private fun showPermissionRationaleDialog(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Permission")
        dialog.setMessage(getString(R.string.explanation_camera))

        dialog.setPositiveButton("ALLOW") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
            moveSettingPage.launch(intent)
        }
        dialog.setNegativeButton("DENY") { _, _ ->
            Toast.makeText(this, "권한 거절됨", Toast.LENGTH_LONG)
                .show()
        }
        dialog.show()
    }
}
