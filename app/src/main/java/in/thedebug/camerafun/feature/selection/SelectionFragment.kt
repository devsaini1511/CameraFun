package `in`.thedebug.camerafun.feature.selection

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import `in`.thedebug.camerafun.databinding.FragmentSelectionBinding
import `in`.thedebug.camerafun.core.util.MyImageName
import `in`.thedebug.camerafun.core.util.PermissionType
import `in`.thedebug.camerafun.core.util.permission

class SelectionFragment : Fragment() {

    private var _binding: FragmentSelectionBinding? = null
    private val binding get() = _binding!!
    private val cameraPermission by permission(PermissionType.CAMERA) {

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        binding.cameraBtn.setOnClickListener {
            val direction =
                SelectionFragmentDirections.actionSelectionFragmentToCaptureCameraFragment(
                    MyImageName
                )
            findNavController().navigate(direction)

        }
        binding.galleryBTN.setOnClickListener {
            val direction =
                SelectionFragmentDirections.actionSelectionFragmentToPickGalleryFragment(
                    MyImageName
                )
            findNavController().navigate(direction)
        }

    }

    private fun checkPermission() =
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
            cameraPermission.request()
        else
            cameraPermission.request()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}