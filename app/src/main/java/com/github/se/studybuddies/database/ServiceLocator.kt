package com.github.se.studybuddies.database

import com.google.firebase.auth.FirebaseAuth

object ServiceLocator {
  private var dbRepository: DbRepository? = null
  private var currentUserUID: String? = null

  fun provideDatabase(): DbRepository {
    return dbRepository ?: DatabaseConnection()
  }

  fun setMockDatabase(dbRepository: DbRepository) {
    this.dbRepository = dbRepository
  }

  fun setCurrentUserUID(uid: String) {
    currentUserUID = uid
  }

  fun getCurrentUserUID(): String? {
    return currentUserUID ?: FirebaseAuth.getInstance().currentUser?.uid
  }

  fun reset() {
    dbRepository = null
    currentUserUID = null
  }
}
