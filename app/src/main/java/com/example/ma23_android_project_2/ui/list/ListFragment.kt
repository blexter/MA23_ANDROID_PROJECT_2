package com.example.ma23_android_project_2.ui.list

import places
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.example.ma23_android_project_2.DataChangeListener
import com.example.ma23_android_project_2.MainActivity
import com.example.ma23_android_project_2.R
import com.example.ma23_android_project_2.databinding.FragmentListBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class ListFragment : Fragment(), ListRecyclerAdapter.OnDeleteClickListener{//, DataChangeListener {

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var locations = mutableListOf<places>()
    lateinit var recyclerView : RecyclerView
    private lateinit var listAdapter: ListRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var mainActivity = requireActivity() as MainActivity

        locations = mainActivity.location

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        listAdapter = ListRecyclerAdapter(mainActivity, requireContext(), locations, this, this)
        recyclerView.adapter = listAdapter

        val fab = binding.floatingActionButton
        if(!mainActivity.loggedIn) {
            fab.isVisible = false;
        }
        else
        {
            fab.setOnClickListener {
                findNavController().navigate(R.id.nav_addPlace)
            }
        }

        val db = Firebase.firestore
        val docRef = db.collection("places")

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                locations.clear()

                for (document in snapshot.documents) {
                    val place = document.toObject<places>()
                    if (place != null) {
                        place.documentId = document.id
                        locations.add(place)
                        Log.d("!!!", place.toString())
                    }
                }

                updateAdapter()
            }
        }


        return root
    }

    override fun onDeleteClick(position: Int) {
        val mainActivity = requireActivity() as MainActivity

        val deletedPlace = mainActivity.location[position]

        val documentId = deletedPlace.documentId
        if (documentId != null) {
            val db = Firebase.firestore
            val docRef = db.collection("places").document(documentId)

            docRef.delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                    listAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error deleting document", e)
                }
        } else {
            Log.e("Firestore", "Document ID is null. Unable to delete place.")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume(){
        super.onResume()
        listAdapter.notifyDataSetChanged()
    }

    fun updateAdapter() {
        listAdapter.notifyDataSetChanged()
    }

}