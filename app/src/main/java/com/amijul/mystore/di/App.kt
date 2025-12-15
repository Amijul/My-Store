package com.amijul.mystore.di

import android.app.Application
import androidx.room.Room
import com.amijul.mystore.data.auth.AuthRepositoryImpl
import com.amijul.mystore.data.local.address.AddressDao
import com.amijul.mystore.data.local.address.AddressLocalRepositoryImpl
import com.amijul.mystore.data.local.db.LocalDatabase
import com.amijul.mystore.data.local.user.UserLocalRepositoryImpl
import com.amijul.mystore.data.remote.ProductFirestoreDataSource
import com.amijul.mystore.data.remote.StoreFirestoreDataSource
import com.amijul.mystore.domain.address.AddressLocalRepository
import com.amijul.mystore.domain.auth.AuthRepository
import com.amijul.mystore.domain.user.UserLocalRepository
import com.amijul.mystore.presentation.auth.AuthViewModel
import com.amijul.mystore.ui.account.AccountViewModel
import com.amijul.mystore.ui.account.address.EditAddressViewModel
import com.amijul.mystore.ui.account.profile.EditUserProfileViewModel
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.home.HomeViewModel
import com.amijul.mystore.ui.products.ProductListViewModel
import com.google.firebase.auth.FirebaseAuth
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

    // Doa and Database
    single<LocalDatabase> {
        Room.databaseBuilder(
            androidContext(),
            LocalDatabase::class.java,
            "my_store.db"
        ).fallbackToDestructiveMigration(true)
            .build()
    }

    single { get<LocalDatabase>().userDao() }
    single { get<LocalDatabase>().addressDao() }

    single<UserLocalRepository> { UserLocalRepositoryImpl(get()) }
    single<AddressLocalRepository> { AddressLocalRepositoryImpl(get()) }


    // Firestore singleton
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAuth.getInstance() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    // Data sources
    single { StoreFirestoreDataSource(get()) }
    single { ProductFirestoreDataSource(get()) }


    // ViewModels
    viewModel { HomeViewModel() }

    // ViewModel with parameter (storeId)
    viewModel { (storeId: String) ->
        ProductListViewModel(
            storeId = storeId,
            productRemote = get()
        )
    }

    viewModel { CartViewModel() }
    viewModel { AuthViewModel(get()) }

    viewModel {
        AccountViewModel(
            userIdProvider = { get<FirebaseAuth>().currentUser?.uid },
            userLocalRepo = get(),
        )
    }

    viewModel {
        EditAddressViewModel(
            userIdProvider = { get<FirebaseAuth>().currentUser?.uid },
            addressRepo = get(),
            getDefaultAddress = { uid ->
                // uses DAO directly; simple and fast
                get<AddressDao>().getDefault(uid)
            }
        )
    }

    viewModel {
        EditUserProfileViewModel(
            userIdProvider = { get<FirebaseAuth>().currentUser?.uid },        // provide lambda
            authEmailProvider = { get<FirebaseAuth>().currentUser?.email },      // provide lambda
            userRepo = get()
        )
    }



}