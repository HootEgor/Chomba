package com.example.chomba.neironkanaxuy

import com.example.chomba.data.Player

class Neironkabl9 {
}class YourPlayerListEnvironment(val playerList: List<Player>) : MDP<YourGameState, Int, Int> {
    override fun reset(): YourGameState {
        // Инициализируйте начальное состояние игры, установите игроков и руки
        // Верните начальное состояние
        val initialState = YourGameState(...)
        return initialState
    }

    override fun actionSpaceSize(): Int {
        // Верните количество доступных действий (например, True и False)
        return 2
    }

    override fun observationSpaceSize(): Int {
        // Верните размерность пространства наблюдений (состояний)
        return YourGameState.SIZE
    }

    override fun step(action: Int): Pair<YourGameState, Double> {
        // Выполните действие (например, измените состояние игры) и определите награду
        val newState = YourGameState(...) // Новое состояние после действия
        val reward = calculateReward(...) // Рассчитайте награду
        return Pair(newState, reward)
    }
}
