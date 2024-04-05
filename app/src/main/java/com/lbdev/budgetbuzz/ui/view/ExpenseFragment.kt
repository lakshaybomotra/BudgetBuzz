package com.lbdev.budgetbuzz.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Category
import com.lbdev.budgetbuzz.data.repository.CategoriesRepository
import com.lbdev.budgetbuzz.databinding.FragmentExpenseBinding
import com.lbdev.budgetbuzz.ui.adaptor.CardItemAdaptor
import com.lbdev.budgetbuzz.ui.viewmodel.CategoryViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import kotlin.math.min

class ExpenseFragment : Fragment(), CardItemAdaptor.OnItemClickListener, ImageLoaderFactory {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CardItemAdaptor
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoriesRepository: CategoriesRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        val view = binding.root
        categoriesRepository = CategoriesRepository()
        categoryViewModel = CategoryViewModel(categoriesRepository)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewExpense)

        val startIndex = arguments?.getInt("startIndex", 0) ?: 0
        val count = arguments?.getInt("count", 9) ?: 9

        sharedViewModel.expenseCategories.observe(viewLifecycleOwner) { categories ->
            val itemsForPage = categories.subList(startIndex, min(startIndex + count, categories.size))
            adapter = CardItemAdaptor(itemsForPage, this)
            recyclerView.layoutManager = GridLayoutManager(context, 3)
            recyclerView.adapter = adapter
        }
        return view
    }

    override fun onItemClick(item: Category) {
        sharedViewModel.selectedItem.value = item
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(startIndex: Int, count: Int): ExpenseFragment {
            val fragment = ExpenseFragment()
            val args = Bundle()
            args.putInt("startIndex", startIndex)
            args.putInt("count", count)
            fragment.arguments = args
            return fragment
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(requireContext())
            .memoryCache {
                MemoryCache.Builder(requireContext())
                    .maxSizePercent(0.25)
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(requireContext().cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}