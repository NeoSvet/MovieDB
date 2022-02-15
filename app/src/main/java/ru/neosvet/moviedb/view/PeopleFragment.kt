package ru.neosvet.moviedb.view

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.list.Person
import ru.neosvet.moviedb.list.PersonsAdapter

class PeopleFragment : Fragment() {
    companion object {
        private const val CARD_WIDTH = 120
        private var people = listOf<Person>()
        fun newInstance(list: List<Person>): PeopleFragment {
            people = list
            return PeopleFragment()
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
        initList()
    }

    private fun initList() {
        val adapter = PersonsAdapter { id ->
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, PersonFragment.newInstance(id))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }

        view?.let {
            val rvPeople = it.findViewById<RecyclerView>(R.id.rvPeople)
            val width = getDisplayWidth() / resources.displayMetrics.density
            val count = width.toInt() / CARD_WIDTH
            rvPeople.layoutManager = GridLayoutManager(
                requireContext(), count
            )
            rvPeople.adapter = adapter
            adapter.setItems(people)
        }
    }

    private fun getDisplayWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val d = requireActivity().windowManager.currentWindowMetrics
            d.bounds.width()
        } else {
            val p = Point(0, 0)
            requireActivity().windowManager.defaultDisplay.getRealSize(p)
            p.x
        }
    }
}