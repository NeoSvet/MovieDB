package ru.neosvet.moviedb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.databinding.FragmentMovieBinding
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.Movie

private const val ARG_ID = "movie_id"

class MovieFragment : Fragment(), Observer<MovieState> {
    private var movieId: Int? = null
    private var _binding: FragmentMovieBinding? = null
    private val binding get() = _binding!!
    private lateinit var model: MovieModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            movieId = it.getInt(ARG_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDetails();
    }

    override fun onResume() {
        super.onResume()
        model.getState().observe(this, this)
    }

    override fun onPause() {
        super.onPause()
        model.getState().removeObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDetails() {
        model = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(MovieModel::class.java)
        model.loadDetails(movieId)
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessItem -> {
                showItem(state.item)
            }
            is MovieState.Error -> {
                Snackbar.make(
                    binding.tvTitle, getString(R.string.error)
                            + ": " + state.error.message,
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    }

    private fun showItem(item: Movie) {
        binding.tvTitle.text = item.title
        binding.tvYear.text = item.year.toString()
        binding.tvCountry.text = item.country
        binding.tvGenres.text = item.genres
        binding.tvDescription.text = item.description
    }

    companion object {
        @JvmStatic
        fun newInstance(movieId: Int) =
            MovieFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, movieId)
                }
            }
    }
}