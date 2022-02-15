package ru.neosvet.moviedb.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentMovieBinding
import ru.neosvet.moviedb.list.PeopleAdapter
import ru.neosvet.moviedb.list.Person
import ru.neosvet.moviedb.list.PersonsAdapter
import ru.neosvet.moviedb.model.Details
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.model.getLink
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.ImageUtils
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.NoConnectionExc
import ru.neosvet.moviedb.view.extension.OnBackFragment
import ru.neosvet.moviedb.view.extension.hideKeyboard
import ru.neosvet.moviedb.view.extension.showKeyboard

class MovieFragment : OnBackFragment(), Observer<MovieState> {
    companion object {
        private const val LIMIT_PERSON = 6
        private const val ARG_ID = "movie_id"
        fun newInstance(movieId: Int) =
            MovieFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, movieId)
                }
            }
    }

    private var movieId: Int? = null
    private var movie: MovieEntity? = null
    private var _binding: FragmentMovieBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemEdit: MenuItem
    private lateinit var itemSave: MenuItem
    private lateinit var des: String
    private lateinit var note: String
    private val model: MovieModel by lazy {
        ViewModelProvider(this).get(MovieModel::class.java)
    }
    private val main: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private lateinit var peopleAdapter: PeopleAdapter
    private val onPersonClick: (Int) -> Unit = { id ->
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, PersonFragment.newInstance(id))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        loadDetails()
        initLink()
        binding.ivPoster.setOnClickListener {
            movie?.let {
                if (it.poster.isNotEmpty())
                    main.loadBigImage(it.poster)
            }
        }
    }

    private fun initLists() {
        peopleAdapter = PeopleAdapter { list ->
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, PeopleFragment.newInstance(list))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }
        binding.rvPeople.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPeople.adapter = peopleAdapter
    }

    private fun initLink() {
        binding.tvCountries.setOnClickListener {
            var s = binding.tvCountries.text
            s = s.substring(s.indexOf(":") + 2)
            if (s.contains(","))
                showMenu(s)
            else
                openMap(s)
        }
        binding.tvTryLoadEn.setOnClickListener {
            movieId?.let { model.loadDetailsEn(it) }
        }
    }

    private fun showMenu(list: String) {
        val menu = PopupMenu(requireContext(), binding.tvCountries)
        list.split(",").forEach {
            menu.menu.add(it.trimStart())
        }
        menu.setOnMenuItemClickListener {
            openMap(it.title.toString())
            return@setOnMenuItemClickListener true
        }
        menu.show()
    }

    private fun openMap(country: String) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, MapsFragment.newInstance(country))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
    }

    override fun onBackPressed(): Boolean {
        if (itemEdit.isVisible)
            return true
        closeEditNote()
        return false
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
        inflater.inflate(R.menu.movie, menu)
        itemEdit = menu.findItem(R.id.edit)
        itemSave = menu.findItem(R.id.save)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (movieId == null)
            return super.onOptionsItemSelected(item)
        if (item.itemId == R.id.message) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, ContactsFragment.newInstance(initMessage()))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
            return super.onOptionsItemSelected(item)
        }
        with(binding) {
            if (itemEdit.isVisible) {
                etNote.setText(note)
                tvDescription.visibility = View.GONE
                etNote.visibility = View.VISIBLE
                itemEdit.isVisible = false
                itemSave.isVisible = true
                etNote.requestFocus()
                etNote.setSelection(note.length)
                requireActivity().showKeyboard(etNote)
            } else {
                requireActivity().hideKeyboard(etNote)
                note = etNote.text.toString()
                movieId?.let {
                    model.addNote(it, note)
                }
                showDes()
                closeEditNote()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMessage(): String {
        movie?.let {
            return getString(R.string.recommend_movie) + it.title +
                    "\n" + it.getLink()
        }
        return ""
    }

    private fun closeEditNote() {
        binding.tvDescription.visibility = View.VISIBLE
        binding.etNote.visibility = View.GONE
        itemEdit.setVisible(true)
        itemSave.setVisible(false)
    }

    private fun loadDetails() {
        movieId?.let { model.loadDetails(it) }
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessMovie -> {
                showMovie(state.movie)
            }
            is MovieState.SuccessDetails -> {
                showDetails(state.details)
            }
            is MovieState.SuccessAll -> {
                showMovie(state.movie)
                showDetails(state.details)
            }
            is MovieState.Loading -> {
                main.startLoad()
            }
            is MovieState.Error -> {
                main.finishLoad()
                if (state.error is NoConnectionExc)
                    return
                val message = if (state.error is MyException)
                    state.error.getTranslate(requireContext())
                else
                    state.error.message
                main.showError(
                    message, getString(R.string.repeat)
                ) { loadDetails() }
            }
        }
    }

    private fun showMovie(item: MovieEntity) {
        movie = item
        with(binding) {
            ImageUtils.load(item.poster, ivPoster)
            tvTitle.text = item.title
            if (item.date.isNotEmpty()) {
                tvDate.text = String.format(getString(R.string.release_date), item.date)
                tvDate.visibility = View.VISIBLE
            }
            tvOriginal.text = item.original
            if (item.genre_ids.isNotEmpty()) {
                tvGenres.text = model.genresToString(item.genre_ids)
                tvGenres.visibility = View.VISIBLE
            }
            des = item.description
            if (des.isEmpty())
                binding.tvTryLoadEn.visibility = View.VISIBLE
            else
                binding.tvTryLoadEn.visibility = View.GONE
            note = model.getNote(item.id)
            showDes()
            barVote.progress = (item.vote * 10).toInt()
            tvVote.text = "(${item.vote})"
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDetails(details: Details) {
        with(binding) {
            if (details.countries.isNotEmpty()) {
                tvCountries.text =
                    String.format(getString(R.string.format_countries), details.countries)
                tvCountries.visibility = View.VISIBLE
            }

            peopleAdapter.addItem(
                title = getString(R.string.cast),
                people = details.cast,
                adapter = createAdapter(details.cast)
            )
            peopleAdapter.addItem(
                title = getString(R.string.crew),
                people = details.crew,
                adapter = createAdapter(details.crew)
            )
            peopleAdapter.notifyDataSetChanged()
        }
        main.finishLoad()
        model.getState().value = MovieState.Finished
    }

    private fun createAdapter(list: List<Person>): PersonsAdapter {
        var i = 0
        val adapter = PersonsAdapter(onPersonClick)
        while (i < LIMIT_PERSON && i < list.size) {
            adapter.addItem(list[i])
            i++
        }
        return adapter
    }

    private fun showDes() {
        if (note.isEmpty())
            binding.tvDescription.text = des
        else {
            val s = StringBuilder(des)
            s.appendLine()
            s.appendLine()
            s.appendLine(getString(R.string.my_note))
            s.append(note)
            binding.tvDescription.text = s.toString()
        }
    }
}