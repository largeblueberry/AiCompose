package com.largeblueberry.aicompose.feature_auth.dataLayer.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface GoogleAuthDataSource {
    fun getSignInIntent(): Intent
    fun getSignedInAccountFromIntent(data: Intent?): GoogleSignInAccount?
}