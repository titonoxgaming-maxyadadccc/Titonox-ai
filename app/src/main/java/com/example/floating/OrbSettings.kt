package com.example.floating

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PlasmaBlue

enum class OrbDimension(val label: String) {
    TWO_D("2D Orbs"),
    THREE_D("3D Orbs")
}

/**
 * 30 Completely Unique TITONOX Orb Designs:
 * 15 Distinct 2D Orbs & 15 Distinct 3D Orbs.
 */
enum class OrbThemeType(
    val displayName: String,
    val dimension: OrbDimension,
    val description: String
) {
    // ==========================================
    // 15 UNIQUE 2D ORBS
    // ==========================================
    PULSE_RING(
        displayName = "Pulse Ring",
        dimension = OrbDimension.TWO_D,
        description = "Concentric shockwave rings with radiating harmonic spikes and frequency arcs"
    ),
    AUDIO_WAVE(
        displayName = "Audio Wave",
        dimension = OrbDimension.TWO_D,
        description = "Multi-band fluid sinusoidal frequency waveforms flowing horizontally and radially"
    ),
    DIGITAL_RADAR(
        displayName = "Digital Radar",
        dimension = OrbDimension.TWO_D,
        description = "360-degree sweeping sonar radar beam with target blips and polar phosphor grid"
    ),
    NEON_CIRCUIT(
        displayName = "Neon Circuit",
        dimension = OrbDimension.TWO_D,
        description = "Glowing PCB bus tracks with traveling packet pulses and solder nodes"
    ),
    HEX_CORE(
        displayName = "Hex Core",
        dimension = OrbDimension.TWO_D,
        description = "Interlocking honeycomb hexagonal matrix with center reactive power prism"
    ),
    PARTICLE_MATRIX(
        displayName = "Particle Matrix",
        dimension = OrbDimension.TWO_D,
        description = "Swarm of matrix digital particle points orbiting in gravitational physics"
    ),
    EQUALIZER_CORE(
        displayName = "Equalizer Core",
        dimension = OrbDimension.TWO_D,
        description = "Circular radial spectrum equalizer bars dynamically bouncing to audio decibels"
    ),
    INFINITY_CORE(
        displayName = "Infinity Core",
        dimension = OrbDimension.TWO_D,
        description = "Glowing lemniscate infinity ribbon with flowing energy nodes and crossing gradients"
    ),
    DIGITAL_EYE(
        displayName = "Digital Eye",
        dimension = OrbDimension.TWO_D,
        description = "Sentient cyber iris with mechanical aperture blades, targeting crosshair and pupil dilation"
    ),
    ENERGY_RING(
        displayName = "Energy Ring",
        dimension = OrbDimension.TWO_D,
        description = "Multi-layered rotating energy arcs with coronal plasma sparks and solar discharge"
    ),
    NEURAL_NETWORK(
        displayName = "Neural Network",
        dimension = OrbDimension.TWO_D,
        description = "Synaptic neuron nodes connected by pulsing axon dendrite lines firing action potentials"
    ),
    HOLOGRAPHIC_LINES(
        displayName = "Holographic Lines",
        dimension = OrbDimension.TWO_D,
        description = "Interference fringe scanlines, horizontal glitch ribbons and holographic laser grids"
    ),
    CONCENTRIC_SCANNER(
        displayName = "Concentric Scanner",
        dimension = OrbDimension.TWO_D,
        description = "Triple concentric counter-rotating ring vernier scales with dual laser scanner beams"
    ),
    STAR_CORE(
        displayName = "Star Core",
        dimension = OrbDimension.TWO_D,
        description = "Pulsating 8-point diffraction star with radiant beam flares and diamond center"
    ),
    TITONOX_DIGITAL_CORE(
        displayName = "TITONOX Digital Core",
        dimension = OrbDimension.TWO_D,
        description = "Signature dual cyber glyph rings, central digital nexus, and dynamic audio needles"
    ),

    // ==========================================
    // 15 UNIQUE 3D ORBS
    // ==========================================
    PLASMA_SPHERE(
        displayName = "Plasma Sphere",
        dimension = OrbDimension.THREE_D,
        description = "Volumetric 3D glowing sphere with undulating plasma bubbles and depth lighting"
    ),
    CRYSTAL_CORE(
        displayName = "Crystal Core",
        dimension = OrbDimension.THREE_D,
        description = "Faceted 3D rotating isometric crystal gemstone with refractive light highlights"
    ),
    QUANTUM_REACTOR(
        displayName = "Quantum Reactor",
        dimension = OrbDimension.THREE_D,
        description = "Toroidal magnetic tokamak fusion ring with 3D orbiting charge probability rings"
    ),
    MECHANICAL_REACTOR(
        displayName = "Mechanical Reactor",
        dimension = OrbDimension.THREE_D,
        description = "3D interlocking bevel gears rotating with synchronized torque around central reactor shaft"
    ),
    HOLOGRAPHIC_GLOBE(
        displayName = "Holographic Globe",
        dimension = OrbDimension.THREE_D,
        description = "3D wireframe rotating planet with latitude/longitude parallels and glowing orbit satellite"
    ),
    GALAXY_CORE(
        displayName = "Galaxy Core",
        dimension = OrbDimension.THREE_D,
        description = "3D spiral galaxy with stellar dust lanes and luminous central supermassive core"
    ),
    LIQUID_ENERGY_SPHERE(
        displayName = "Liquid Energy Sphere",
        dimension = OrbDimension.THREE_D,
        description = "Volumetric fluid metaball droplet with 3D organic surface tension waves and specular glint"
    ),
    THREE_D_NEURAL_BRAIN(
        displayName = "3D Neural Brain",
        dimension = OrbDimension.THREE_D,
        description = "3D isometric twin cerebral hemisphere mesh with depth-layered synaptic nodes and firing sparks"
    ),
    ATOMIC_CORE(
        displayName = "Atomic Core",
        dimension = OrbDimension.THREE_D,
        description = "Classic 3D Bohr-Rutherford nucleus surrounded by 3 inclined Cartesian electron orbits"
    ),
    CYBER_REACTOR(
        displayName = "Cyber Reactor",
        dimension = OrbDimension.THREE_D,
        description = "3D isometric hypercube tesseract rotating on 3 axes with glowing neon edge wireframes"
    ),
    BLACK_HOLE_CORE(
        displayName = "Black Hole Core",
        dimension = OrbDimension.THREE_D,
        description = "3D gravitational lensing event horizon with relativistic tilted accretion disk and jet plumes"
    ),
    ICE_CORE(
        displayName = "Ice Core",
        dimension = OrbDimension.THREE_D,
        description = "3D cryogenic frozen snowflake crystal with sharp glacial facets and cyan frost needles"
    ),
    FIRE_ENERGY_CORE(
        displayName = "Fire Energy Core",
        dimension = OrbDimension.THREE_D,
        description = "3D cyber flame vortex spiraling upwards in helical cylinder with rising glowing ember sparks"
    ),
    WIREFRAME_PLANET(
        displayName = "Wireframe Planet",
        dimension = OrbDimension.THREE_D,
        description = "3D geodesic sphere wireframe cage orbiting in perspective with glowing terrain vertices"
    ),
    TITONOX_3D_CORE(
        displayName = "TITONOX 3D Core",
        dimension = OrbDimension.THREE_D,
        description = "Flagship 3D gyroscope gimbal rings, floating central hypercube, and audio-reactive 3D reactor"
    );

    companion object {
        fun fromLegacyOrName(name: String?): OrbThemeType {
            if (name == null) return TITONOX_3D_CORE
            return try {
                valueOf(name)
            } catch (e: Exception) {
                // Map older legacy names gracefully
                when (name) {
                    "PLASMA_CORE" -> PLASMA_SPHERE
                    "DIGITAL_RING" -> PULSE_RING
                    "HOLOGRAPHIC_SPHERE" -> HOLOGRAPHIC_GLOBE
                    "ENERGY_REACTOR" -> QUANTUM_REACTOR
                    "AI_EYE" -> DIGITAL_EYE
                    "PARTICLE_GALAXY" -> GALAXY_CORE
                    "CYBER_CORE" -> CYBER_REACTOR
                    "LIQUID_ORB" -> LIQUID_ENERGY_SPHERE
                    "HEX_REACTOR" -> HEX_CORE
                    "QUANTUM_SPHERE" -> QUANTUM_REACTOR
                    "GLASS_ORB" -> PLASMA_SPHERE
                    "MECHANICAL_CORE" -> MECHANICAL_REACTOR
                    "WAVE_CORE" -> AUDIO_WAVE
                    "NEON_CUBE" -> CYBER_REACTOR
                    "ROTATING_RINGS" -> TITONOX_3D_CORE
                    "ENERGY_BUBBLE" -> PLASMA_SPHERE
                    "MATRIX_CORE" -> PARTICLE_MATRIX
                    "COSMIC_CORE" -> GALAXY_CORE
                    "MINIMAL_DOT" -> EQUALIZER_CORE
                    "RADAR_CORE" -> DIGITAL_RADAR
                    "PULSING_CIRCLE" -> PULSE_RING
                    "FLOATING_FLAME" -> FIRE_ENERGY_CORE
                    "DIGITAL_BRAIN" -> THREE_D_NEURAL_BRAIN
                    "CIRCUIT_CORE" -> NEON_CIRCUIT
                    "WIREFRAME_3D" -> WIREFRAME_PLANET
                    "LIGHT_TUNNEL" -> INFINITY_CORE
                    "MAGNETIC_CORE" -> QUANTUM_REACTOR
                    "TITONOX_SIGNATURE" -> TITONOX_3D_CORE
                    "SUPERNOVA_CORE" -> STAR_CORE
                    "SOLAR_FLARE" -> FIRE_ENERGY_CORE
                    else -> TITONOX_3D_CORE
                }
            }
        }
    }
}

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    SUCCESS,
    ERROR,
    OFF
}

enum class GlowPreset(val displayName: String, val multiplier: Float) {
    OFF("Off", 0.0f),
    LOW("Low", 0.4f),
    MEDIUM("Medium", 1.0f),
    HIGH("High", 1.6f),
    EXTREME("Extreme", 2.3f)
}

enum class VoicePersona(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultPitch: Float,
    val defaultRate: Float
) {
    ALPHA("alpha", "TITONOX Alpha", "Crisp, authoritative AI commander cadence", 0.95f, 1.05f),
    NOVA("nova", "TITONOX Nova", "High-energy, fast-paced futuristic intellect", 1.15f, 1.15f),
    CORE("core", "TITONOX Core", "Jarvis-style composure and tactical precision", 1.00f, 1.00f),
    CYBER("cyber", "TITONOX Cyber", "Robotic digital synthesizer modulation", 0.82f, 1.08f),
    CALM("calm", "TITONOX Calm", "Smooth ambient guidance tone", 0.92f, 0.92f),
    PULSE("pulse", "TITONOX Pulse", "Punchy modern executive presence", 1.05f, 1.10f)
}

data class OrbCustomization(
    val theme: OrbThemeType = OrbThemeType.TITONOX_3D_CORE,
    val primaryColor: Long = 0xFF00E5FF,      // ElectricCyan
    val secondaryColor: Long = 0xFF00B0FF,    // NeonBlue
    val glowColor: Long = 0xFF80D8FF,         // BrightCyan
    val particleColor: Long = 0xFFF0F6FC,     // HoloWhite
    val ringColor: Long = 0xFF2979FF,         // PlasmaBlue
    val backgroundGlow: Boolean = true,
    val glowIntensity: Float = 1.0f,          // 0.0 to 2.5
    val animationSpeed: Float = 1.0f,         // 0.3x to 2.5x
    val glowPreset: GlowPreset = GlowPreset.HIGH,
    val particleDensity: Int = 20,            // 5 to 50
    val pulseIntensity: Float = 1.0f,         // 0.3x to 2.5x
    val rotationSpeed: Float = 1.0f,          // 0.3x to 2.5x
    val sizeDp: Int = 70,                     // 48dp to 130dp
    val opacity: Float = 0.95f,               // 0.1 to 1.0
    val snapToEdge: Boolean = true,
    val isAssistantEnabled: Boolean = true,
    val isPowerOff: Boolean = false,
    val voicePersona: VoicePersona = VoicePersona.CORE,
    val speechRate: Float = 1.05f,
    val speechPitch: Float = 1.00f,
    val speechLanguage: String = "en-US"
) {
    val primaryColorCompose: Color get() = Color(primaryColor)
    val secondaryColorCompose: Color get() = Color(secondaryColor)
    val glowColorCompose: Color get() = Color(glowColor)
    val particleColorCompose: Color get() = Color(particleColor)
    val ringColorCompose: Color get() = Color(ringColor)
}
