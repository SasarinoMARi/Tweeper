package com.sasarinomari.tweeper

import android.app.Application

class Tweeper : Application() {
    companion object DataHolder {
        private var map : HashMap<String, Any> = hashMapOf()
        fun loadData(key: String, data: Any) {
            map[key] = data
        }

        fun getData(key:String) : Any? {
            if(!hasData(key)) return null
            return map[key];
        }

        fun dropData(key:String){
            map.remove(key)
        }

        fun hasData(key: String): Boolean {
            return map.containsKey(key)
        }
    }
}