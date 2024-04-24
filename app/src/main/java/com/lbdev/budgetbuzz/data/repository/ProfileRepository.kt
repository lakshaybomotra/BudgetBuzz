package com.lbdev.budgetbuzz.data.repository

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lbdev.budgetbuzz.data.db.dao.UserProfileDao
import com.lbdev.budgetbuzz.data.model.Profile
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProfileRepository(private val userProfileDao: UserProfileDao) {
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val fireStoreDB = FirebaseFirestore.getInstance()

    fun getUserProfile(uid: String) = userProfileDao.getProfile(uid)

    suspend fun saveUserProfile(profile: Profile) = userProfileDao.insertProfile(profile)

    fun saveProfileImage(image: Bitmap, callback: (String?, Exception?) -> Unit) {
        val imageName = UUID.randomUUID().toString()
        val imageRef = storageRef.child("images/userProfile/$imageName")
        val baos = ByteArrayOutputStream()

        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { exception ->
                    callback(null, exception)
                    throw exception
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                callback(downloadUri, null)
            } else {
                callback(null, task.exception)
            }
        }
    }

    fun saveProfile(profile: Profile, callback: (Boolean, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference = fireStoreDB.collection("Users").document(uid)
            documentReference.set(profile)
                .addOnSuccessListener {
                    callback(true, null)
                }.addOnFailureListener { exception ->
                    callback(false, exception)
                }
        } ?: callback(false, Exception("User not logged in"))
    }

    fun getProfile(callback: (Profile?, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference = fireStoreDB.collection("Users").document(uid)
            documentReference.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profile = document.toObject(Profile::class.java)
                        callback(profile, null)
                    } else {
                        callback(null, null)
                    }
                }.addOnFailureListener { exception ->
                    callback(null, exception)
                }.addOnCanceledListener {
                    callback(null, Exception("Profile fetch cancelled"))
                }
        } ?: callback(null, Exception("User not logged in"))
    }
}