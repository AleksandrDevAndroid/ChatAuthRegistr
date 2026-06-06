package ru.netology.nmedia.fagment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentShowPhotoBinding

class ShowPhotoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentShowPhotoBinding.inflate(inflater, container, false)
        val url = arguments?.getString("url")
        val urlAttachment = "${BuildConfig.BASE_URL}/media/${url}"
        Glide.with(binding.showPhoto)
            .load(urlAttachment)
            .placeholder(R.drawable.outline_arrow_cool_down_24)
            .error(R.drawable.error)
            .timeout(6_000)
            .into(binding.showPhoto)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

}