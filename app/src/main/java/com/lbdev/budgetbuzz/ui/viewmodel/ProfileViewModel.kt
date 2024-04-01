package com.lbdev.budgetbuzz.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbdev.budgetbuzz.data.model.Profile
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _savedProfile = MutableLiveData<Profile?>()
    val savedProfile: MutableLiveData<Profile?> = _savedProfile

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> = _imageUrl

    fun getUserProfile(uid: String): LiveData<Profile> = profileRepository.getUserProfile(uid)

    fun saveUserProfile(userProfile: Profile) {
        viewModelScope.launch {
            profileRepository.saveUserProfile(userProfile)
        }
    }
    fun saveProfilePicToStorage(image: Bitmap) {
        profileRepository.saveProfileImage(image) { imageUrl, exception ->
            if (exception != null) {
                _error.postValue(exception.localizedMessage)
            } else {
                if (!imageUrl.isNullOrEmpty()) {
                    _imageUrl.postValue(imageUrl)
                } else {
                    _error.postValue("Failed to upload image.")
                }
            }
        }
    }

    fun saveProfileToDatabase(profile: Profile) {
        profileRepository.saveProfile(profile) { success, exception ->
            if (success) {
                _savedProfile.postValue(profile)
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }

    fun getSavedProfile() {
        profileRepository.getProfile { profile, exception ->
            if (profile != null) {
                _savedProfile.postValue(profile)
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }
}