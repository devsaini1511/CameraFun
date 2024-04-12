package `in`.thedebug.camerafun.core.util

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import `in`.thedebug.camerafun.R
import `in`.thedebug.camerafun.databinding.DialogPermissionRequestBinding
import timber.log.Timber


fun ComponentActivity.permission(
    onResult: (Boolean) -> Unit,
) = lazyOf(registerForActivityResult(ActivityResultContracts.RequestPermission(), onResult))

fun ComponentActivity.permissions(
    onResult: (Map<String, Boolean>) -> Unit,
) = lazyOf(registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), onResult))

fun Fragment.multiplePermissions(
    onResult: (Map<String, Boolean>) -> Unit,
) = lazyOf(registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), onResult))

fun Map<String, Boolean>.isAllGranted() = filterValues { it.not() }.isEmpty()

fun <I> ActivityResultLauncher<I>.request(input: I) = launch(input)

fun Fragment.permission(
    permissionType: PermissionType,
    onRational: (PermissionType) -> Unit = {},
    onDenied: () -> Unit = {},
    onGranted: () -> Unit,
): Lazy<PermissionWrapper> {
    val registerForActivityResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) onGranted() else onDenied()
    }
    return lazyOf(PermissionWrapper(registerForActivityResult, permissionType,
        onRational))
}

fun Fragment.permission(
    permissionType: PermissionType,
    onDenied: (() -> Unit)? = null,
    onGranted: () -> Unit,
): Lazy<PermissionWrapper> {
    return permission(
        permissionType,
        onRational = {
            Timber.d("on rational")
            permissionDenied(it, onDenied = onDenied)
        },
        onDenied = {
            Timber.d("on denied")
            if (onDenied != null)
                permissionDenied(permissionType, onDenied)
        },
        onGranted = onGranted
    )
}

fun Fragment.requestPermission(
    permission: PermissionWrapper,
) {
    if (shouldShowRequestPermissionRationale(permission.permissionType.permission))
        permission.onRational(permission.permissionType)
    else permission.request()
}


class PermissionWrapper(
    private val launcher: ActivityResultLauncher<String>,
    val permissionType: PermissionType,
    val onRational: (PermissionType) -> Unit,
) {
    fun request() = launcher.launch(permissionType.permission)
}



sealed class PermissionType(
    val permission: String,
    val icon: Int,
    val permissionTitle: String,
    val permissionSubtitle: String,
) {
    data object CAMERA : PermissionType(
        permission = Manifest.permission.CAMERA,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Camera",
        permissionSubtitle = "To capture photos and videos, allow phone to access to your camera"
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object POST_NOTIFICATION : PermissionType(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Video KYC Notification",
        permissionSubtitle = "To complete video KYC, allow phone to access to your notification"
    )

    data object MICROPHONE : PermissionType(
        Manifest.permission.RECORD_AUDIO,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Microphone",
        permissionSubtitle = "To record audio, allow phone to access to your microphone")

    data object LOCATION : PermissionType(
        Manifest.permission.ACCESS_FINE_LOCATION,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Location",
        permissionSubtitle = "To capture location, allow phone to access to your location")


    data object GPS : PermissionType(
        Manifest.permission.ACCESS_FINE_LOCATION,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "GPS",
        permissionSubtitle = "To capture location, allow phone to access to your GPS")

    data object SMS : PermissionType(
        Manifest.permission.READ_SMS,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Sms",
        permissionSubtitle = "To capture sms, allow phone to access to your sms")

    data object CONTACT : PermissionType(
        Manifest.permission.READ_CONTACTS,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Contact",
        permissionSubtitle = "To capture contact, allow phone to access to your contact")

    data object STORAGE : PermissionType(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        icon = R.drawable.icon_camera_24,
        permissionTitle = "Storage",
        permissionSubtitle = "To capture storage, allow phone to access to your storage")
}


fun Fragment.permissionDenied(
    permissionType: PermissionType,
    onDenied: (() -> Unit)? = null,
    onAllow: (() -> Unit) = { },
) {
    val alertBinding = DialogPermissionRequestBinding.inflate(layoutInflater)
        .apply {
            name = permissionType.permissionTitle
            description = permissionType.permissionSubtitle
            imageView.load(permissionType.icon)
        }

    val alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedCornersDialog)
        .setView(alertBinding.root)
        .setCancelable(false)
        .create()

    val onAllowClick: (View) -> Unit = {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        onAllow()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (alertDialog.isShowing)
            alertDialog.dismiss()
        startActivity(intent)
        alertDialog.dismiss()

    }



    alertBinding.allowButton.setOnClickListener(onAllowClick)
    alertBinding.denyButton.setOnClickListener {
        if (alertDialog.isShowing) {
            alertDialog.dismiss()
            if (onDenied != null) {
                onDenied()
            } else findNavController().navigateUp()
        }
    }
    alertDialog.show()


}