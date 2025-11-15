package com.largeblueberry.aicompose.feature_auth.dataLayer.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

interface GoogleAuthDataSource {
    fun getSignInIntent(): Intent
    fun getSignedInAccountFromIntent(data: Intent?): GoogleSignInAccount?
    fun getReauthenticationIntent(): Intent
    fun signOut(): Task<Void>
}