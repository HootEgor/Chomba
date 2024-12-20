package com.egorhoot.chomba.repo.impl

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.OnLineGame
import com.egorhoot.chomba.data.Room
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.data.isFull
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class OnLineGameRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val idConverter: IdConverter
): OnLineGameRepository {

    override fun isOwner(onLineGameUiState: MutableState<OnLineGameUiState>): Boolean {
        return onLineGameUiState.value.game.userList.isNotEmpty() && onLineGameUiState.value.game.userList[0].id == auth.currentUser?.uid
    }

    override fun isNonOwnerReady(onLineGameUiState: MutableState<OnLineGameUiState>): Boolean {
        return onLineGameUiState.value.game.userList.size > 1 && onLineGameUiState.value.game.userList.subList(1, onLineGameUiState.value.game.userList.size).all { it.ready }
    }

    override fun getThatUserName(): String {
        return auth.currentUser?.displayName ?: ""
    }

    override suspend fun createRoom(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val gameUser = User(id = user.uid, userPicture = user.photoUrl.toString(), name = if(user.displayName != null) user.displayName!! else "")
            val roomId = generateRoomId()
            val date = System.currentTimeMillis()
            val room = onLineGameUiState.value.copy(
                game = OnLineGame(
                    room = Room(id = roomId, private = true),
                    date = date,
                    userList = listOf(gameUser)
                )
            )

            db.collection("rooms")
                .document(roomId)
                .set(room)
                .addOnSuccessListener {
                    onLineGameUiState.value = onLineGameUiState.value.copy(
                        game = room.game,
                    )
                    profileUi.value = profileUi.value.copy(
                        alertMsg = idConverter.getString(R.string.room_created),
                        isSuccess = true
                    )
                    onResult()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                    profileUi.value = profileUi.value.copy(
                        alertMsg = idConverter.getString(R.string.error),
                        isSuccess = false
                    )
                    onResult()
                }
        }else {
            profileUi.value = profileUi.value.copy(
                alertMsg = idConverter.getString(R.string.failed_you_are_not_authenticated),
                isSuccess = false
            )
            onResult()
        }
    }

    private fun generateRoomId(
    ): String {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            val roomIdList = mutableListOf<String>()
            db.collection("rooms")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        roomIdList.add(document.id)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }

            var roomId = ""
            do {
                roomId = (0..5).map { ('0'..'9').random() }.joinToString("")
            } while (roomIdList.contains(roomId))

            return roomId
        }
        return ""
    }

    override suspend fun joinRoom(
        code: String,
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val gameUser = User(id = user.uid, userPicture = user.photoUrl.toString(), name = if(user.displayName != null) user.displayName!! else "")
            if (onLineGameUiState.value.rooms.contains(code)){
                db.collection("rooms")
                    .document(code)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            var room = document.toObject(OnLineGameUiState::class.java)
                            if (room != null) {
                                if (!room.game.isFull() || room.game.userList.find { it.id == gameUser.id } != null){
                                    if(room.game.userList.find { it.id == gameUser.id } == null){
                                        val userList = room.game.userList.toMutableList()
                                        userList.add(gameUser)
                                        room = room.copy(
                                            game = room.game.copy(
                                                userList = userList
                                            )
                                        )

                                        db.collection("rooms")
                                            .document(code)
                                            .set(room)
                                            .addOnSuccessListener {
                                                onLineGameUiState.value = onLineGameUiState.value.copy(
                                                    game = room.game
                                                )
                                                profileUi.value = profileUi.value.copy(
                                                    alertMsg = idConverter.getString(R.string.room_joined),
                                                    isSuccess = true
                                                )
                                                onResult()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("TAG", "Error adding document", e)
                                                profileUi.value = profileUi.value.copy(
                                                    alertMsg = idConverter.getString(R.string.error),
                                                    isSuccess = false
                                                )
                                                onResult()
                                            }
                                    }else{
                                        onLineGameUiState.value = onLineGameUiState.value.copy(
                                            game = room.game
                                        )
                                        profileUi.value = profileUi.value.copy(
                                            alertMsg = idConverter.getString(R.string.room_joined),
                                            isSuccess = true
                                        )
                                        onResult()
                                    }


                                }else{
                                    profileUi.value = profileUi.value.copy(
                                        alertMsg = idConverter.getString(R.string.room_full),
                                        isSuccess = false
                                    )
                                    onResult()
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error getting documents: ", exception)
                        profileUi.value = profileUi.value.copy(
                            alertMsg = idConverter.getString(R.string.error),
                            isSuccess = false
                        )
                        onResult()
                    }
            }

            profileUi.value = profileUi.value.copy(
                alertMsg = idConverter.getString(R.string.room_not_found),
                isSuccess = false
            )
            onResult()


        }else {
            profileUi.value = profileUi.value.copy(
                alertMsg = idConverter.getString(R.string.failed_you_are_not_authenticated),
                isSuccess = false
            )
            onResult()
        }
    }

    override suspend fun exitRoom(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val room = onLineGameUiState.value.game
            val userList = room.userList.filter { it.id != user.uid }
            val newRoom = room.copy(
                userList = userList
            )
            if(userList.isNotEmpty()){
                db.collection("rooms")
                    .document(room.room.id)
                    .set(newRoom)
                    .addOnSuccessListener {
                        onLineGameUiState.value = onLineGameUiState.value.copy(
                            game = newRoom
                        )
                        profileUi.value = profileUi.value.copy(
                            alertMsg = idConverter.getString(R.string.room_exited),
                            isSuccess = true
                        )
                        onResult()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                        profileUi.value = profileUi.value.copy(
                            alertMsg = idConverter.getString(R.string.error),
                            isSuccess = false
                        )
                        onResult()
                    }
            }else{
                profileUi.value = profileUi.value.copy(
                    alertMsg = idConverter.getString(R.string.room_exited),
                    isSuccess = false
                )
                onResult()
                deleteRoom(room.room.id)
            }

        }else {
            profileUi.value = profileUi.value.copy(
                alertMsg = idConverter.getString(R.string.failed_you_are_not_authenticated),
                isSuccess = false
            )
            onResult()
        }
    }

     private fun deleteRoom(
        code: String)
    {
        db.collection("rooms")
            .document(code)
            .delete()
            .addOnSuccessListener {
                Log.d("TAG", "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error deleting document", e)
            }
    }

    override suspend fun getAvailableRooms(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        db.collection("rooms")
            .get()
            .addOnSuccessListener { documents ->
                val rooms = mutableListOf<String>()
                for (document in documents) {
                    rooms.add(document.id)
                }
                onLineGameUiState.value = onLineGameUiState.value.copy(
                    rooms = rooms
                )
                profileUi.value = profileUi.value.copy(
                    alertMsg = idConverter.getString(R.string.rooms_fetched),
                    isSuccess = true
                )
                onResult()
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents: ", exception)
                profileUi.value = profileUi.value.copy(
                    alertMsg = idConverter.getString(R.string.error),
                    isSuccess = false
                )
                onResult()
            }
    }

    override suspend fun readyToPlay(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val room = onLineGameUiState.value
            val userList = room.game.userList.map {
                if (it.id == user.uid) {
                    it.copy(ready = !it.ready)
                } else {
                    it
                }
            }
            val newGame = room.game.copy(
                userList = userList
            )
            val newUiState = room.copy(
                game = newGame
            )
            db.collection("rooms")
                .document(room.game.room.id)
                .set(newUiState)
                .addOnSuccessListener {
                    onLineGameUiState.value = onLineGameUiState.value.copy(
                        game = newGame
                    )
                    profileUi.value = profileUi.value.copy(
                        alertMsg = idConverter.getString(R.string.ready),
                        isSuccess = true
                    )
                    onResult()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                    profileUi.value = profileUi.value.copy(
                        alertMsg = idConverter.getString(R.string.error),
                        isSuccess = false
                    )
                    onResult()
                }
        }else {
            profileUi.value = profileUi.value.copy(
                alertMsg = idConverter.getString(R.string.failed_you_are_not_authenticated),
                isSuccess = false
            )
            onResult()
        }
    }

    override suspend fun subscribeOnUpdates(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        // look for updates for onlineGameUiState.value.room.id
        db.collection("rooms")
            .document(onLineGameUiState.value.game.room.id)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("TAG", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (value != null && value.exists()) {
                    val room = value.toObject(OnLineGameUiState::class.java)
                    if (room != null) {
                        onLineGameUiState.value = onLineGameUiState.value.copy(
                            game = room.game
                        )
                    }
                } else {
                    Log.d("TAG", "Current data: null")
                }
            }
    }

    override suspend fun updateGame(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        //just save onLineGameUiState to db
        db.collection("rooms")
            .document(onLineGameUiState.value.game.room.id)
            .set(onLineGameUiState.value)
            .addOnSuccessListener {
                profileUi.value = profileUi.value.copy(
                    alertMsg = idConverter.getString(R.string.game_saved),
                    isSuccess = true
                )
                onResult()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
                profileUi.value = profileUi.value.copy(
                    alertMsg = idConverter.getString(R.string.error),
                    isSuccess = false
                )
                onResult()
            }
    }


}