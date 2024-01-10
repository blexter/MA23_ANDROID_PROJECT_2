package com.example.ma23_android_project_2.ui.list

import places
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.ma23_android_project_2.MainActivity
import com.example.ma23_android_project_2.databinding.FragmentCreateAndChangePlaceBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
lateinit var nameEditText : EditText
lateinit var longitudeEditText : EditText
lateinit var latitudeEditText : EditText
lateinit var openingHoursEditText : EditText
lateinit var otherInfoEditText : EditText
private lateinit var binding: FragmentCreateAndChangePlaceBinding
lateinit var saveButton : Button
lateinit var mainActivity : MainActivity



/**
 * A simple [Fragment] subclass.
 * Use the [CreateAndChangePlaceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateAndChangePlaceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        mainActivity = requireActivity() as MainActivity




    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateAndChangePlaceBinding.inflate(inflater, container, false)
        val view = binding.root


        nameEditText = binding.nameEditText
        longitudeEditText = binding.longitudeEditText
        latitudeEditText = binding.latitudeEditText
        openingHoursEditText = binding.openHoursEditText
        otherInfoEditText = binding.otherInfoEditText
        saveButton = binding.saveButton

        if(!mainActivity.loggedIn){
            saveButton.isVisible = false
            nameEditText.isFocusable = false
            longitudeEditText.isFocusable = false
            latitudeEditText.isFocusable = false
            openingHoursEditText.isFocusable = false
            otherInfoEditText.isFocusable = false
        }


        val position = arguments?.getInt("position", -1)

        if(position != -1 && position != null){
            showPlace(position)
            saveButton.setOnClickListener {
                editPlace(position)
            }
        }
        else {
            saveButton.setOnClickListener {
                addPlace()
            }
        }
        return view
    }

    fun showPlace(position : Int){

        val place = mainActivity.location[position]
        nameEditText.setText(place.name)
        longitudeEditText.setText(place.positionLon)
        latitudeEditText.setText(place.positionLat)
        openingHoursEditText.setText(place.openingHours)
        otherInfoEditText.setText(place.otherInfo)
    }

    fun editPlace(position : Int) {
        var mainActivity = requireActivity() as MainActivity
        val editedPlace = mainActivity.location[position]
        val db = Firebase.firestore
        val docRef = db.collection("places").document(editedPlace.documentId)



        db.runTransaction { transaction ->
            transaction.update(docRef, "name", nameEditText.text.toString())
            transaction.update(docRef, "positionLon", longitudeEditText.text.toString())
            transaction.update(docRef, "positionLat", latitudeEditText.text.toString())
            transaction.update(docRef, "openingHours", openingHoursEditText.text.toString())
            transaction.update(docRef, "otherInfo", otherInfoEditText.text.toString())
        }.addOnSuccessListener {
            Log.d("Firestore", "Transaction success!")
            findNavController().popBackStack()
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Transaction failure.", e)
        }
    }
    fun addPlace(){
        val name = nameEditText.text.toString()
        val longitude = longitudeEditText.text.toString()
        val latitude = latitudeEditText.text.toString()
        val openingHours = openingHoursEditText.text.toString()
        val otherInfo = otherInfoEditText.text.toString()

        val place = places(name, longitude, latitude, mainActivity.loggedInUser, openingHours, otherInfo)
        val db = Firebase.firestore

        db.collection("places").add(place)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                place.documentId = documentReference.id

                db.collection("places").document(documentReference.id)
                    .set(place)
                    .addOnSuccessListener {
                        Log.d("Firestore", "DocumentSnapshot updated with correct ID")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating document with correct ID", e)
                    }
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateAndChangePlaceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateAndChangePlaceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}