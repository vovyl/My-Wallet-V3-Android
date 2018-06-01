package piuk.blockchain.android.ui.buysell.coinify.signup.kyc

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.util.PermissionUtil
import piuk.blockchain.androidcore.utils.annotations.Thunk
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.toast
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_coinify_kyc.linear_layout_kyc_root as rootView
import kotlinx.android.synthetic.main.activity_coinify_kyc.web_view_coinify_kyc as webView


@Suppress("MemberVisibilityCanBePrivate")
class CoinifyKycActivity : BaseAuthActivity() {

    @Thunk var permissionRequest: PermissionRequest? = null
    private val redirectUrl by unsafeLazy { intent.getStringExtra(EXTRA_REDIRECT_URL) }
    private val returnUrl by unsafeLazy { intent.getStringExtra(EXTRA_RETURN_URL) }

    // Upload Objects
    @Thunk var valueCallback: ValueCallback<Array<Uri>>? = null
    @Thunk var capturedImageUri: Uri? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_kyc)
        setupToolbar(toolbar_general, R.string.buy_sell_identification_verification)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                view?.scrollTo(0, 0)
                if (url == returnUrl) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            @TargetApi(Build.VERSION_CODES.M)
            override fun onPermissionRequest(request: PermissionRequest?) {
                permissionRequest = request
                requestNecessaryPermissions()
            }
//            @TargetApi(Build.VERSION_CODES.KITKAT)
//            override fun openFileChooser(
//                    uploadMsg: ValueCallback<Uri>,
//                    acceptType: String,
//                    capture: String
//            ) {
//                val intent = Intent(Intent.ACTION_GET_CONTENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.type = "*/*"
//                startActivityForResult(Intent.createChooser(intent, "File Browser"), 12345)
//            }

            // TODO: I'm not sure if we need something different for other API levels
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(
                    mWebView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                // Double check that we don't have any existing callbacks
                valueCallback?.onReceiveValue(null)
                valueCallback = filePathCallback

                var fileChooserIntent: Intent? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fileChooserIntent = fileChooserParams.createIntent()
                }
                return try {

                    var captureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (captureIntent!!.resolveActivity(packageManager) != null) {
                        // Create the File where the photo should go
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            captureIntent.putExtra("PhotoPath", capturedImageUri)
                        } catch (e: IOException) {
                            // Error occurred while creating the File
                            Timber.e(e, "Unable to create Image File")
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            capturedImageUri = Uri.parse("file:" + photoFile.absolutePath)
                            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                        } else {
                            captureIntent = null
                        }
                    }

                    val intentArray: Array<Intent> = if (captureIntent != null) {
                        arrayOf(captureIntent)
                    } else {
                        emptyArray()
                    }
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        putExtra(Intent.EXTRA_INTENT, fileChooserIntent)
                        putExtra(Intent.EXTRA_TITLE, getString(R.string.buy_sell_choose_file))
                        putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                    }
                    startActivityForResult(chooserIntent, REQUEST_CODE_PICK_FILE)

                    true
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.buy_sell_cannot_open_file, ToastCustom.TYPE_ERROR)
                    false
                }

            }
        }
        webView.loadUrl(redirectUrl)
        requestNecessaryPermissions()
    }

    @Throws(IOException::class)
    @Thunk
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_FILE) {
            val uri = if (data == null) capturedImageUri!! else data.data
            valueCallback!!.onReceiveValue(arrayOf(uri))
            valueCallback = null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean = consume {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun grantPermissionToWebView() {
        permissionRequest?.grant(permissionRequest!!.resources)
    }

    @Thunk
    fun requestNecessaryPermissions() {
        if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PermissionUtil.requestWriteStorageAndCameraPermissionFromActivity(rootView, this)
        } else {
            grantPermissionToWebView()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_WRITE_STORAGE_AND_CAMERA) {
            if (grantResults.size == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                grantPermissionToWebView()
            } else {
                // Permission request was denied - should be handled by permissions util class
            }
        }
    }

    /**
     * If a user is trying to find a particular file, we don't want to frustrate them by killing the
     * app, as it will also kill the Activity started to find the file.
     */
    override fun startLogoutTimer() = Unit

    override fun shouldShowRequestPermissionRationale(permission: String) = false

    companion object {

        private const val EXTRA_REDIRECT_URL =
                "piuk.blockchain.android.ui.buysell.coinify.signup.kyc.EXTRA_REDIRECT_URL"
        private const val EXTRA_RETURN_URL =
                "piuk.blockchain.android.ui.buysell.coinify.signup.kyc.EXTRA_RETURN_URL"
        private const val REQUEST_CODE_PICK_FILE = 9123

        fun startForResult(
                activity: Activity,
                redirectUrl: String,
                returnUrl: String,
                requestCode: Int
        ) {
            Intent(activity, CoinifyKycActivity::class.java).apply {
                putExtra(EXTRA_REDIRECT_URL, redirectUrl)
                putExtra(EXTRA_RETURN_URL, returnUrl)
            }.run { activity.startActivityForResult(this, requestCode) }
        }

    }

}