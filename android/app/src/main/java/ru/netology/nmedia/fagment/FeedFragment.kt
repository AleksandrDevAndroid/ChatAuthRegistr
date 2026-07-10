package ru.netology.nmedia.fagment

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.adapter.LoadPostAdapter
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private fun showDialog() {
        AlertDialog.Builder(context)
            .setTitle("Don't enter in app")
            .setMessage("Please authorized in app")
            .setIcon(R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes") { dialog, _ ->
                findNavController().navigate(ru.netology.nmedia.R.id.login_fragment)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (!authViewModel.authenticated) {
                    showDialog()
                    return
                }
                viewModel.likeById(post.id, post.likedByMe)
                viewModel.refreshPosts()
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                viewModel.refreshPosts()

            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(
                        intent,
                        getString(ru.netology.nmedia.R.string.chooser_share_post)
                    )
                startActivity(shareIntent)
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(
                    ru.netology.nmedia.R.id.action_feedFragment_to_showPhotoFragment2,
                    Bundle().apply {
                        putString("url", post.attachment?.url)
                    }
                )
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadPostAdapter { adapter.retry() },
            footer = LoadPostAdapter { adapter.retry() }
        )

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(
                    binding.root,
                    ru.netology.nmedia.R.string.error_loading,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(ru.netology.nmedia.R.string.retry_loading) { viewModel.refreshPosts() }
                    .show()
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            lifecycleScope.launchWhenCreated {
                viewModel.data.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing = it.refresh is LoadState.Loading
            }
        }


        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()

        }

        binding.updateList.setOnClickListener {
            viewModel.updateStatus()
            viewModel.refreshPosts()
            binding.updateList.isVisible = false
            binding.list.post {
                binding.list.smoothScrollToPosition(0)
            }
        }

        binding.fab.setOnClickListener {
            if (!authViewModel.authenticated) {
                showDialog()
                return@setOnClickListener
            }
            findNavController().navigate(ru.netology.nmedia.R.id.action_feedFragment_to_newPostFragment)
        }
        return binding.root
    }
}