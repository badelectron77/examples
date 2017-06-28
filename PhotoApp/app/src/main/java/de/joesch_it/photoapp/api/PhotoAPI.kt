package de.joesch_it.photoapp.api

import de.joesch_it.photoapp.models.PhotoList
import retrofit2.Call
import retrofit2.http.GET


interface PhotoAPI {
    @GET("?key=5409470-4acf3f1021b7982da7f54a2ec&q=nature&image_type=photo")
    fun getPhotos() : Call<PhotoList>
}