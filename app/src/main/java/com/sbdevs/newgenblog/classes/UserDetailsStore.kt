package com.sbdevs.newgenblog.classes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class UserDetailsStore (val context: Context) {
    val Context.dataStore:DataStore<Preferences> by preferencesDataStore("pref")

    companion object{
        val USERNAME = stringPreferencesKey("USER_NAME")
        val USERIMG = stringPreferencesKey("USER_IMG")

    }

    suspend fun storeUserData(name:String,img:String){
        context.dataStore.edit {
            it[USERNAME] = name
            it[USERIMG] = img
        }
    }

    fun getName() = context.dataStore.data.map {
        it[USERNAME]?:""
    }
    fun getImg() = context.dataStore.data.map {
        it[USERIMG]?:""
    }
}