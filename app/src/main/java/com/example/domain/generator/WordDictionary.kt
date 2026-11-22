package com.example.domain.generator

data class WordTheme(
    val name: String,
    val words: List<String>
)

object WordDictionary {
    val themes: List<WordTheme> = listOf(
        WordTheme(
            name = "Cosmos",
            words = listOf(
                "STAR", "MOON", "ORBIT", "COMET", "SOLAR", "LUNAR", "NOVA", "GALAXY",
                "NEBULA", "QUASAR", "PULSAR", "PLANET", "METEOR", "ECLIPSE", "COSMOS",
                "ZENITH", "HORIZON", "ASTEROID", "GRAVITY", "SATURN", "JUPITER",
                "MERCURY", "VENUS", "PHOTON", "SOLSTICE", "EQUINOX", "AURORA", "CRATER",
                "SUPERNOVA", "TELESCOPE", "ASTRONOMY", "EXOPLANET", "MAGNETAR", "PERIHELION"
            )
        ),
        WordTheme(
            name = "Forest",
            words = listOf(
                "OAK", "PINE", "FERN", "MOSS", "BARK", "LEAF", "CEDAR", "BIRCH",
                "MAPLE", "WILLOW", "SPRUCE", "REDWOOD", "CANOPY", "THICKET", "TIMBER",
                "MEADOW", "GROVE", "ACORN", "BRANCH", "FOLIAGE", "SAPLING", "LICHEN",
                "FOREST", "WOODLAND", "GLADE", "STREAM", "HOLLOW", "ASPEN",
                "UNDERGROWTH", "EVERGREEN", "WILDERNESS", "SEQUOIA", "PINECONE", "CHESTNUT"
            )
        ),
        WordTheme(
            name = "Ocean",
            words = listOf(
                "WAVE", "TIDE", "REEF", "KELP", "SURF", "COVE", "ATOLL", "CORAL",
                "WHALE", "SHARK", "DOLPHIN", "ANCHOR", "HARBOR", "CURRENT", "ABYSS",
                "LAGOON", "TRENCH", "MARINER", "NAUTILUS", "PELAGIC", "ESTUARY",
                "PLANKTON", "SEABIRD", "VOYAGE", "HORIZON", "FOAM", "SHELL", "DRIFT",
                "ARCHIPELAGO", "BATHYSCAPHE", "TIDEPOOL", "SUBMARINE", "SEASCAPE", "MARITIME"
            )
        ),
        WordTheme(
            name = "Architecture",
            words = listOf(
                "ARCH", "DOME", "BEAM", "PIER", "WALL", "VAULT", "COLUMN", "FACADE",
                "ATRIUM", "PLINTH", "LINTEL", "CORNICE", "FRIEZE", "TRUSS", "PORTICO",
                "GALLERY", "TOWER", "SPIRE", "BRIDGE", "TERRACE", "ALCOVE", "PARAPET",
                "MASONRY", "PAVILION", "CANTILEVER", "MONOLITH", "ROTUNDA", "STUDIO",
                "BALUSTRADE", "COLONNADE", "MEZZANINE", "BUTTRESS", "COURTYARD", "SKYLIGHT"
            )
        ),
        WordTheme(
            name = "Music",
            words = listOf(
                "NOTE", "TONE", "CHORD", "BEAT", "REST", "SCALE", "TEMPO", "LYRIC",
                "MELODY", "RHYTHM", "HARMONY", "CADENCE", "SONATA", "FUGUE", "OCTAVE",
                "TIMBRE", "VIOLIN", "CELLO", "TRUMPET", "PRELUDE", "ENCORE", "BALLAD",
                "STACCATO", "LEGATO", "SYMPHONY", "NOCTURNE", "REFRAIN", "ACCENT",
                "CRESCENDO", "ARPEGGIO", "OVERTURE", "POLYPHONY", "RESONANCE", "ENSEMBLE"
            )
        ),
        WordTheme(
            name = "Elements",
            words = listOf(
                "IRON", "GOLD", "ZINC", "NEON", "LEAD", "ARGON", "BORON", "CARBON",
                "COBALT", "COPPER", "HELIUM", "NICKEL", "OXYGEN", "SILVER", "SODIUM",
                "QUARTZ", "GRANITE", "BASALT", "MARBLE", "OBSIDIAN", "CRYSTAL",
                "MINERAL", "TITANIUM", "PLATINUM", "SILICON", "AMBER", "TOPAZ", "JADE",
                "TUNGSTEN", "PALLADIUM", "SAPPHIRE", "TOURMALINE", "ALUMINUM", "GRAPHITE"
            )
        ),
        WordTheme(
            name = "Weather",
            words = listOf(
                "RAIN", "WIND", "MIST", "HAIL", "GALE", "SNOW", "FROST", "CLOUD",
                "STORM", "BREEZE", "SLEET", "THUNDER", "MONSOON", "CYCLONE", "TEMPEST",
                "DRIZZLE", "SQUALL", "ZEPHYR", "CUMULUS", "CIRRUS", "NIMBUS",
                "THERMAL", "BAROMETER", "ISOBAR", "CLIMATE", "BLIZZARD", "DELUGE", "FOG",
                "ATMOSPHERE", "LIGHTNING", "HURRICANE", "STRATUS", "SUNBREAK", "DOWNPOUR"
            )
        ),
        WordTheme(
            name = "Craft",
            words = listOf(
                "LOOM", "KILN", "CLAY", "WOOL", "YARN", "CHISEL", "MALLET", "ANVIL",
                "FORGE", "LATHE", "WEAVE", "STITCH", "POTTERY", "CERAMIC", "CARVING",
                "ORIGAMI", "ENAMEL", "SOLDER", "GLASS", "FABRIC", "NEEDLE", "THIMBLE",
                "WORKSHOP", "ARTISAN", "PATTERN", "SKETCH", "CANVAS", "EASEL",
                "CRAFTSMAN", "TAPESTRY", "SCULPTURE", "WOODWORK", "PARCHMENT", "MOSAIC"
            )
        ),
        WordTheme(
            name = "Culinary",
            words = listOf(
                "SALT", "HERB", "ZEST", "BROTH", "ROAST", "SAUCE", "SPICE", "THYME",
                "BASIL", "GINGER", "SAFFRON", "PEPPER", "VANILLA", "SIMMER", "BRAISE",
                "GARNISH", "PASTRY", "SOURDOUGH", "ESPRESSO", "OLIVE", "TRUFFLE",
                "CUMIN", "CARDAMOM", "OREGANO", "WHISK", "SKILLET", "HARVEST", "HONEY",
                "CARAMELIZE", "CORIANDER", "ROSEMARY", "FERMENT", "BRIOCHE", "GASTRONOMY"
            )
        ),
        WordTheme(
            name = "Literature",
            words = listOf(
                "PAGE", "BOOK", "TALE", "POEM", "PLOT", "PROSE", "VERSE", "NOVEL",
                "CHAPTER", "STANZA", "SONNET", "ESSAY", "MEMOIR", "AUTHOR", "EDITOR",
                "LEXICON", "MARGIN", "FOLIO", "INKWELL", "SCRIPT", "VOLUME", "PREFACE",
                "EPILOGUE", "METAPHOR", "SYNTAX", "ALLEGORY", "CHRONICLE", "ARCHIVE",
                "ANTHOLOGY", "MANUSCRIPT", "NARRATIVE", "SOLILOQUY", "PARAGRAPH", "VOCABULARY"
            )
        ),
        WordTheme(
            name = "Botanical",
            words = listOf(
                "ROSE", "LILY", "IRIS", "STEM", "ROOT", "SEED", "BLOOM", "PETAL",
                "POLLEN", "ORCHID", "LOTUS", "CACTUS", "BONSAI", "CLOVER", "JASMINE",
                "LAVENDER", "TULIP", "DAHLIA", "VIOLET", "MAGNOLIA", "CYPRESS",
                "BAMBOO", "NECTAR", "SPROUT", "THORN", "BOTANY", "FLORAL", "PEONY",
                "HYDRANGEA", "CHLOROPHYLL", "GERANIUM", "SUNFLOWER", "EUCALYPTUS", "CAMELLIA"
            )
        ),
        WordTheme(
            name = "Geometry",
            words = listOf(
                "LINE", "CONE", "CUBE", "EDGE", "NODE", "ANGLE", "CHORD", "PRISM",
                "SPHERE", "RADIUS", "VECTOR", "VERTEX", "MATRIX", "SPIRAL", "HELIX",
                "POLYGON", "ELLIPSE", "TANGENT", "ARC", "PLANE", "AXIS", "SYMMETRY",
                "FRACTAL", "TESSELLATE", "TORUS", "HEXAGON", "RHOMBUS", "SEGMENT",
                "ICOSAHEDRON", "PERIMETER", "PARALLELOGRAM", "HYPERBOLA", "QUADRANT", "ORTHOGONAL"
            )
        ),
        WordTheme(
            name = "Voyage",
            words = listOf(
                "PATH", "ROAD", "TREK", "MAP", "PORT", "TRAIL", "ROUTE", "ATLAS",
                "COMPASS", "SUMMIT", "VALLEY", "CANYON", "FJORD", "GLACIER", "ISLAND",
                "PLATEAU", "PASSAGE", "FRONTIER", "ODYSSEY", "WAYPOINT", "CARAVAN",
                "PASSPORT", "LANTERN", "CABIN", "RAILWAY", "EXPEDITION", "VISTA", "PEAK",
                "ITINERARY", "PENINSULA", "LONGITUDE", "LATITUDE", "LANDMARK", "CROSSROADS"
            )
        ),
        WordTheme(
            name = "Mind",
            words = listOf(
                "CALM", "MIND", "IDEA", "THOUGHT", "FOCUS", "LOGIC", "CLARITY", "INSIGHT",
                "MEMORY", "REASON", "WONDER", "WISDOM", "CONCEPT", "PUZZLE", "RIDDLE",
                "PATTERN", "SILENCE", "STILLNESS", "LUCID", "SERENE", "MINDFUL",
                "REFLECT", "PONDER", "CURIOUS", "INTENT", "BALANCE", "SOLITUDE", "SPARK",
                "CONTEMPLATE", "PERCEPTION", "COGNITION", "TRANQUIL", "AWARENESS", "DISCOVERY"
            )
        ),
        WordTheme(
            name = "Science",
            words = listOf(
                "ATOM", "CELL", "GENE", "LENS", "WAVE", "PRISM", "LASER", "PROTON",
                "NEUTRON", "ELECTRON", "MOLECULE", "CATALYST", "ISOTOPE", "ENZYME",
                "SPECTRUM", "QUANTUM", "VOLTAGE", "CURRENT", "ENTROPY", "KINETIC",
                "OSMOSIS", "FUSION", "GENOME", "NEURON", "SYNAPSE", "ORBITAL", "THEORY",
                "WAVELENGTH", "MAGNETISM", "HYPOTHESIS", "EXPERIMENT", "EQUILIBRIUM", "PHOTONICS"
            )
        ),
        WordTheme(
            name = "Motion",
            words = listOf(
                "LEAP", "SOAR", "GLIDE", "DASH", "SPIN", "SWIRL", "DRIFT", "SURGE",
                "PULSE", "SPRINT", "STRIDE", "ORBIT", "PIVOT", "SWEEP", "VAULT",
                "CASCADE", "RIPPLE", "MOMENTUM", "VELOCITY", "CADENCE", "TUMBLE",
                "ASCEND", "DESCEND", "GALLOP", "HOVER", "RECOIL", "VECTOR", "FLIGHT",
                "TRAJECTORY", "ACCELERATE", "OSCILLATE", "PENDULUM", "WHIRLWIND", "PROPULSION"
            )
        )
    )

    val allWords: List<String> by lazy {
        themes.flatMap { it.words }.distinct().sorted()
    }
}
