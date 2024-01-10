package com.example.ma23_android_project_2.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ma23_android_project_2.MainActivity
import com.example.ma23_android_project_2.R
import com.example.ma23_android_project_2.databinding.FragmentLoginBinding
import com.example.ma23_android_project_2.ui.list.mainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    lateinit var auth : FirebaseAuth
    lateinit var emailView : EditText
    lateinit var passwordView : EditText

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val loginViewModel =
            ViewModelProvider(this).get(LoginViewModel::class.java)

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = Firebase.auth
        emailView = binding.emailEditText
        passwordView = binding.passwordEditText

        val signUpButton = binding.signUpButton
        signUpButton.setOnClickListener {
            signUp()
        }

        val signInButton = binding.signInButton
        signInButton.setOnClickListener{
            signIn()
        }

        if(auth.currentUser != null){
            Toast.makeText(requireContext(), "Already logged in, return" , Toast.LENGTH_SHORT).show()
            returnTo(true)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun returnTo(loggedIn : Boolean){
        val mainActivity = requireActivity() as MainActivity

        if(loggedIn) {
            mainActivity.loggedIn = true
            mainActivity.loggedInUser = auth.currentUser?.email ?: "Unknown LOGGED in user"

        } else {
            mainActivity.loggedIn = false
        }
        findNavController().popBackStack()
    }
    fun signIn() {
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            return

        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    Log.d("!!!", "login success")
                    returnTo(true)
                }
                else {
                    Log.d("!!!", "User not signed in ${task.exception}")
                    Toast.makeText(requireContext(), "Login/register failed, try again" , Toast.LENGTH_SHORT).show()
                    returnTo(false)
                }
            }

    }

    fun signUp(){
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            return

        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    Log.d("!!!", "create success")
                    returnTo(true)
                }
                else {
                    Log.d("!!!", "User not created ${task.exception}")
                    Toast.makeText(requireContext(), "Login/register failed, try again" , Toast.LENGTH_SHORT).show()
                    returnTo(false)
                }
            }
    }
}