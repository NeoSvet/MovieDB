package ru.neosvet.moviedb.view

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentMovieBinding
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.Movie
import ru.neosvet.moviedb.utils.Poster


private const val ARG_ID = "movie_id"

class MovieFragment : Fragment(), Observer<MovieState> {
    private var movieId: Int? = null
    private var _binding: FragmentMovieBinding? = null
    private val binding get() = _binding!!

    private val model: MovieModel by lazy {
        ViewModelProvider(this).get(MovieModel::class.java)
    }

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
        binding.tvDescription.setMovementMethod(ScrollingMovementMethod())
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
        model.loadDetails(movieId)
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessItem -> {
                showItem(state.item)
            }
            is MovieState.Error -> {
                binding.tvTitle.showError(state.error.message,
                    getString(R.string.repeat), { loadDetails() })
            }
        }
    }

    private fun showItem(item: Movie) {
        with(binding) {
            tvTitle.text = item.title
            tvDate.text = getString(R.string.release_date) + item.date
            tvOriginal.text = item.original
            tvGenres.text = model.genresToString(item.genres)
            tvDescription.text = item.description
            val poster = Poster(requireContext())
            ivPoster.setImageURI(
                android.net.Uri.parse(
                    poster.getFile(item.poster).path
                )
            )
        }
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