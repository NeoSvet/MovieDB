package ru.neosvet.moviedb.view

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.list.ContactCallbacks
import ru.neosvet.moviedb.list.ContactsAdapter

class ContactsFragment : Fragment(), ContactCallbacks {
    companion object {
        private val ARG_MSG = "msg"
        fun newInstance(message: String) =
            ContactsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MSG, message)
                }
            }
    }

    private val REQUEST_CODE = 472
    private var message: String? = null
    private val adapter: ContactsAdapter by lazy {
        ContactsAdapter(requireContext(), this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString(ARG_MSG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContacts)
        rvContacts.adapter = adapter
        checkPermission()
    }

    override fun onDestroyView() {
        adapter.onDestory()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val item = menu.add(R.string.share)
        item.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_share_24)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(share)
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    getContacts()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.access_contacts)
                        .setMessage(R.string.about_access_contacts)
                        .setNegativeButton(android.R.string.no) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                getContacts()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.access_contacts)
                    .setMessage(R.string.about_access_contacts)
                    .setPositiveButton(R.string.give_access) { _, _ ->
                        requestPermission()
                    }
                    .setNegativeButton(R.string.do_not) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun getContacts() {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val cursorWithContacts: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        cursorWithContacts?.let { cursor ->
            if (!cursor.moveToFirst())
                return
            val iName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val iId = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val iHasPhone = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            do {
                if (cursor.getInt(iHasPhone) == 0)
                    continue
                val name = cursor.getString(iName)
                val id = cursor.getInt(iId)
                adapter.addConctact(id, name)
            } while (cursor.moveToNext())
            adapter.notifyDataSetChanged()
        }
        cursorWithContacts?.close()
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE)
    }

    override fun onContactClicked(phone: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone))
        intent.putExtra("sms_body", message)
        startActivity(intent)
    }
}