package com.example.ma23_android_project_2.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is List Fragment"
    }
    val text: LiveData<String> = _text
}