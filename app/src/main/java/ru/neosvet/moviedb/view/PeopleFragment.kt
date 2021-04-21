package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.list.PeopleAdapter
import ru.neosvet.moviedb.list.PersonCallbacks

class PeopleFragment : Fragment(), PersonCallbacks {
    companion object {
        private val ARG_IDS = "ids"
        private val ARG_PEOPLE = "people"
        fun newInstance(ids: List<String>, people: List<String>) =
            PeopleFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_IDS, ids.toTypedArray())
                    putStringArray(ARG_PEOPLE, people.toTypedArray())
                }
            }
    }

    private var ids: Array<String> = arrayOf("")
    private var people: Array<String> = arrayOf("")
    private val adapter: PeopleAdapter by lazy {
        PeopleAdapter(requireContext(), this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            getStringArray(ARG_IDS)?.let {
                ids = it
            }
            getStringArray(ARG_PEOPLE)?.let {
                people = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvPeople = view.findViewById<RecyclerView>(R.id.rvPeople)
        rvPeople.adapter = adapter
        initList()
    }

    private fun initList() {
        for (i in people.indices) {
            adapter.addPerson(ids[i].toInt(), people[i])
        }
        adapter.notifyDataSetChanged()
    }

    override fun onPersonClicked(id: Int) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, PersonFragment.newInstance(id))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
    }
}