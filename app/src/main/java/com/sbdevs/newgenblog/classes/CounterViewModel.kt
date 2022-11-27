package com.sbdevs.newgenblog.classes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CounterViewModel: ViewModel() {
    val counterLiveData = MutableLiveData<Int>(DataCountClass.clickCount)

    fun increaseCounter(){
        DataCountClass.clickCount +=1

    }
}