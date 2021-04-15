package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentMovieBinding
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.PosterUtils
import java.lang.StringBuilder

class MovieFragment : Fragment(), Observer<MovieState> {
    companion object {
        private val ARG_ID = "movie_id"
        fun newInstance(movieId: Int) =
            MovieFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, movieId)
                }
            }
    }

    private var movieId: Int? = null
    private var _binding: FragmentMovieBinding? = null
    private var movie: MovieEntity? = null
    private lateinit var itemEdit: MenuItem
    private lateinit var itemSave: MenuItem
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
    ): View {
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        itemEdit = menu.add(R.string.edit_note)
        itemEdit.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_edit_24)
        itemEdit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        itemSave = menu.add(R.string.save)
        itemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        itemSave.setVisible(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (movie == null)
            return super.onOptionsItemSelected(item)
        with(binding) {
            if (itemEdit.isVisible) {
                movie?.let {
                    etNote.setText(it.note)
                }
                tvDescription.visibility = View.GONE
                etNote.visibility = View.VISIBLE
                itemEdit.setVisible(false)
                itemSave.setVisible(true)
            } else {
                movie?.let {
                    it.note = etNote.text.toString()
                    model.updateMovie(it)
                }
                showDes()
                tvDescription.visibility = View.VISIBLE
                etNote.visibility = View.GONE
                itemEdit.setVisible(true)
                itemSave.setVisible(false)
            }
        }
        return super.onOptionsItemSelected(item)
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
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                binding.tvTitle.showError(message,
                    getString(R.string.repeat), { loadDetails() })
            }
        }
    }

    private fun showItem(item: MovieEntity) {
        movie = item
        with(binding) {
            PosterUtils.load(item.poster, ivPoster)
            tvTitle.text = item.title
            tvDate.text = getString(R.string.release_date) + item.date
            tvOriginal.text = item.original
            tvGenres.text = model.genresToString(item.genre_ids)
            showDes()
            barVote.progress = (item.vote * 10).toInt()
            tvVote.text = "(${item.vote})"
        }
    }

    private fun showDes() {
        val item = movie ?: return
        if (item.note.length == 0)
            binding.tvDescription.text = item.description
        else {
            val s = StringBuilder(item.description)
            s.appendLine()
            s.appendLine()
            s.appendLine(getString(R.string.my_note))
            s.append(item.note)
            binding.tvDescription.text = s.toString()
        }
    }
}