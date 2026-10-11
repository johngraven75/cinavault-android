package com.cinavault.android.agent

/**
 * A cols x rows grid over the photo with its weights worked out once, ready
 * for Canvas.drawBitmapMesh. Each frame only moves the vertices for the pose.
 */
class PortraitMesh(
    calibration: PortraitCalibration,
    val aspect: Double,
    val cols: Int = 32,
    val rows: Int = 32,
) {
    private val frame = PortraitMath.faceFrame(calibration, aspect)
    private val weights: Array<PortraitWeights>
    val vertexCount = (cols + 1) * (rows + 1)

    /**
     * Per-vertex colours the photo is multiplied by: the head keeps its natural
     * colour, the rest of the photo fades into a dim cyan hologram, and the gap
     * between the lips darkens as the mouth opens.
     */
    private val headMask = DoubleArray(vertexCount)
    private val lipGap = DoubleArray(vertexCount)

    init {
        weights = Array(vertexCount) { index ->
            val u = (index % (cols + 1)).toDouble() / cols
            val v = (index / (cols + 1)).toDouble() / rows
            PortraitMath.weights(frame, u, v, aspect)
        }
        weights.forEachIndexed { index, w ->
            headMask[index] = w.head
            lipGap[index] = w.gap
        }
    }

    /** Fills [out] (2 floats per vertex) with pixel positions on a width x height photo. */
    fun vertices(pose: PortraitPose, width: Float, height: Float, out: FloatArray) {
        require(out.size >= vertexCount * 2) { "vertex buffer too small" }
        for (index in 0 until vertexCount) {
            val u = (index % (cols + 1)).toDouble() / cols
            val v = (index / (cols + 1)).toDouble() / rows
            val point = PortraitMath.deform(frame, weights[index], u, v, pose, aspect)
            out[index * 2] = (point.x * width).toFloat()
            out[index * 2 + 1] = (point.y * height).toFloat()
        }
    }

    /** Fills [out] with ARGB colours for the pose (see [headMask]). */
    fun colors(pose: PortraitPose, out: IntArray) {
        require(out.size >= vertexCount) { "colour buffer too small" }
        for (index in 0 until vertexCount) {
            val head = headMask[index]
            val shade = 1.0 - 0.55 * lipGap[index] * pose.mouth
            // Hologram tint off the head: (80, 220, 255) at low alpha.
            val r = lerp(80.0, 255.0, head) * shade
            val g = lerp(220.0, 255.0, head) * shade
            val b = 255.0 * shade
            val a = lerp(46.0, 255.0, head)
            out[index] = (a.toInt().coerceIn(0, 255) shl 24) or
                (r.toInt().coerceIn(0, 255) shl 16) or
                (g.toInt().coerceIn(0, 255) shl 8) or
                b.toInt().coerceIn(0, 255)
        }
    }

    private fun lerp(from: Double, to: Double, t: Double) = from + (to - from) * t.coerceIn(0.0, 1.0)
}
