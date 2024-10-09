package com.egorhoot.chomba.repo.impl

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.OnLineGame
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

    override suspend fun createRoom(
        onLineGameUiState: MutableState<OnLineGameUiState>,
        profileUi: MutableState<ProfileScreenUiState>,
        onResult: () -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val gameUser = User(id = user.uid)
            val roomId = generateRoomId()
            val room = onLineGameUiState.value.copy(
                game = OnLineGame(
                    roomCode = roomId,
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
            val gameUser = User(id = user.uid)
            if (onLineGameUiState.value.rooms.contains(code)){
                db.collection("rooms")
                    .document(code)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            var room = document.toObject(OnLineGameUiState::class.java)
                            if (room != null) {
                                if (!room.game.isFull()) {
                                    if(room.game.userList.find { it.id == gameUser.id } == null){
                                        val userList = room.game.userList.toMutableList()
                                        userList.add(gameUser)
                                        room = room.copy(
                                            game = room.game.copy(
                                                userList = userList
                                            )
                                        )
                                    }
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


}