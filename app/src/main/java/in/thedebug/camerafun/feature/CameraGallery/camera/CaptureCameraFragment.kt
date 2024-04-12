package `in`.thedebug.camerafun.feature.CameraGallery.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import `in`.thedebug.camerafun.databinding.FragmentCaptureCameraBinding
import `in`.thedebug.camerafun.core.util.CameraHelper
import `in`.thedebug.camerafun.core.util.MyImageName
import kotlinx.coroutines.launch


class CaptureCameraFragment : Fragment() {

    private var _binding: FragmentCaptureCameraBinding? = null
    private val binding get() = _binding!!
    private val cameraHelper by lazy { CameraHelper(requireContext(), MyImageName) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCaptureCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            cameraHelper.startCamera(binding.cameraView,this@CaptureCameraFragment)
        }

        binding.ivCapture.setOnClickListener {
            cameraHelper.takePicture{
                val directions =
                    CaptureCameraFragmentDirections.actionCaptureCameraFragmentToPreviewFragment(
                        MyImageName
                    )
                findNavController().navigate(directions)
            }
        }
    }

}