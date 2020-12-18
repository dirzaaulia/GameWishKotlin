package com.dirzaaulia.gamewish.deals

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentDealsBinding
import com.dirzaaulia.gamewish.deals.adapter.DealsAdapter
import com.dirzaaulia.gamewish.models.Deals
import com.google.android.material.transition.MaterialFadeThrough

class DealsFragment : Fragment() {

    private lateinit var binding : FragmentDealsBinding

    private val viewModel: DealsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }

        ViewModelProvider(
            this, DealsViewModel.Factory(activity.application)
        ).get(DealsViewModel::class.java)
    }

    private val adapter = DealsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentDealsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        binding.dealsRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.deals.observe(viewLifecycleOwner, {
            it?.apply {
                adapter.submitList(it)
            }
        })
    }

}