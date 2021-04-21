package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentPersonBinding
import ru.neosvet.moviedb.model.PersonModel
import ru.neosvet.moviedb.model.PersonState
import ru.neosvet.moviedb.repository.room.PersonEntity
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.PosterUtils

class PersonFragment : Fragment(), Observer<PersonState> {
    companion object {
        private const val ARG_ID = "id"

        @JvmStatic
        fun newInstance(id: Int) =
            PersonFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                }
            }
    }

    private var personId: Int? = null
    private var _binding: FragmentPersonBinding? = null
    private val binding get() = _binding!!
    private val model: PersonModel by lazy {
        ViewModelProvider(this).get(PersonModel::class.java)
    }
    private val main: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            personId = it.getInt(ARG_ID)
            loadPerson()
        }
        binding.tvPlace.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, MapsFragment.newInstance(binding.tvPlace.text.toString()))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }
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

    private fun loadPerson() {
        personId?.let {
            model.loadPerson(it)
        }
    }

    override fun onChanged(state: PersonState) {
        when (state) {
            is PersonState.Success -> {
                main.getStatusView().visibility = View.GONE
                showPerson(state.person)
            }
            is PersonState.Loading -> {
                main.getStatusView().visibility = View.VISIBLE
                main.hideError()
            }
            is PersonState.Error -> {
                main.getStatusView().visibility = View.GONE
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                main.showError(message,
                    getString(R.string.repeat), { loadPerson() })
            }
        }
    }

    private fun showPerson(person: PersonEntity) {
        with(binding) {
            if (person.photo.length > 0)
                PosterUtils.load(person.photo, ivPhoto)
            tvName.text = person.name
            tvPlace.text = person.place_of_birth
            tvDates.text = getDates(person.birthday, person.deathday)
            barVote.progress = (person.popularity).toInt()
            tvVote.text = "(${person.popularity})"
            tvBiography.text = person.biography
        }
    }

    private fun getDates(birthday: String, deathday: String): String {
        if (birthday.length > 0) {
            val s = StringBuilder()
            s.append(getString(R.string.birthday))
            s.append(birthday)
            if (deathday.length > 0) {
                s.appendLine()
                s.append(getString(R.string.deathday))
                s.append(deathday)
            }
            return s.toString()
        }
        return ""
    }
}