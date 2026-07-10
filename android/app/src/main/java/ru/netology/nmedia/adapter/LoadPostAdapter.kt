package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ItemLodiangBinding

class LoadPostAdapter(private val retryListener: () -> Unit) :
    LoadStateAdapter<PostLoadingViewHolder>() {
    override fun onBindViewHolder(
        holder: PostLoadingViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostLoadingViewHolder =
        PostLoadingViewHolder(
            ItemLodiangBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retryListener
        )

}

class PostLoadingViewHolder(
    private val item: ItemLodiangBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(item.root) {

    fun bind(loadState: LoadState) {
        item.apply {
            progress.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            retryButton.setOnClickListener {
                retryListener()
            }
        }
    }
}