package com.sbdevs.newgenblog.classes

import kotlinx.coroutines.flow.Flow

interface INetworkState {
    fun observe ():Flow<Status>
     enum class Status{
         Available,UnAvailable,Losing,Lost,
     }
}