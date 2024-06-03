package com.github.se.studybuddies.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studybuddies.data.Contact
import com.github.se.studybuddies.data.ContactList
import com.github.se.studybuddies.data.RequestList
import com.github.se.studybuddies.database.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactsViewModel(private val uid: String? = null) : ViewModel() {
  private val db = ServiceLocator.provideDatabase()
  private val _contacts = MutableStateFlow<ContactList>(ContactList(emptyList()))
  val contacts: StateFlow<ContactList> = _contacts

  private val _contact = MutableLiveData(Contact.empty())
  val contact: LiveData<Contact> = _contact

  private val _requests =  MutableStateFlow<RequestList>(RequestList(emptyList()))
  val requests: StateFlow<RequestList> = _requests


  init {
    if (uid != null) {
      fetchAllContacts(uid)
    }
  }

  fun createContact(otherUID: String, contactID: String) {
    viewModelScope.launch { db.createContact(otherUID, contactID) }
  }

  fun fetchContactData(contactID: String) {
    Log.d("contact", "A fetch contact called with ID $contactID")
    viewModelScope.launch {
      _contact.value = db.getContact(contactID)
      Log.d("contact", "A fetched contact in VMscope ${_contact.value}")
    }
    Log.d("contact", "A fetched contact ${_contact.value}")
  }

  fun getOtherUser(contactID: String, uid: String): String {
    fetchContactData(contactID)
    if (_contact.value == Contact.empty()) {
      return ""
    }
    return if ((contact.value?.members?.get(0) ?: "") == uid) {
      Log.d("contact", "getOtherUser 1")
      contact.value?.members?.get(1) ?: ""
    } else {
      Log.d("contact", "getOtherUser 0")
      contact.value?.members?.get(0) ?: ""
    }
  }

  private fun fetchAllContacts(uid: String) {
    viewModelScope.launch {
      try {
        val contacts = db.getAllContacts(uid)
        _contacts.value = contacts
      } catch (e: Exception) {
        Log.d("MyPrint", "In ViewModel, could not fetch contacts with error $e")
      }
    }
  }

  private fun fetchAllRequests(uid: String) {
    viewModelScope.launch {
      try {
        val requests = db.getAllRequests(uid)
        _requests.value = requests
      } catch (e: Exception) {
        Log.d("MyPrint", "In ViewModel, could not fetch contact requests with error $e")
      }
    }
  }

  fun sendContactRequest(userID : String){

  }

  fun updateContact(contactID: String, showOnMap: Boolean) {
    db.updateContact(contactID, showOnMap)
  }

  fun deleteContact(contactID: String) {
    Log.d("contact", "called deletecontact in VM")
    db.deleteContact(contactID)
  }
}
