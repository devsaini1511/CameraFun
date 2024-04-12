package `in`.thedebug.camerafun.feature.CameraGallery.gallery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import `in`.thedebug.camerafun.databinding.FragmentPickGalleryBinding


class PickGalleryFragment : Fragment() {
    private var _binding: FragmentPickGalleryBinding? = null
    private val binding get() = _binding!!


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        val galleryUri = it
        try{
           binding.imageView.load(galleryUri)
        }catch(e:Exception){
            e.printStackTrace()
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPickGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pickBtn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

    }
}