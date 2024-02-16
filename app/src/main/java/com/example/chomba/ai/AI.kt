package com.example.chomba.ai

import android.content.Context
import android.util.Log
import com.example.chomba.data.Card
import com.example.chomba.data.CardSuit
import com.example.chomba.data.CardValue
import org.encog.Encog
import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid
import org.encog.engine.network.activation.ActivationLinear
import org.encog.engine.network.activation.ActivationReLU
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.engine.network.activation.ActivationSoftMax
import org.encog.engine.network.activation.ActivationTANH
import org.encog.ml.data.MLData
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataPair
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation
import org.encog.persist.EncogDirectoryPersistence
import java.io.File
import kotlin.random.Random


class CardEvaluator(context: Context?) {
    private val modelFileName = "neural_network_model.eg"
    private val modelFilePath: String = File(context?.filesDir, modelFileName).absolutePath
    private val inputSize = 24
    private val outputSize = 1

    private val network: BasicNetwork = createOrLoadNeuralNetwork()

    private fun createOrLoadNeuralNetwork(): BasicNetwork {
        val loadedNetwork = try {
            EncogDirectoryPersistence.loadObject(File(modelFilePath)) as BasicNetwork
        } catch (e: Exception) {
            createNeuralNetwork()
        }

        return loadedNetwork
    }
    private fun saveNeuralNetwork() {
        EncogDirectoryPersistence.saveObject(File(modelFilePath), network)
            }
    private fun createNeuralNetwork(): BasicNetwork {
        val net = BasicNetwork()

        net.addLayer(BasicLayer(ActivationSigmoid(), true, inputSize))
        net.addLayer(BasicLayer(ActivationReLU(), true, 48))
        net.addLayer(BasicLayer(ActivationSoftMax(), true, 48))
        net.addLayer(BasicLayer(ActivationSigmoid(), true, outputSize))

        //net.addLayer(BasicLayer(ActivationSigmoid(), true, inputSize))
        //net.addLayer(BasicLayer(ActivationTANH(), true, 64))
        //net.addLayer(BasicLayer(ActivationReLU(), true, 64))
        //net.addLayer(BasicLayer(ActivationTANH(), true, 64))
        //net.addLayer(BasicLayer(ActivationSigmoid(), true, outputSize))




        net.structure.finalizeStructure()
        net.reset()
        return net
    }

    fun train(hands: List<List<Card>>, actualPoints: List<Double>) {
        val dataSet = BasicMLDataSet()

        for (i in hands.indices) {
            val hand = hands[i]
            val inputArray = hand.flatMap { cardToInputArray(it) }.toDoubleArray()
            val outputArray = doubleArrayOf(actualPoints[i])

            val pair = BasicMLDataPair(BasicMLData(inputArray), BasicMLData(outputArray))
            dataSet.add(pair)
        }

        val train = ResilientPropagation(network, dataSet)

        for (i in 0..10) {
            train.iteration()
//            if(i % 10 == 0)
//                Log.d("AI_test", "Iteration: $i Error: ${train.error}")
            if (train.error < 0.01) break
        }

        train.finishTraining()
    }

    fun endTraining() {
        Encog.getInstance().shutdown()
        saveNeuralNetwork()
    }

    fun predict(hand: List<Card>): Double {
        val inputArray = hand.flatMap { cardToInputArray(it) }.toDoubleArray()
        val input = BasicMLData(inputArray)

        val output: MLData = network.compute(input)
        return output.getData(0)
    }

    fun cardToInputArray(card: Card): List<Double> {
        val inputArray = DoubleArray(inputSize) { 0.0 }

        val valueIndex = when (card.value) {
            CardValue.NINE.customValue -> 0
            CardValue.JACK.customValue -> 1
            CardValue.QUEEN.customValue -> 2
            CardValue.KING.customValue -> 3
            CardValue.TEN.customValue -> 4
            CardValue.ACE.customValue -> 5
            else -> 0
        }

        val suitIndex = when (card.suit) {
            CardSuit.CORAZON.ordinal -> 0
            CardSuit.DIAMANTE.ordinal -> 1
            CardSuit.TREBOL.ordinal -> 2
            CardSuit.PICA.ordinal -> 3
            else -> 0
        }

        val index = valueIndex + suitIndex * CardValue.values().size

        inputArray[index] = 1.0

        return inputArray.toList()
    }
}




