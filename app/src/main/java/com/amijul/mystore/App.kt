package com.amijul.mystore

import android.app.Application
import com.amijul.mystore.data.remote.ProductFirestoreDataSource
import com.amijul.mystore.data.remote.StoreFirestoreDataSource
import com.amijul.mystore.ui.home.HomeViewModel
import com.amijul.mystore.ui.products.ProductListViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}

// You can move this to a separate file later if you want
private val appModule = module {

    // Firestore singleton
    single { FirebaseFirestore.getInstance() }

    // Data sources
    single { StoreFirestoreDataSource(get()) }
    single { ProductFirestoreDataSource(get()) }

    // ViewModels
    viewModel { HomeViewModel(get()) }

    // ViewModel with parameter (storeId)
    viewModel { (storeId: String) ->
        ProductListViewModel(
            storeId = storeId,
            productRemote = get()
        )
    }
}