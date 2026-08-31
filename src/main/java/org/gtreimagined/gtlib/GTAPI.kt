package org.gtreimagined.gtlib

import com.mojang.datafixers.util.Either
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.LoadingModList
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.TriConsumer
import org.gtreimagined.gtlib.datagen.IGTLibProvider
import org.gtreimagined.gtlib.gui.GuiProperties
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin
import org.gtreimagined.gtlib.machine.Tier
import org.gtreimagined.gtlib.machine.types.Machine
import org.gtreimagined.gtlib.material.Material
import org.gtreimagined.gtlib.material.MaterialType
import org.gtreimagined.gtlib.ore.StoneType
import org.gtreimagined.gtlib.recipe.map.IRecipeMap
import org.gtreimagined.gtlib.registration.*
import org.gtreimagined.gtlib.util.NonNullSupplier
import org.gtreimagined.gtlib.util.TagUtils
import org.gtreimagined.gtlib.util.Utils
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.RuntimeException
import kotlin.Suppress
import kotlin.check
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.requireNotNull
import kotlin.synchronized

object GTAPI {
    private val OBJECTS: MutableMap<Class<*>, MutableMap<String, Either<ISharedGTObject, MutableMap<String, Any>>>> =
        Object2ObjectOpenHashMap()
    private val CALLBACKS = EnumMap<RegistrationEvent, MutableList<Runnable>>(RegistrationEvent::class.java)
    private val REGISTRATION_EVENTS_HANDLED: EnumSet<RegistrationEvent> = EnumSet
        .noneOf(RegistrationEvent::class.java)
    private val BLOCK_UPDATE_HANDLERS: ObjectList<IBlockUpdateEvent> = ObjectArrayList()
    private val DEFERRED_QUEUE: Int2ObjectMap<Deque<Runnable>> = Int2ObjectOpenHashMap()
    private val REPLACEMENTS: Object2ObjectMap<ResourceLocation, Supplier<Any>> =
        Object2ObjectOpenHashMap()
    private val CLASS_LOOKUP: MutableMap<String, MutableMap<String, Class<*>>> =
        Object2ObjectOpenHashMap()

    var phase: RegistrationEvent? = null
        private set

    private var INTERNAL_REGISTRAR: IGTRegistrar? = null

    fun init() {
    }

    /**
     * Internal Registry Section
     */
    private fun registerInternal(c: Class<*>, id: String, domain: String?, o: Any) {
        val present: Any?
        if (domain != null) {
            present = OBJECTS.computeIfAbsent(c) { Object2ObjectLinkedOpenHashMap() }
            check(
                present
                    .computeIfAbsent(domain) { Either.right(Object2ObjectLinkedOpenHashMap()) }
                    .map<Any?>({ null }) { t ->
                        t.put(id, o) } == null
            ) { "Class ${c.getName()}'s object: $id has already been registered by: $present" }
        } else {
            check(o is ISharedGTObject){
                "Class ${c.getName()} is not an ISharedGTObject"
            }
            val map = OBJECTS.computeIfAbsent(c) { Object2ObjectLinkedOpenHashMap() }
            val present = map.put(id, Either.left(o))
            check(present == null) {
                "Class ${c.getName()}'s object: $id has already been registered by: $present"
            }
        }
        val name = c.getSimpleName()
        CLASS_LOOKUP.computeIfAbsent(domain ?: (o as ISharedGTObject).domain) { Object2ObjectOpenHashMap() }
            .putIfAbsent(name, c)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> register(c: Class<T>, id: String, domain: String, o: Any): T {
        synchronized(OBJECTS) {
            check(allowRegistration()) { "Registering after DataDone in GTAPI - badbad!" }
            if (o is IGTObject && !o.shouldRegister()) return o as T
            if (o is ISharedGTObject && getInternal(c as Class<out ISharedGTObject>, id) != null) {
                return getInternal<ISharedGTObject?>(c, id) as T
            }
            registerInternal(c, id, if (o is ISharedGTObject) null else domain, o)
            if (o is Block && notRegistered(Block::class.java, id, domain)) registerInternal(Block::class.java, id, domain, o)
            else if (o is Item && notRegistered(Item::class.java, id, domain)) registerInternal(Item::class.java, id, domain, o)
            else if (o is IRegistryEntryProvider) {
                val changedId = if (o is Material) "material_$id" else if (o is StoneType) "stone_$id" else id
                if (notRegistered(IRegistryEntryProvider::class.java, changedId, domain)) {
                    registerInternal(IRegistryEntryProvider::class.java, changedId, domain, o)
                }
            }
            return o as T
        }
    }

    @JvmStatic
    fun <T> register(c: Class<T>, o: IGTObject): T {
        return register<T>(c, o.getId(), o.domain, o)
    }

    inline fun <reified T> register(o: IGTObject): T {
        return register(T::class.java, o)
    }

    inline fun <reified T> register(id: String, domain: String, o: Any) : T{
        return register(T::class.java, id, domain, o)
    }

    private fun notRegistered(c: Class<*>, id: String, domain: String): Boolean {
        return !GTAPI.has(c, id, domain)
    }

    private fun <T> getInternal(c: Class<T>, id: String, domain: String?): T? {
        val map = OBJECTS[c]
        if (map != null) {
            val inner = map[domain]
            if (inner != null) {
                return inner.map<T?>({ null }) { t ->
                    val o = t[id]
                    if (o == null) null else c.cast(o)
                }
            }
        }
        return null
    }

    @JvmStatic
    fun <T> get(c: Class<T>, id: String, domain: String): T? {
        val obj = getInternal(c, id, domain)
        return obj
    }

    @JvmStatic
    fun <T> get(c: Class<T>, location: ResourceLocation): T? {
        return get(c, location.path, location.namespace)
    }

    fun allowRegistration(): Boolean {
        return phase == RegistrationEvent.DATA_INIT || phase == RegistrationEvent.CLIENT_DATA_INIT || phase == RegistrationEvent.WORLDGEN_INIT
    }

    private fun <T : ISharedGTObject?> getInternal(c: Class<out T>, id: String): T? {
        val map = OBJECTS[c] ?: return null
        val obj = map[id]
        return if (obj == null) null else c.cast(
            obj.map<ISharedGTObject>({ it }) { null }
        )
    }

    @JvmStatic
    fun <T : ISharedGTObject> get(c: Class<out T>, id: String): T? {
        if (!allowRegistration()) {
            synchronized(OBJECTS) {
                return getInternal(c, id)
            }
        }
        return getInternal(c, id)
    }

    @JvmStatic
    fun <T> getOrDefault(c: Class<T>, id: String, domain: String, supplier: NonNullSupplier<out T>): T {
        val obj: Any? = get(c, id, domain)
        return if (obj != null) c.cast(obj) else supplier.get()
    }

    @JvmStatic
    fun <T> getOrThrow(c: Class<T>, id: String, domain: String, supplier: Supplier<out RuntimeException>): T {
        val obj: Any? = get(c, id, domain)
        if (obj != null) {
            return c.cast(obj)
        }
        throw supplier.get()
    }

    fun <T : ISharedGTObject> getOrThrow(c: Class<T>, id: String, supplier: Supplier<out RuntimeException>): T {
        val obj: Any? = get(c, id)
        if (obj != null) {
            return c.cast(obj)
        }
        throw supplier.get()
    }

    fun <T> has(c: Class<T?>?, id: String?, domain: String?): Boolean {
        val map = OBJECTS.get(c)
        if (map != null) {
            val either = map[domain]
            if (either == null) return false
            return either.map<Boolean?>(
                Function { t: ISharedGTObject? -> true },
                Function { t: MutableMap<String?, Any?>? -> t!!.containsKey(id) })
        }
        return false
    }

    fun <T> has(c: Class<T?>?, id: String?): Boolean {
        val map = OBJECTS.get(c)
        if (map != null) {
            val inner = map[id]
            return inner != null && inner.left().isPresent
        }
        return false
    }

    fun <T> get(className: String?, domain: String, id: String?): T? {
        val map = CLASS_LOOKUP[domain]
        if (map == null) return null
        val clazz = map[className] as Class<out T?>?
        if (clazz == null) return null
        return GTAPI.get(clazz, id, domain)
    }

    fun <T> all(className: String?, domain: String?, consumer: Consumer<T?>?) {
        synchronized(OBJECTS) {
            val map = CLASS_LOOKUP[domain]
            if (map == null) return
            val clazz = map[className] as Class<out T?>?
            if (clazz == null) return
            if (domain == null) {
                GTAPI.allInternal(clazz).forEach(consumer)
            } else {
                GTAPI.allInternal(clazz, domain)!!.forEach(consumer)
            }
        }
    }

    fun <T> get(className: String?, id: String?): T? {
        return get<T?>(className, Ref.SHARED_ID, id)
    }

    fun <T> all(c: Class<T?>): MutableList<T?> {
        if (!allowRegistration()) {
            val list: MutableList<T?>
            synchronized(OBJECTS) {
                list = allInternal<T?>(c).collect(Collectors.toList())
            }
            return list
        }
        return allInternal<T?>(c).collect(Collectors.toList())
    }

    fun <T> all(c: Class<T?>, domain: String): MutableList<T?> {
        if (!allowRegistration()) {
            val list: MutableList<T?>
            synchronized(OBJECTS) {
                list = allInternal<T?>(c, domain)!!.collect(Collectors.toList())
            }
            return list
        }
        return allInternal<T?>(c, domain)!!.collect(Collectors.toList())
    }

    private fun <T> allInternal(c: Class<T?>): Stream<T?> {
        val map = OBJECTS[c]
        return if (map == null)
            Stream.empty<T?>()
        else
            map.values.stream().flatMap<Any?> { t: Either<ISharedGTObject?, MutableMap<String?, Any?>?>? ->
                t!!.map<Stream<out Any?>?>(
                    Function { t: ISharedGTObject? -> Stream.of(t) },
                    Function { right: MutableMap<String?, Any?>? -> right!!.values.stream() })
            }.map<T?> { obj: Any? -> c.cast(obj) }
    }

    private fun <T> allInternal(c: Class<T?>, domain: String): Stream<T?>? {
        return allInternal<T?>(c)
            .filter { o: T? -> o is IGTObject && (o as IGTObject).domain == domain }
    }

    fun <T> all(c: Class<T?>, consumer: TriConsumer<T?, String?, String?>) {
        synchronized(OBJECTS) {
            val map = OBJECTS[c]
            if (map != null) {
                map.forEach { (d: String?, e: String<ISharedGTObject?, MutableMap<String?, Any?>?>?) ->
                    if (e!!.left().isPresent()) {
                        e.left().ifPresent(Consumer { o: ISharedGTObject? ->
                            consumer.accept(
                                c.cast(o),
                                o!!.domain,
                                o.getId()
                            )
                        })
                    } else {
                        e.right().ifPresent(Consumer { m: MutableMap<String?, Any?>? ->
                            m!!.forEach { (i: String?, o: Any?) ->
                                consumer.accept(c.cast(o), d, i)
                            }
                        })
                    }
                }
            }
        }
    }

    fun <T> all(c: Class<T?>, domain: String, consumer: TriConsumer<T?, String?, String?>) {
        synchronized(OBJECTS) {
            val map = OBJECTS[c]
            if (map != null) {
                map.forEach { (d: String?, e: String<ISharedGTObject?, MutableMap<String?, Any?>?>?) ->
                    if (e!!.left().isPresent()) {
                        if (domain == Ref.SHARED_ID) {
                            e.left().ifPresent(Consumer { o: ISharedGTObject? ->
                                consumer.accept(
                                    c.cast(o),
                                    o!!.domain,
                                    o.getId()
                                )
                            })
                        }
                    } else {
                        e.right().ifPresent(Consumer { m: MutableMap<String?, Any?>? ->
                            m!!.forEach { (i: String?, o: Any?) ->
                                if (d == domain) {
                                    consumer.accept(c.cast(o), d, i)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    fun <T> all(c: Class<T?>, consumer: Consumer<T?>?) {
        if (!allowRegistration()) {
            synchronized(OBJECTS) {
                allInternal<T?>(c).forEach(consumer)
            }
        } else {
            allInternal<T?>(c).forEach(consumer)
        }
    }

    fun <T> all(c: Class<T?>, domain: String, consumer: Consumer<T?>?) {
        if (allowRegistration()) {
            synchronized(OBJECTS) {
                allInternal<T?>(c, domain)!!.forEach(consumer)
            }
        } else {
            allInternal<T?>(c, domain)!!.forEach(consumer)
        }
    }

    fun <T> all(c: Class<T?>, domains: Array<String>, consumer: Consumer<T?>?) {
        if (allowRegistration()) {
            synchronized(OBJECTS) {
                for (domain in domains) {
                    allInternal<T?>(c, domain)!!.forEach(consumer)
                }
            }
        } else {
            for (domain in domains) {
                allInternal<T?>(c, domain)!!.forEach(consumer)
            }
        }
    }

    private fun runProvider(provider: IGTLibProvider) {
        LogManager.getLogger().debug("Running " + provider.name)
        provider.run()
    }

    val commonDeferredQueue: Optional<Deque<Runnable?>?>
        /**
         * DeferredWorkQueue Section
         */
        get() = Optional.ofNullable<Deque<Runnable?>?>(
            DEFERRED_QUEUE.get(
                0
            )
        )

    val clientDeferredQueue: Optional<Deque<Runnable?>?>
        get() = Optional.ofNullable<Deque<Runnable?>?>(
            DEFERRED_QUEUE.get(
                1
            )
        )

    val serverDeferredQueue: Optional<Deque<Runnable?>?>
        get() = Optional.ofNullable<Deque<Runnable?>?>(
            DEFERRED_QUEUE.get(
                2
            )
        )

    @JvmStatic
    fun runLaterCommon(vararg r: Runnable?) {
        synchronized(DEFERRED_QUEUE) {
            DEFERRED_QUEUE.computeIfAbsent(
                0,
                it.unimi.dsi.fastutil.ints.Int2ObjectFunction { q: Int ->
                    LinkedList<Runnable?>(
                        Arrays.asList<Runnable?>(*r)
                    )
                })!!.addAll(Arrays.asList<Runnable?>(*r))
        }
    }

    @JvmStatic
    fun runLaterClient(vararg r: Runnable?) {
        synchronized(DEFERRED_QUEUE) {
            DEFERRED_QUEUE.computeIfAbsent(
                1,
                it.unimi.dsi.fastutil.ints.Int2ObjectFunction { q: Int -> LinkedList<Runnable?>() })!!
                .addAll(
                    Arrays.asList<Runnable?>(*r)
                )
        }
    }

    fun runLaterServer(vararg r: Runnable?) {
        synchronized(DEFERRED_QUEUE) {
            DEFERRED_QUEUE.computeIfAbsent(
                2,
                it.unimi.dsi.fastutil.ints.Int2ObjectFunction { q: Int ->
                    LinkedList<Runnable?>(
                        Arrays.asList<Runnable?>(*r)
                    )
                })!!.addAll(Arrays.asList<Runnable?>(*r))
        }
    }

    /**
     * Registrar Section
     */
    @JvmStatic
    fun onRegistration(event: RegistrationEvent) {
        val previous: RegistrationEvent? = phase
        phase = event
        GTLib.LOGGER.info("Registration event " + event)
        val side = FMLEnvironment.dist
        if (!REGISTRATION_EVENTS_HANDLED.add(event)) {
            if (ModLoadingContext.get().activeNamespace == Ref.ID) return
            throw IllegalStateException("The RegistrationEvent " + event.name + " has already been handled")
        }
        INTERNAL_REGISTRAR!!.onRegistrationEvent(event, side)
        val list = GTAPI.all<IGTRegistrar?>(IGTRegistrar::class.java).stream()
            .sorted { c1: IGTRegistrar?, c2: IGTRegistrar? -> Integer.compare(c2!!.priority, c1!!.priority) }
            .filter { obj: IGTRegistrar? -> obj!!.isEnabled }.toList()
        list.forEach(Consumer { r: IGTRegistrar? -> r!!.onRegistrationEvent(event, side) })
        if (CALLBACKS.containsKey(event)) CALLBACKS[event]!!.forEach(Consumer { obj: Runnable? -> obj!!.run() })
        if (event == RegistrationEvent.CLIENT_DATA_INIT) phase = previous
    }

    @JvmStatic
    fun isModLoaded(modid: String): Boolean {
        if (ModList.get() == null) {
            return LoadingModList.get().mods.stream().map<String?> { obj: ModInfo? -> obj!!.modId }
                .anyMatch { anObject: String? -> modid.equals(anObject) }
        }
        return ModList.get().isLoaded(modid)
    }

    @JvmStatic
    val isClientThread: Boolean
        /**
         * For async stuff use this, otherwise use [isClientSide][GTAPI]
         * 
         * @return if the current thread is the client thread
         */
        get() = isClientSide && Minecraft.getInstance().isSameThread

    val isClientSide: Boolean
        /**
         * @return if the game is the **PHYSICAL** client, e.g. not a dedicated server.
         * @apiNote Do not use this to check if you're currently on the server thread for side-specific actions!
         * It does **NOT** work for that. Use [.isClientThread] instead.
         * @see .isClientThread
         */
        get() = FMLEnvironment.dist.isClient

    fun runOnEvent(event: RegistrationEvent?, runnable: Runnable?) {
        CALLBACKS.computeIfAbsent(event) { k: RegistrationEvent? -> ObjectArrayList<Runnable?>() }!!
            .add(runnable)
    }

    @JvmStatic
    fun addRegistrar(registrar: IGTRegistrar) {
        if (INTERNAL_REGISTRAR == null && registrar is GTLib) INTERNAL_REGISTRAR = registrar
        else if (registrar.isEnabled) {
            synchronized(OBJECTS) {
                registerInternal(IGTRegistrar::class.java, registrar.getId(), registrar.domain, registrar)
            }
        }
    }

    fun getRegistrar(id: String?): Optional<IGTRegistrar?> {
        return GTAPI.allInternal<IGTRegistrar?>(IGTRegistrar::class.java)
            .filter { t: IGTRegistrar? -> t!!.getId() == id }.findFirst()
    }

    fun isRegistrarEnabled(id: String?): Boolean {
        return getRegistrar(id).map<Boolean?>(Function { obj: IGTRegistrar? -> obj!!.isEnabled }).orElse(false)
    }

    /**
     * JEI Registry Section
     */
    @JvmOverloads
    fun registerJEICategory(
        map: IRecipeMap, gui: GuiProperties?, tier: Tier? = Tier.LV, model: ResourceLocation? = null,
        override: Boolean = true
    ) {
        if (isModLoaded(Ref.MOD_JEI) || isModLoaded(Ref.MOD_REI) || isModLoaded(Ref.MOD_EMI)) {
            GTLibRecipeViewerPlugin.registerCategory(map, gui, tier, model, override)
        }
    }

    fun registerJEICategory(map: IRecipeMap, gui: GuiProperties?, machine: Machine<*>, tier: Tier?, override: Boolean) {
        var tier = tier
        if (isModLoaded(Ref.MOD_JEI) || isModLoaded(Ref.MOD_REI) || isModLoaded(Ref.MOD_EMI)) {
            if (tier == null) tier = machine.firstTier
            GTLibRecipeViewerPlugin.registerCategory(
                map, gui, tier,
                ResourceLocation(machine.getDomain(), machine.getIdFromTier(tier)), override
            )
        }
    }

    fun registerJEICategoryWorkstation(map: IRecipeMap, machine: Machine<*>, tier: Tier?) {
        if (isModLoaded(Ref.MOD_JEI) || isModLoaded(Ref.MOD_REI) || isModLoaded(Ref.MOD_EMI)) {
            GTLibRecipeViewerPlugin.registerCategoryWorkstation(
                map,
                ResourceLocation(
                    machine.getDomain(),
                    machine.getIdFromTier(if (tier != null) tier else machine.firstTier)
                )
            )
        }
    }

    // TODO: Allow other than item.
    fun getReplacement(type: MaterialType<*>, material: Material, vararg namespaces: String?): Item? {
        if (type.getId().contains("liquid")) return null
        val tag = type.getMaterialTag(material)
        return getReplacement<Item?>(null, tag, *namespaces)
    }

    fun getReplacement(
        type: MaterialType<*>,
        material: Material,
        stone: StoneType,
        vararg namespaces: String?
    ): Item? {
        if (type.getId().contains("liquid")) return null
        val tag = TagUtils
            .getForgelikeItemTag(
                String.join(
                    "",
                    stone.id,
                    "_",
                    Utils.getConventionalMaterialType(type),
                    "/",
                    material.id
                )
            )
        return getReplacement<Item?>(null, tag, *namespaces)
    }

    /**
     * This must run after DataGenerators have ran OR when the tag jsons are
     * acknowledged. Otherwise this is useless!
     * 
     * @param originalItem Item that wants a replacement, may be null if only the
     * tag should be the only query
     * @param tag          Tag that wants a replacement (as the originalItem may
     * have multiple tags to search from)
     * @param namespaces   Namespaces of the tags to check against, by default this
     * only checks against 'minecraft' if no namespaces are
     * defined
     * @return originalItem if there's nothing found, null if there is no
     * originalItem, or an replacement
     */
    fun <T> getReplacement(originalItem: T?, tag: TagKey<T?>, vararg namespaces: String?): T? {
        requireNotNull(tag) { "GTAPI#getReplacement received a null tag!" }
        if (REPLACEMENTS.containsKey(tag.location())) return REPLACEMENTS[tag.location()]!!.get() as T? // return

        // RecipeIngredient.of(REPLACEMENTS.get(tag.getName().getPath().hashCode()),1);
        return originalItem
        // if (replacementsFound) return originalItem;
        // Set<String> checks = Sets.newHashSet(namespaces);
        // if (checks.isEmpty()) checks.add("minecraft");
        /*
         * Objects.requireNonNull(TagUtils.nc(tag).getAllElements().stream().filter(i ->
         * checks.contains(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
         * .findAny().map(i -> { REPLACEMENTS.put(tag.getName(), i); return i;
         * }).orElse(originalItem));
         */
    }

    fun <T> addReplacement(tag: ResourceLocation?, obj: Supplier<T?>) {
        REPLACEMENTS.put(tag, Supplier { obj.get() })
    }

    fun <T> addReplacement(tag: TagKey<Item?>, obj: Supplier<T?>) {
        REPLACEMENTS.put(tag.location(), Supplier { obj.get() })
    }

    fun hasReplacement(tag: TagKey<Item?>): Boolean {
        return REPLACEMENTS.containsKey(tag.location())
    }

    @JvmStatic
    fun registerBlockUpdateHandler(handler: IBlockUpdateEvent?) {
        BLOCK_UPDATE_HANDLERS.add(handler)
    }

    /**
     * COREMOD METHOD INSERTION: Runs every time when this is called:
     * 
     * //  * @see ServerWorld#notifyBlockUpdate(BlockPos, BlockState, BlockState, int)
     */
    @JvmStatic
    @Suppress("unused")
    fun onNotifyBlockUpdate(
        world: Level?, pos: BlockPos?, oldState: BlockState?, newState: BlockState?,
        flags: Int
    ) {
        BLOCK_UPDATE_HANDLERS.forEach(Consumer { h: IBlockUpdateEvent? ->
            h!!.onNotifyBlockUpdate(
                world,
                pos,
                oldState,
                newState,
                flags
            )
        })
    }

    interface IBlockUpdateEvent {
        fun onNotifyBlockUpdate(world: Level?, pos: BlockPos?, oldState: BlockState?, newState: BlockState?, flags: Int)
    }
}
