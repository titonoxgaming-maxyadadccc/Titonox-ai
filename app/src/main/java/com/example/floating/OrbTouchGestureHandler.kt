package com.example.floating

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.abs

/**
 * Real Touch Gesture Handler for TITONOX Orb.
 *
 * Implements:
 * - 3D Orb: Real-time 3D rotation on X & Y axes with swipe fling inertia and deceleration.
 * - 2D Orb:
 *     - Horizontal drag right: Next orb design
 *     - Horizontal drag left: Previous orb design
 *     - Vertical drag up: Increase intensity (glow & pulse)
 *     - Vertical drag down: Decrease intensity
 * - Tap: Select / trigger action
 * - Double tap: Toggle animation speed / mode
 * - Long press: Open Orb Customization Studio
 *
 * Ensures gestures on the orb do not interfere with background scroll unless intentional.
 */
fun Modifier.orbTouchGestures(
    controller: OrbController,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {}
): Modifier = this
    .pointerInput(Unit) {
        detectTapGestures(
            onTap = { onTap() },
            onDoubleTap = { controller.toggleAnimationMode() },
            onLongPress = { onLongPress() }
        )
    }
    .pointerInput(controller) {
        val velocityTracker = VelocityTracker()
        var totalDragX = 0f
        var totalDragY = 0f
        val dragThreshold = 40f
        var is2DActionFired = false

        detectDragGestures(
            onDragStart = { offset ->
                velocityTracker.resetTracking()
                totalDragX = 0f
                totalDragY = 0f
                is2DActionFired = false
            },
            onDrag = { change: PointerInputChange, dragAmount ->
                change.consume()
                velocityTracker.addPosition(change.uptimeMillis, change.position)

                val is3D = controller.settings.value.theme.dimension == OrbDimension.THREE_D

                if (is3D) {
                    // Continuous real 3D rotation:
                    // Horizontal drag -> Y axis rotation
                    // Vertical drag -> X axis rotation
                    controller.onDragDelta(dragAmount.x, dragAmount.y)
                } else {
                    // 2D Orb touch interaction with threshold-based triggers
                    totalDragX += dragAmount.x
                    totalDragY += dragAmount.y

                    if (!is2DActionFired) {
                        if (abs(totalDragX) > abs(totalDragY)) {
                            if (totalDragX > dragThreshold) {
                                controller.nextOrb()
                                is2DActionFired = true
                            } else if (totalDragX < -dragThreshold) {
                                controller.previousOrb()
                                is2DActionFired = true
                            }
                        } else {
                            if (totalDragY < -dragThreshold) {
                                // Drag up -> increase intensity
                                controller.increaseIntensity()
                                is2DActionFired = true
                            } else if (totalDragY > dragThreshold) {
                                // Drag down -> decrease intensity
                                controller.decreaseIntensity()
                                is2DActionFired = true
                            }
                        }
                    }
                }
            },
            onDragEnd = {
                val is3D = controller.settings.value.theme.dimension == OrbDimension.THREE_D
                if (is3D) {
                    val velocity = velocityTracker.calculateVelocity()
                    controller.onFling(velocity.x, velocity.y)
                }
            },
            onDragCancel = {
                // Drag cancelled
            }
        )
    }
