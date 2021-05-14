package com.p2m.core.internal.graph

import java.util.concurrent.atomic.AtomicInteger


internal interface Graph<N:Node, KEY> {

    var stageSize: Int

    var stageCompletedCount: AtomicInteger

    fun evaluate():Map<KEY, N>

    fun getHeadStage(): Stage<N>

    fun getTailStage(): Stage<N>

    fun eachStageBeginFromTail(block:(stage:Stage<N>)->Unit)

    fun eachStageBeginFromHead(block:(stage:Stage<N>)->Unit)

}