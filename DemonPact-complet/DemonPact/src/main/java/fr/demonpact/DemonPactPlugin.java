/*
 * DemonPact - plugin Paper 26.3 en un seul fichier source.
 * Fichiers requis dans le jar : ce fichier + plugin.yml (voir ci-dessous). config.yml et dialogues.yml
 * sont générés automatiquement au premier démarrage (contenu embarqué en bas de ce fichier).
 *
 * plugin.yml minimal :
 *   name: DemonPact
 *   version: 1.0.0
 *   main: fr.demonpact.DemonPactPlugin
 *   api-version: '26.3'
 *   softdepend: [WorldGuard]
 *   commands:
 *     demon: {description: DemonPact, aliases: [demons, pacte]}
 *   permissions:
 *     demon.admin: {default: op, children: {demon.moderator: true, demon.use: true, demon.info: true}}
 *     demon.moderator: {default: op, children: {demon.use: true, demon.info: true}}
 *     demon.use: {default: true}
 *     demon.info: {default: true}
 */
package fr.demonpact;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Point d'entrée du plugin DemonPact. */
public final class DemonPactPlugin extends JavaPlugin {
    private PlayerDataManager data;
    private CooldownManager cooldowns;
    private RegionGuard regionGuard;
    private DemonItem items;
    private Heads heads;
    private DemonManager demons;
    private PactManager pacts;
    private PowerManager powers;
    private DemonSpawner spawner;
    private ContractManager contracts;
    private DemonGUI gui;
    private DisguiseManager disguise;

    @Override
    public void onEnable() {
        writeDefault("config.yml", Defaults.CONFIG);
        writeDefault("dialogues.yml", Defaults.DIALOGUES);
        reloadConfig();
        applyDefaults();

        data = new PlayerDataManager(this);
        cooldowns = new CooldownManager();
        regionGuard = new RegionGuard(this);
        items = new DemonItem(this);
        heads = new Heads(this);
        demons = new DemonManager(this);
        pacts = new PactManager(this);
        powers = new PowerManager(this);
        spawner = new DemonSpawner(this);
        contracts = new ContractManager(this);
        gui = new DemonGUI(this);
        disguise = new DisguiseManager(this);

        data.load();
        regionGuard.load();
        demons.load();
        powers.load();
        spawner.load();
        spawner.purgeStray();

        getServer().getPluginManager().registerEvents(powers, this);
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(disguise, this);
        disguise.start();
        getServer().getPluginManager().registerEvents(new DemonListener(this), this);

        PluginCommand cmd = getCommand("demon");
        if (cmd != null) {
            DemonCommand dc = new DemonCommand(this);
            cmd.setExecutor(dc);
            cmd.setTabCompleter(dc);
        }

        // Sauvegarde périodique et réapplication de la vie maximale des joueurs déjà connectés (rechargement).
        new BukkitRunnable() {
            @Override
            public void run() { data.saveIfDirty(); }
        }.runTaskTimer(this, 1200L, 1200L);
        getServer().getOnlinePlayers().forEach(p -> {
            if (data.has(p.getUniqueId())) pacts.applyHealth(p);
        });
        getLogger().info("DemonPact activé : " + demons.all().size() + " démons.");
    }

    @Override
    public void onDisable() {
        if (disguise != null) disguise.shutdown();
        if (contracts != null) contracts.shutdown();
        if (spawner != null) spawner.shutdown();
        if (powers != null) powers.shutdown();
        if (data != null) data.saveNow();
    }

    /** Écrit un fichier par défaut (embarqué dans ce fichier source) s'il n'existe pas encore. */
    private void writeDefault(String name, String content) {
        File f = new File(getDataFolder(), name);
        if (f.exists()) return;
        try {
            getDataFolder().mkdirs();
            java.nio.file.Files.writeString(f.toPath(), content, java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            getLogger().severe("Impossible d'écrire " + name + " : " + e.getMessage());
        }
    }

    /** Les clés absentes d'un ancien config.yml reprennent la valeur par défaut embarquée. */
    private void applyDefaults() {
        getConfig().setDefaults(org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                new java.io.StringReader(Defaults.CONFIG)));
    }

    /** Recharge config.yml et dialogues.yml. */
    public void reloadAll() {
        reloadConfig();
        applyDefaults();
        regionGuard.load();
        demons.load();
        powers.load();
        spawner.load();
        getServer().getOnlinePlayers().forEach(p -> {
            if (data.has(p.getUniqueId())) pacts.applyHealth(p);
        });
    }

    public String prefix() { return getConfig().getString("prefix", "&4&l[&c&lDemonPact&4&l] &7"); }

    public PlayerDataManager data() { return data; }
    public CooldownManager cooldowns() { return cooldowns; }
    public RegionGuard regionGuard() { return regionGuard; }
    public DemonItem items() { return items; }
    public Heads heads() { return heads; }
    public DemonManager demons() { return demons; }
    public PactManager pacts() { return pacts; }
    public PowerManager powers() { return powers; }
    public DemonSpawner spawner() { return spawner; }
    public ContractManager contracts() { return contracts; }
    public DemonGUI gui() { return gui; }
    public DisguiseManager disguise() { return disguise; }
}

/** Propositions aléatoires de contrats, négociation chronométrée, acceptation et refus. */
final class ContractManager {

    /** Contrat proposé à un joueur. */
    public static final class Offer {
        public final UUID player;
        public final Demon demon;
        public final DemonContract contract;
        public final long expiresAt;
        public final DemonSpawner.Session session;
        public boolean resolved;
        /** Offrandes (têtes / objets) jetées au centre de la stell et déjà absorbées. */
        public final List<ItemStack> deposited = new ArrayList<>();
        public BukkitTask task;

        Offer(UUID player, Demon demon, DemonContract contract, long expiresAt, DemonSpawner.Session session) {
            this.player = player;
            this.demon = demon;
            this.contract = contract;
            this.expiresAt = expiresAt;
            this.session = session;
        }
    }

    private final DemonPactPlugin plugin;
    private final Map<UUID, Offer> offers = new HashMap<>();
    private final Map<String, String> lastPick = new HashMap<>();
    private final Random rnd = new Random();

    public ContractManager(DemonPactPlugin plugin) { this.plugin = plugin; }

    public Offer offerOf(UUID id) { return offers.get(id); }

    // ------------------------------------------------------------------ Proposition

    public void startOffer(DemonSpawner.Session s, Player p) {
        if (s == null || s.ending || !p.isOnline()) return;
        Demon d = s.demon;
        DemonContract c = pick(d, p);
        DemonManager dm = plugin.demons();
        if (c == null) {
            dm.say(d, dm.format("Tu n'as plus rien à m'offrir, {player}. Je m'en vais.", p, null, d), p, false);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.spawner().dismiss(s, "nothing"), 60L);
            return;
        }
        int seconds = plugin.getConfig().getInt("negotiation.time-seconds", 300);
        final Offer o = new Offer(p.getUniqueId(), d, c, System.currentTimeMillis() + seconds * 1000L, s);
        offers.put(p.getUniqueId(), o);
        s.offered.add(p.getUniqueId());
        s.offer = o;
        s.target = p.getUniqueId();
        s.waitingFirst = false;
        lastPick.put(d.id(), c.key());

        Power power = plugin.powers().power(c.power());
        dm.say(d, dm.format(d.line("propose"), p, c, d), p, false);
        dm.say(d, dm.format(d.line("sacrifice." + c.primary().name()), p, c, d), p, false);
        p.sendMessage(Txt.c(plugin.prefix() + "&7Contrat : &f" + c.describeCosts() + " &8→ " + d.color() + power.name()));
        p.sendMessage(Txt.c(plugin.prefix() + "&7Vous avez &f" + Txt.mmss(seconds)
                + " &7pour accepter ou refuser. &8(clic droit sur le démon ou /demon contract)"));
        if (needsOfferings(c)) {
            p.sendMessage(Txt.c(plugin.prefix() + "&6Offrandes : &7jetez (touche Q) &f" + offeringsText(c)
                    + " &7au &fcentre de la Demon Stell&7. Elles seront absorbées. Puis acceptez le pacte."));
        }
        Fx.sound(p.getLocation(), "entity.wither.ambient", 1f, 0.6f);

        long delay = plugin.getConfig().getLong("negotiation.open-gui-delay-ticks", 40L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!o.resolved && p.isOnline()) plugin.gui().open(p, o);
        }, delay);

        o.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (o.resolved) { cancel(); return; }
                Player pl = Bukkit.getPlayer(o.player);
                if (pl == null) { cancel(); playerLeft(o); return; }
                if (System.currentTimeMillis() >= o.expiresAt) { cancel(); expire(o); return; }
                plugin.gui().refresh(pl, o);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private DemonContract pick(Demon d, Player p) {
        List<DemonContract> ok = new ArrayList<>();
        for (DemonContract c : d.contracts()) {
            if (plugin.pacts().canTake(p.getUniqueId(), c.power()) != null) continue;
            double hp = heartsNeeded(c);
            if (hp > 0 && !plugin.pacts().canLose(p, hp)) continue;
            ok.add(c);
        }
        if (ok.isEmpty()) return null;
        String last = lastPick.get(d.id());
        if (ok.size() > 1) ok.removeIf(c -> c.key().equals(last));
        return ok.get(rnd.nextInt(ok.size()));
    }

    // ------------------------------------------------------------------ Coûts

    public double heartsNeeded(DemonContract c) {
        double hp = 0;
        for (DemonContract.Cost cost : c.costs()) {
            if (cost.type() == DemonContract.CostType.HEARTS) hp += cost.amount() * plugin.pacts().heartHealth();
        }
        return hp;
    }

    public boolean needsOfferings(DemonContract c) {
        for (DemonContract.Cost cost : c.costs()) if (cost.type() != DemonContract.CostType.HEARTS) return true;
        return false;
    }

    /** Texte des offrandes à jeter (hors cœurs). */
    public String offeringsText(DemonContract c) {
        List<String> parts = new ArrayList<>();
        for (DemonContract.Cost cost : c.costs()) {
            if (cost.type() != DemonContract.CostType.HEARTS) parts.add(DemonContract.describe(cost));
        }
        return String.join(" + ", parts);
    }

    public int depositedHeads(Offer o) {
        int n = 0;
        for (ItemStack s : o.deposited) if (s.getType() == Material.PLAYER_HEAD) n += s.getAmount();
        return n;
    }

    public int depositedItems(Offer o, Material mat) {
        int n = 0;
        ItemStack ref = new ItemStack(mat);
        for (ItemStack s : o.deposited) if (s.isSimilar(ref)) n += s.getAmount();
        return n;
    }

    /** Retourne un message d'erreur si le sacrifice n'est pas complet, sinon null. */
    public String checkCosts(Player p, Offer o) {
        DemonContract c = o.contract;
        double hp = heartsNeeded(c);
        if (hp > 0 && !plugin.pacts().canLose(p, hp)) {
            return "Ce sacrifice vous laisserait moins de "
                    + (int) (plugin.getConfig().getDouble("sacrifice.min-max-health", 6.0) / 2) + " cœurs de vie maximale.";
        }
        for (DemonContract.Cost cost : c.costs()) {
            switch (cost.type()) {
                case HEADS -> {
                    int have = depositedHeads(o);
                    if (have < cost.amount()) {
                        return "Offrande incomplète : têtes de joueurs " + have + "/" + cost.amount()
                                + ". Jetez-les au centre de la Demon Stell.";
                    }
                }
                case ITEMS -> {
                    int have = depositedItems(o, cost.material());
                    if (have < cost.amount()) {
                        return "Offrande incomplète : " + Txt.pretty(cost.material().name()) + " " + have + "/" + cost.amount()
                                + ". Jetez-les au centre de la Demon Stell.";
                    }
                }
                default -> { }
            }
        }
        return null;
    }

    private void pay(Player p, Offer o) {
        double hp = heartsNeeded(o.contract);
        o.deposited.clear();
        if (hp > 0) plugin.pacts().addLostHealth(p, hp);
    }

    // ------------------------------------------------------------------ Offrandes jetées au centre

    /** Suit un objet jeté par un joueur ayant un contrat ; il est absorbé s'il atterrit au centre de la stell. */
    public void trackDrop(Player p, Item item) {
        final Offer o = offers.get(p.getUniqueId());
        if (o == null || o.resolved || !needsOfferings(o.contract)) return;
        final Location c = plugin.spawner().center();
        if (c == null || c.getWorld() == null || !c.getWorld().equals(p.getWorld())) return;
        final double radius = plugin.getConfig().getDouble("stell.deposit-radius", 2.5);
        final Location mid = c.clone().add(0.5, 0, 0.5);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks += 4;
                if (!item.isValid() || o.resolved || ticks > 300) { cancel(); return; }
                Location l = item.getLocation();
                if (!l.getWorld().equals(mid.getWorld())) { cancel(); return; }
                if (Math.hypot(l.getX() - mid.getX(), l.getZ() - mid.getZ()) > radius || Math.abs(l.getY() - mid.getY()) > 3) return;
                if (!item.isOnGround() && item.getVelocity().lengthSquared() > 0.02 && ticks < 40) return;
                Player pl = Bukkit.getPlayer(o.player);
                if (pl == null) { cancel(); return; }
                int taken = absorb(pl, o, item);
                if (taken == 0) {
                    pl.sendMessage(Txt.c(plugin.prefix() + "&cCet objet n'est pas demandé par ce contrat (ou l'offrande est déjà complète)."));
                }
                cancel();
            }
        }.runTaskTimer(plugin, 5L, 4L);
    }

    private int absorb(Player p, Offer o, Item item) {
        ItemStack stack = item.getItemStack();
        int taken = 0;
        for (DemonContract.Cost cost : o.contract.costs()) {
            if (stack.getAmount() <= 0) break;
            int need;
            if (cost.type() == DemonContract.CostType.HEADS && plugin.heads().isGenuine(stack, p)) {
                need = cost.amount() - depositedHeads(o);
            } else if (cost.type() == DemonContract.CostType.ITEMS && stack.isSimilar(new ItemStack(cost.material()))) {
                need = cost.amount() - depositedItems(o, cost.material());
            } else {
                continue;
            }
            int take = Math.min(need, stack.getAmount());
            if (take <= 0) continue;
            ItemStack part = stack.clone();
            part.setAmount(take);
            o.deposited.add(part);
            stack.setAmount(stack.getAmount() - take);
            taken += take;
        }
        if (taken > 0) {
            Location at = item.getLocation().add(0, 0.5, 0);
            if (stack.getAmount() <= 0) item.remove();
            else item.setItemStack(stack);
            Fx.spawn(at, "soul", 20, 0.3, 0.5, 0.3, 0.05);
            Fx.spawn(at, "soul_fire_flame", 15, 0.2, 0.4, 0.2, 0.03);
            Fx.sound(at, "block.end_portal_frame.fill", 1f, 0.6f);
            Fx.sound(at, o.demon.element().sound, 0.6f, 0.7f);
            p.sendMessage(Txt.c(plugin.prefix() + "&6Offrande absorbée : &f" + progress(o)));
            if (checkOfferings(o)) {
                p.sendMessage(Txt.c(plugin.prefix() + "&aOffrande complète ! &7Cliquez sur l'émeraude pour sceller le pacte."));
                Fx.sound(at, "block.beacon.activate", 1f, 0.6f);
            }
            plugin.gui().refresh(p, o);
        }
        return taken;
    }

    private String progress(Offer o) {
        List<String> parts = new ArrayList<>();
        for (DemonContract.Cost cost : o.contract.costs()) {
            if (cost.type() == DemonContract.CostType.HEADS) parts.add("têtes " + depositedHeads(o) + "/" + cost.amount());
            else if (cost.type() == DemonContract.CostType.ITEMS) {
                parts.add(Txt.pretty(cost.material().name()) + " " + depositedItems(o, cost.material()) + "/" + cost.amount());
            }
        }
        return String.join(", ", parts);
    }

    private boolean checkOfferings(Offer o) {
        for (DemonContract.Cost cost : o.contract.costs()) {
            if (cost.type() == DemonContract.CostType.HEADS && depositedHeads(o) < cost.amount()) return false;
            if (cost.type() == DemonContract.CostType.ITEMS && depositedItems(o, cost.material()) < cost.amount()) return false;
        }
        return true;
    }

    /** Rend les offrandes déposées (refus, expiration, départ du démon). */
    private void returnDeposits(Offer o) {
        if (o.deposited.isEmpty()) return;
        Location c = plugin.spawner().center();
        if (c != null && c.getWorld() != null) {
            Location at = c.clone().add(0.5, 1, 0.5);
            for (ItemStack s : o.deposited) {
                Item it = c.getWorld().dropItem(at, s);
                it.setOwner(o.player);
            }
            Player p = Bukkit.getPlayer(o.player);
            if (p != null) p.sendMessage(Txt.c(plugin.prefix() + "&7Vos offrandes vous sont rendues au centre de la stell."));
        }
        o.deposited.clear();
    }

    // ------------------------------------------------------------------ Décisions

    public void accept(Player p) {
        Offer o = offers.get(p.getUniqueId());
        if (o == null || o.resolved) {
            p.sendMessage(Txt.c(plugin.prefix() + "&cAucun contrat ne vous attend."));
            return;
        }
        if (System.currentTimeMillis() >= o.expiresAt) { expire(o); return; }
        String err = plugin.pacts().canTake(p.getUniqueId(), o.contract.power());
        if (err == null) err = checkCosts(p, o);
        if (err != null) {
            p.sendMessage(Txt.c(plugin.prefix() + "&c" + err));
            Fx.sound(p.getLocation(), "entity.villager.no", 1f, 0.8f);
            plugin.gui().refresh(p, o);
            return;
        }
        o.resolved = true;
        if (o.task != null) o.task.cancel();
        offers.remove(p.getUniqueId());
        double hp = heartsNeeded(o.contract);
        pay(p, o);
        Pact pact = plugin.pacts().grant(p, o.demon, o.contract);
        pact.healthPaid(hp);
        plugin.data().saveNow();
        plugin.gui().close(p);
        signature(p, o.demon, o.contract, pact, hp);
        DemonManager dm = plugin.demons();
        dm.say(o.demon, dm.format(o.demon.line("accept"), p, o.contract, o.demon), p, false);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.spawner().dismiss(o.session, "accepted"), 80L);
    }

    public void refuse(Player p) {
        Offer o = offers.get(p.getUniqueId());
        if (o == null || o.resolved) return;
        finish(o);
        plugin.gui().close(p);
        p.sendMessage(Txt.c(plugin.prefix() + "&7Vous avez refusé le pacte. Aucun sacrifice n'a été effectué."));
        DemonManager dm = plugin.demons();
        dm.say(o.demon, dm.format(o.demon.line("refuse"), p, o.contract, o.demon), p, false);
        follow(o.session, plugin.getConfig().getString("negotiation.on-refuse", "DESPAWN"));
    }

    private void expire(Offer o) {
        finish(o);
        Player p = Bukkit.getPlayer(o.player);
        if (p != null) {
            plugin.gui().close(p);
            p.sendMessage(Txt.c(plugin.prefix() + "&7Le temps de négociation est écoulé."));
        }
        DemonManager dm = plugin.demons();
        dm.say(o.demon, dm.format(o.demon.line("timeout"), p, o.contract, o.demon), p, false);
        follow(o.session, plugin.getConfig().getString("negotiation.on-timeout", "NEXT_PLAYER"));
    }

    private void playerLeft(Offer o) {
        finish(o);
        DemonManager dm = plugin.demons();
        dm.say(o.demon, dm.format(o.demon.line("timeout"), null, o.contract, o.demon), null, false);
        follow(o.session, plugin.getConfig().getString("negotiation.on-timeout", "NEXT_PLAYER"));
    }

    /** Déconnexion d'un joueur ayant un contrat en attente. */
    public void onQuit(Player p) {
        Offer o = offers.get(p.getUniqueId());
        if (o != null && !o.resolved) {
            if (o.task != null) o.task.cancel();
            playerLeft(o);
        }
    }

    private void finish(Offer o) {
        o.resolved = true;
        returnDeposits(o);
        if (o.task != null) o.task.cancel();
        offers.remove(o.player);
        if (o.session != null) o.session.offer = null;
    }

    /** Suite après un refus ou une expiration : disparaître, ou se tourner vers un autre joueur. */
    private void follow(DemonSpawner.Session s, String mode) {
        if (s == null || s.ending) return;
        if ("NEXT_PLAYER".equalsIgnoreCase(mode)) {
            Player next = plugin.spawner().randomCandidate(s.offered);
            if (next != null) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> startOffer(s, next), 60L);
                return;
            }
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.spawner().dismiss(s, "declined"), 60L);
    }

    /** Annule les contrats liés à une session (renvoi du démon). */
    public void cancelSession(DemonSpawner.Session s) {
        Iterator<Offer> it = offers.values().iterator();
        List<Offer> toClose = new ArrayList<>();
        while (it.hasNext()) {
            Offer o = it.next();
            if (o.session == s) toClose.add(o);
        }
        for (Offer o : toClose) {
            finish(o);
            Player p = Bukkit.getPlayer(o.player);
            if (p != null) plugin.gui().close(p);
        }
    }

    // ------------------------------------------------------------------ Effets de signature

    private void signature(Player p, Demon d, DemonContract c, Pact pact, double hp) {
        Location l = p.getLocation();
        Power power = plugin.powers().power(c.power());
        l.getWorld().strikeLightningEffect(l);
        Fx.sound(l, "entity.wither.spawn", 0.8f, 1.4f);
        Fx.sound(l, "block.end_portal.spawn", 0.6f, 1.6f);
        Fx.spawn(l.clone().add(0, 1, 0), "explosion_emitter", 1, 0, 0, 0, 0);
        Color col = d.element().color;
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (!p.isOnline() || t >= 30) { cancel(); return; }
                Location base = p.getLocation();
                for (int i = 0; i < 3; i++) {
                    double a = t * 0.5 + i * 2.1;
                    double r = 1.6 - t * 0.04;
                    Location pt = base.clone().add(Math.cos(a) * r, t * 0.08, Math.sin(a) * r);
                    Fx.dust(pt, col, 1.6f, 3, 0.05);
                    Fx.spawn(pt, d.element().particle, 2, 0.05, 0.05, 0.05, 0.01);
                    Fx.dust(pt, Color.fromRGB(10, 0, 10), 1.3f, 2, 0.05);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.showTitle(Title.title(Txt.c(d.color() + "&lPACTE SCELLÉ"), Txt.c("&7" + power.name() + " &8| &c" + c.describeCosts())));
        p.sendMessage(Txt.c(plugin.prefix() + "&4Pacte conclu avec " + d.tag() + "&4 : " + d.color() + power.name()));
        if (hp > 0) {
            p.sendMessage(Txt.c(plugin.prefix() + "&cVous perdez définitivement &4" + (int) (hp / 2) + " ❤&c. Vie maximale actuelle : &f"
                    + (int) (plugin.pacts().currentMax(p) / 2) + " ❤"));
        }
        p.sendMessage(Txt.c(plugin.prefix() + "&7Votre objet de pacte vous a été remis. Clic droit pour utiliser le pouvoir."));
    }

    public void shutdown() {
        for (Offer o : new ArrayList<>(offers.values())) {
            if (o.task != null) o.task.cancel();
            returnDeposits(o);
        }
        offers.clear();
    }
}

/** Gestion des cooldowns par joueur et par pouvoir, plus protection anti-spam. */
final class CooldownManager {
    private final Map<UUID, Map<String, Long>> ready = new HashMap<>();
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public long remaining(UUID id, String key) {
        Map<String, Long> m = ready.get(id);
        if (m == null) return 0;
        Long t = m.get(key);
        if (t == null) return 0;
        long r = t - System.currentTimeMillis();
        if (r <= 0) {
            m.remove(key);
            return 0;
        }
        return r;
    }

    public void set(UUID id, String key, long millis) {
        if (millis <= 0) return;
        ready.computeIfAbsent(id, k -> new HashMap<>()).put(key, System.currentTimeMillis() + millis);
    }

    public void clear(UUID id) {
        ready.remove(id);
        lastUse.remove(id);
    }

    /** Retourne true si le joueur a déjà utilisé un pouvoir il y a moins de minMillis. */
    public boolean spam(UUID id, long minMillis) {
        long now = System.currentTimeMillis();
        Long last = lastUse.get(id);
        if (last != null && now - last < minMillis) return true;
        lastUse.put(id, now);
        return false;
    }
}

/** Définition d'un démon (apparence, personnalité, contrats, objet de pacte). */
final class Demon {
    private final String id;
    private final String name;
    private final String title;
    private final String color;
    private final String glow;
    private final String rarity;
    private final int powerLevel;
    private final Element element;
    private final String entity;
    private final Material helmet;
    private final double scale;
    private final boolean hover;
    private final Material itemMaterial;
    private final String itemName;
    private final List<String> itemLore;
    private final List<DemonContract> contracts;
    private final Map<String, List<String>> dialogues;

    public Demon(String id, String name, String title, String color, String glow, String rarity, int powerLevel,
                 Element element, String entity, Material helmet, double scale, boolean hover,
                 Material itemMaterial, String itemName, List<String> itemLore,
                 List<DemonContract> contracts, Map<String, List<String>> dialogues) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.color = color;
        this.glow = glow;
        this.rarity = rarity;
        this.powerLevel = powerLevel;
        this.element = element;
        this.entity = entity;
        this.helmet = helmet;
        this.scale = scale;
        this.hover = hover;
        this.itemMaterial = itemMaterial;
        this.itemName = itemName;
        this.itemLore = itemLore;
        this.contracts = contracts;
        this.dialogues = dialogues;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String title() { return title; }
    public String color() { return color; }
    public String glow() { return glow; }
    public String rarity() { return rarity; }
    public int powerLevel() { return powerLevel; }
    public Element element() { return element; }
    public String entity() { return entity; }
    public Material helmet() { return helmet; }
    public double scale() { return scale; }
    public boolean hover() { return hover; }
    public Material itemMaterial() { return itemMaterial; }
    public String itemName() { return itemName; }
    public List<String> itemLore() { return itemLore; }
    public List<DemonContract> contracts() { return contracts; }

    public DemonContract contract(String key) {
        for (DemonContract c : contracts) if (c.key().equalsIgnoreCase(key)) return c;
        return null;
    }

    /** Ligne de dialogue aléatoire pour une catégorie (appear, propose, accept, refuse, timeout, sacrifice.TYPE). */
    public String line(String key) {
        List<String> l = dialogues.get(key);
        if (l == null || l.isEmpty()) return "...";
        return l.get(ThreadLocalRandom.current().nextInt(l.size()));
    }

    /** Nom formaté avec la couleur du démon. */
    public String tag() { return color + "&l" + name; }
}

/** Commande /demon et sous-commandes joueur, modérateur et administrateur. */
final class DemonCommand implements TabExecutor {
    private final DemonPactPlugin plugin;
    private final Map<UUID, Long> abandonAsk = new HashMap<>();

    public DemonCommand(DemonPactPlugin plugin) { this.plugin = plugin; }

    private void msg(CommandSender s, String text) { s.sendMessage(Txt.c(plugin.prefix() + text)); }

    private boolean need(CommandSender s, String perm) {
        if (s.hasPermission(perm)) return true;
        msg(s, "&cVous n'avez pas la permission.");
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        String sub = a.length == 0 ? "info" : a[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "info", "help" -> { if (need(s, "demon.info")) info(s); }
            case "pact" -> { if (need(s, "demon.use") && player(s)) pact((Player) s); }
            case "power" -> { if (need(s, "demon.use") && player(s)) power((Player) s); }
            case "abandon" -> { if (need(s, "demon.use") && player(s)) abandon((Player) s, a); }
            case "item" -> { if (need(s, "demon.use") && player(s)) item((Player) s); }
            case "undisguise", "undeguise" -> { if (need(s, "demon.use") && player(s)) plugin.disguise().undisguiseCommand((Player) s); }
            case "contract" -> { if (need(s, "demon.use") && player(s)) contract((Player) s); }
            case "conditions", "requirements", "prerequis" -> { if (need(s, "demon.info")) conditions(s, a); }
            case "stell" -> { if (need(s, "demon.moderator")) stell(s, a); }
            case "summon" -> { if (need(s, "demon.moderator")) summon(s, a); }
            case "despawn" -> {
                if (need(s, "demon.moderator")) {
                    if (plugin.spawner().despawn()) msg(s, "&7Le démon est renvoyé dans les ténèbres.");
                    else msg(s, "&cAucun démon sur la Demon Stell.");
                }
            }
            case "reload" -> {
                if (need(s, "demon.admin")) {
                    plugin.reloadAll();
                    msg(s, "&aConfiguration rechargée.");
                }
            }
            case "give" -> { if (need(s, "demon.admin")) give(s, a); }
            case "remove" -> { if (need(s, "demon.admin")) remove(s, a); }
            case "restore" -> { if (need(s, "demon.admin")) restore(s, a); }
            case "setstell" -> { if (need(s, "demon.admin") && player(s)) setStell((Player) s); }
            default -> msg(s, "&cSous-commande inconnue. &7Tapez &f/demon help&7.");
        }
        return true;
    }

    private boolean player(CommandSender s) {
        if (s instanceof Player) return true;
        msg(s, "&cCette commande est réservée aux joueurs.");
        return false;
    }

    // ------------------------------------------------------------------ Joueur

    private void info(CommandSender s) {
        Location c = plugin.spawner().center();
        String where = c == null ? "&cnon définie &7(un administrateur doit utiliser /demon setstell)"
                : "&f" + c.getWorld().getName() + " &7X:&f" + c.getBlockX() + " &7Y:&f" + c.getBlockY() + " &7Z:&f" + c.getBlockZ();
        String[] lines = {
                "&4&m          &r &c&l⛧ DEMON PACT ⛧ &4&m          ",
                "&c▸ Demon Stell : &7" + where,
                "&c▸ Invocation : &7un modérateur invoque un démon avec &f/demon stell&7. Les runes s'embrasent, le démon surgit.",
                "&c▸ Rencontre : &7le démon choisit un joueur proche de l'autel et lui propose un contrat tiré au hasard.",
                "&c▸ Contrat : &7une interface s'ouvre. &aÉmeraude &7= accepter, &cBarrière &7= refuser. Vous avez &f"
                        + Txt.mmss(plugin.getConfig().getInt("negotiation.time-seconds", 300))
                        + "&7. Clic droit sur le démon ou &f/demon contract &7pour rouvrir l'interface.",
                "&c▸ Pouvoirs : &7tenez l'objet de votre pacte et faites un &fclic droit&7. Chaque pouvoir a un temps de recharge.",
                "&c▸ Têtes : &7un joueur qui meurt a 33 % de chance de laisser sa tête. &fClic droit avec une tête &7(joueur déconnecté) = vous prenez son identité 1h30 ; &f/undeguise &7pour l'annuler.",
                "&c▸ Apparition automatique : &7un démon surgit toutes les 2 h sur la stell : le premier qui lui parle (clic droit) reçoit le pacte.",
                "&c▸ Conditions et offrandes : &f/demon conditions [démon] &7(ce qu'il faut pour invoquer et quoi jeter au centre de la stell).",
                "&c▸ Pacte actuel : &f/demon pact &7et &f/demon power&7. Abandon : &f/demon abandon&7. Objet perdu : &f/demon item&7.",
                "&8Les pouvoirs sont fixes : aucun niveau, aucune évolution.",
        };
        for (String l : lines) s.sendMessage(Txt.c(l));
        if (s.hasPermission("demon.moderator")) {
            s.sendMessage(Txt.c("&6Modération : &f/demon stell [joueur]&7, &f/demon summon <démon> [joueur]&7, &f/demon despawn"));
        }
        if (s.hasPermission("demon.admin")) {
            s.sendMessage(Txt.c("&6Admin : &f/demon setstell&7, &f/demon reload&7, &f/demon give <joueur> <démon> [contrat]&7, "
                    + "&f/demon remove <joueur> [pouvoir]&7, &f/demon restore <joueur>"));
        }
    }

    private void pact(Player p) {
        List<Pact> list = plugin.pacts().pactsOf(p.getUniqueId());
        if (list.isEmpty()) {
            msg(p, "&7Vous n'avez conclu aucun pacte.");
            return;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        p.sendMessage(Txt.c("&4&m     &r &c&lVos pactes &4&m     "));
        int i = 1;
        for (Pact pact : list) {
            Demon d = plugin.demons().get(pact.demonId());
            Power pw = plugin.powers().power(pact.power());
            String col = d == null ? "&5" : d.color();
            p.sendMessage(Txt.c("&c" + i++ + ". " + col + "&l" + pact.demonId() + " &8| " + col + pw.name()
                    + " &8(" + fmt.format(new Date(pact.since())) + ")"));
            DemonContract c = d == null ? null : d.contract(pact.contractKey());
            if (c != null) p.sendMessage(Txt.c("   &7Sacrifice : &f" + c.describeCosts()));
        }
        double lost = plugin.data().get(p.getUniqueId()).lostHealth();
        p.sendMessage(Txt.c("&7Cœurs perdus : &c" + (int) (lost / 2) + " ❤ &7| Vie maximale : &f" + (int) (plugin.pacts().currentMax(p) / 2) + " ❤"));
    }

    private void power(Player p) {
        List<Pact> list = plugin.pacts().pactsOf(p.getUniqueId());
        if (list.isEmpty()) {
            msg(p, "&7Vous n'avez aucun pouvoir.");
            return;
        }
        for (Pact pact : list) {
            Power pw = plugin.powers().power(pact.power());
            Demon d = plugin.demons().get(pact.demonId());
            String col = d == null ? "&5" : d.color();
            p.sendMessage(Txt.c(col + "&l" + pw.name() + " &8— &7" + pw.description()));
            StringBuilder sb = new StringBuilder("&7");
            if (pw.type().passive()) sb.append("Passif. ");
            else {
                long rem = plugin.cooldowns().remaining(p.getUniqueId(), pact.power().name());
                sb.append("Recharge : &f").append(num(pw.cooldown())).append("s&7 (")
                        .append(rem > 0 ? "&c" + String.format(Locale.ROOT, "%.1f", rem / 1000.0) + "s restantes&7" : "&aprêt&7").append(") ");
            }
            if (pw.duration() > 0) sb.append("Durée : &f").append(num(pw.duration())).append("s&7 ");
            if (pw.range() > 0) sb.append("Portée : &f").append(num(pw.range())).append("&7 ");
            p.sendMessage(Txt.c(sb.toString()));
            for (String l : plugin.powers().bonusLines(pw)) p.sendMessage(Txt.c("   &a▸ &7" + l));
        }
        java.util.Set<String> shown = new java.util.HashSet<>();
        for (Pact pact : list) {
            if (!shown.add(pact.demonId())) continue;
            for (String l : plugin.powers().passiveLines(pact.demonId())) p.sendMessage(Txt.c("&d▸ Passif : &7" + l));
        }
        p.sendMessage(Txt.c("&8Utilisation : objet du pacte en main, clic droit. Les bonus passifs exigent l'objet dans l'inventaire."));
    }

    private String num(double v) { return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v); }

    private void abandon(Player p, String[] a) {
        if (!plugin.getConfig().getBoolean("pact.allow-abandon", true)) {
            msg(p, "&cL'abandon d'un pacte est désactivé.");
            return;
        }
        List<Pact> list = plugin.pacts().pactsOf(p.getUniqueId());
        if (list.isEmpty()) {
            msg(p, "&7Vous n'avez aucun pacte.");
            return;
        }
        boolean confirm = a.length > 1 && a[a.length - 1].equalsIgnoreCase("confirm");
        Pact target = null;
        if (list.size() == 1) target = list.get(0);
        else if (a.length > 1) {
            try {
                int idx = Integer.parseInt(a[1]);
                if (idx >= 1 && idx <= list.size()) target = list.get(idx - 1);
            } catch (NumberFormatException ignored) {
                // sélecteur invalide
            }
        }
        if (target == null) {
            msg(p, "&7Précisez le pacte : &f/demon abandon <numéro> &7(voir /demon pact).");
            return;
        }
        Long ask = abandonAsk.get(p.getUniqueId());
        if (!confirm || ask == null || System.currentTimeMillis() - ask > 30000) {
            abandonAsk.put(p.getUniqueId(), System.currentTimeMillis());
            msg(p, "&cAbandonner ce pacte supprime votre pouvoir &4sans rendre les sacrifices&c. Tapez &f/demon abandon"
                    + (list.size() > 1 ? " " + (list.indexOf(target) + 1) : "") + " confirm &cdans les 30 secondes.");
            return;
        }
        abandonAsk.remove(p.getUniqueId());
        double back = plugin.pacts().refund(p.getUniqueId(), target);
        plugin.pacts().remove(p.getUniqueId(), target);
        Fx.sound(p.getLocation(), "entity.wither.death", 0.6f, 1.2f);
        msg(p, "&7Le pacte est rompu. Votre pouvoir s'éteint.");
        if (back > 0) {
            msg(p, "&a" + (int) (back / 2) + " ❤ vous sont rendus. &7Vie maximale : &f" + (int) (plugin.pacts().currentMax(p) / 2) + " ❤");
        }
        msg(p, "&7Les attributs accordés par ce pacte sont réinitialisés.");
    }

    private void item(Player p) {
        if (!plugin.getConfig().getBoolean("items.allow-reclaim", true)) {
            msg(p, "&cLa récupération d'objet est désactivée.");
            return;
        }
        PlayerData pd = plugin.data().get(p.getUniqueId());
        long wait = plugin.getConfig().getLong("items.reclaim-cooldown-seconds", 60) * 1000;
        long left = pd.lastReclaim() + wait - System.currentTimeMillis();
        if (left > 0) {
            msg(p, "&cPatientez encore &f" + (left / 1000 + 1) + "s&c.");
            return;
        }
        int given = 0;
        for (Pact pact : new ArrayList<>(pd.pacts())) {
            if (hasValidItem(p, pact)) continue;
            if (plugin.pacts().reissue(p, pact)) given++;
        }
        if (given == 0) {
            msg(p, "&7Vous possédez déjà tous vos objets de pacte.");
            return;
        }
        pd.lastReclaim(System.currentTimeMillis());
        plugin.data().saveNow();
        msg(p, "&7Un nouvel objet de pacte vous est remis. &8L'ancien est désormais inerte.");
    }

    private boolean hasValidItem(Player p, Pact pact) {
        for (ItemStack it : p.getInventory().getContents()) {
            DemonItem.Info i = plugin.items().read(it);
            if (i != null && i.serial().equals(pact.serial())) return true;
        }
        return false;
    }

    private void conditions(CommandSender s, String[] a) {
        Location c = plugin.spawner().center();
        DemonSpawner.Session ses = plugin.spawner().session();
        int radius = plugin.getConfig().getInt("stell.target-radius", 40);
        boolean candidate = plugin.spawner().randomCandidate(null) != null;
        String ok = "&a✔ ", ko = "&c✘ ";
        if (a.length > 1) {
            Demon d = plugin.demons().get(a[1]);
            if (d == null) {
                msg(s, "&cDémon inconnu. Disponibles : " + String.join(", ", plugin.demons().all().stream().map(Demon::id).toList()));
                return;
            }
            s.sendMessage(Txt.c("&4&m     &r " + d.tag() + " &7(" + d.color() + d.title() + "&7) &4&m     "));
            s.sendMessage(Txt.c("&7Rareté : &f" + d.rarity() + " &7| Puissance : &f" + d.powerLevel() + "/10"));
            for (DemonContract ct : d.contracts()) {
                Power pw = plugin.powers().power(ct.power());
                s.sendMessage(Txt.c(d.color() + "▸ " + pw.name() + " &8➜ &f" + ct.describeCosts()));
                if (plugin.contracts().needsOfferings(ct)) {
                    s.sendMessage(Txt.c("    &6À jeter au centre de la stell : &f" + plugin.contracts().offeringsText(ct)));
                }
                for (String l : plugin.powers().bonusLines(pw)) s.sendMessage(Txt.c("    &a" + l));
            }
            for (String l : plugin.powers().passiveLines(d.id())) s.sendMessage(Txt.c("&d▸ Passif : &7" + l));
            s.sendMessage(Txt.c("&8Un contrat est tiré au hasard parmi ceux-ci à chaque invocation."));
            return;
        }
        s.sendMessage(Txt.c("&4&m     &r &c&lCONDITIONS D'INVOCATION &4&m     "));
        s.sendMessage(Txt.c((c != null ? ok : ko) + "&7Demon Stell définie" + (c == null ? " &8(admin : /demon setstell)" : "")));
        s.sendMessage(Txt.c((ses == null ? ok : ko) + "&7Aucun démon déjà présent sur l'autel"));
        s.sendMessage(Txt.c((candidate ? ok : ko) + "&7Un joueur éligible : permission &fdemon.use&7, moins de &f"
                + plugin.pacts().maxActive() + " &7pacte(s) actif(s), pas en spectateur, à &f"
                + (radius > 0 ? "moins de " + radius + " blocs de l'autel" : "n'importe quelle distance") + "&7 (même monde)"));
        s.sendMessage(Txt.c("&c▸ Invocation : &7un modérateur (&fdemon.moderator&7) tape &f/demon stell [joueur] &7ou &f/demon summon <démon> [joueur]&7."));
        long next = plugin.spawner().nextAuto();
        if (plugin.getConfig().getBoolean("auto-spawn.enabled", true)) {
            s.sendMessage(Txt.c("&c▸ Apparition automatique : &7"
                    + (next <= 0 ? "bientôt" : "dans &f" + Math.max(0, (next - System.currentTimeMillis()) / 60000) + " min")
                    + " &7(toutes les &f" + plugin.getConfig().getInt("auto-spawn.interval-minutes", 120)
                    + " min&7). Le premier joueur qui lui parle (clic droit) reçoit la proposition."));
        }
        s.sendMessage(Txt.c("&c▸ Le démon choisit un joueur éligible et lui propose un contrat tiré au hasard. Délai : &f"
                + Txt.mmss(plugin.getConfig().getInt("negotiation.time-seconds", 300)) + "&7."));
        s.sendMessage(Txt.c("&6Offrandes et sacrifices"));
        s.sendMessage(Txt.c("&6▸ Cœurs : &7retirés automatiquement à la signature (vie max minimale : &f"
                + (int) (plugin.getConfig().getDouble("sacrifice.min-max-health", 6.0) / 2) + " cœurs&7)."));
        s.sendMessage(Txt.c("&6▸ Têtes et objets : &7pendant que le contrat est ouvert, &fjetez-les (touche Q) au CENTRE de la Demon Stell"
                + " &7(rayon &f" + plugin.getConfig().getDouble("stell.deposit-radius", 2.5) + " &7blocs). Ils sont absorbés dans la limite demandée."));
        s.sendMessage(Txt.c("&6▸ &7Objets non renommés ; têtes de vrais joueurs (pas la vôtre). Quand l'offrande est complète, cliquez sur l'émeraude."));
        s.sendMessage(Txt.c("&6▸ &7Refus ou temps écoulé : vos offrandes vous sont rendues au centre de la stell."));
        s.sendMessage(Txt.c("&5Objet de pacte : &7lié à son propriétaire, impossible à jeter, poser, ranger ou utiliser en craft."));
        s.sendMessage(Txt.c("&7Détail des sacrifices d'un démon : &f/demon conditions <démon> &8(" + String.join(", ", plugin.demons().all().stream().map(Demon::id).toList()) + ")"));
    }

    private void contract(Player p) {
        ContractManager.Offer o = plugin.contracts().offerOf(p.getUniqueId());
        if (o == null || o.resolved) msg(p, "&7Aucun contrat ne vous attend.");
        else plugin.gui().open(p, o);
    }

    // ------------------------------------------------------------------ Modération

    private void stell(CommandSender s, String[] a) {
        Player target = null;
        if (a.length > 1) {
            target = Bukkit.getPlayerExact(a[1]);
            if (target == null) {
                msg(s, "&cJoueur introuvable.");
                return;
            }
        }
        Demon d = plugin.demons().random();
        if (d == null) {
            msg(s, "&cAucun démon chargé.");
            return;
        }
        String err = plugin.spawner().summon(d, target);
        if (err != null) msg(s, "&c" + err);
        else msg(s, "&7Invocation lancée... " + d.tag());
    }

    private void summon(CommandSender s, String[] a) {
        if (a.length < 2) {
            msg(s, "&cUsage : /demon summon <démon> [joueur]");
            return;
        }
        Demon d = plugin.demons().get(a[1]);
        if (d == null) {
            msg(s, "&cDémon inconnu. Disponibles : " + String.join(", ", plugin.demons().all().stream().map(Demon::id).toList()));
            return;
        }
        Player target = null;
        if (a.length > 2) {
            target = Bukkit.getPlayerExact(a[2]);
            if (target == null) {
                msg(s, "&cJoueur introuvable.");
                return;
            }
        }
        String err = plugin.spawner().summon(d, target);
        if (err != null) msg(s, "&c" + err);
        else msg(s, "&7Invocation de " + d.tag() + " &7lancée.");
    }

    // ------------------------------------------------------------------ Administration

    private void setStell(Player p) {
        plugin.spawner().setCenter(p.getLocation(), plugin.getConfig().getBoolean("stell.build-on-set", true), p);
        msg(p, "&7Demon Stell définie ici"
                + (plugin.getConfig().getBoolean("stell.build-on-set", true) ? " : construction du grand temple en cours (quelques secondes), vous serez téléporté dedans." : ". &8(construction automatique désactivée)"));
    }

    private void give(CommandSender s, String[] a) {
        if (a.length < 3) {
            msg(s, "&cUsage : /demon give <joueur> <démon> [contrat]");
            return;
        }
        Player t = Bukkit.getPlayerExact(a[1]);
        Demon d = plugin.demons().get(a[2]);
        if (t == null || d == null) {
            msg(s, "&cJoueur ou démon introuvable.");
            return;
        }
        DemonContract c = null;
        if (a.length > 3) c = d.contract(a[3]);
        else {
            List<DemonContract> ok = new ArrayList<>();
            for (DemonContract x : d.contracts()) if (plugin.pacts().canTake(t.getUniqueId(), x.power()) == null) ok.add(x);
            if (!ok.isEmpty()) c = ok.get((int) (Math.random() * ok.size()));
        }
        if (c == null) {
            msg(s, "&cContrat introuvable ou aucun pouvoir disponible pour ce joueur.");
            return;
        }
        String err = plugin.pacts().canTake(t.getUniqueId(), c.power());
        if (err != null) {
            msg(s, "&c" + t.getName() + " : " + err);
            return;
        }
        plugin.pacts().grant(t, d, c);
        Fx.spawn(t.getLocation().add(0, 1, 0), d.element().particle, 50, 0.5, 1, 0.5, 0.1);
        msg(t, "&7Un pacte avec " + d.tag() + " &7vous est accordé : " + d.color() + plugin.powers().power(c.power()).name());
        msg(s, "&7Pacte accordé à &f" + t.getName() + "&7 (" + c.power() + ").");
    }

    private void remove(CommandSender s, String[] a) {
        if (a.length < 2) {
            msg(s, "&cUsage : /demon remove <joueur> [pouvoir]");
            return;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(a[1]);
        if (!op.isOnline() && !op.hasPlayedBefore()) {
            msg(s, "&cJoueur inconnu.");
            return;
        }
        PowerType only = a.length > 2 ? PowerType.of(a[2]) : null;
        List<Pact> list = new ArrayList<>(plugin.pacts().pactsOf(op.getUniqueId()));
        int n = 0;
        for (Pact pact : list) {
            if (only != null && pact.power() != only) continue;
            plugin.pacts().refund(op.getUniqueId(), pact);
            plugin.pacts().remove(op.getUniqueId(), pact);
            n++;
        }
        msg(s, "&7" + n + " pacte(s) retiré(s) à &f" + a[1] + "&7. &8(les cœurs sacrifiés pour ces pactes sont rendus)");
    }

    private void restore(CommandSender s, String[] a) {
        if (a.length < 2) {
            msg(s, "&cUsage : /demon restore <joueur>");
            return;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(a[1]);
        if (!op.isOnline() && !op.hasPlayedBefore()) {
            msg(s, "&cJoueur inconnu.");
            return;
        }
        plugin.pacts().restoreHealth(op.getUniqueId());
        msg(s, "&7Vie maximale de &f" + a[1] + " &7restaurée.");
    }

    // ------------------------------------------------------------------ Complétion

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            List<String> subs = new ArrayList<>(List.of("help", "info", "pact", "power", "abandon", "item", "contract", "conditions", "undisguise"));
            if (s.hasPermission("demon.moderator")) subs.addAll(List.of("stell", "summon", "despawn"));
            if (s.hasPermission("demon.admin")) subs.addAll(List.of("reload", "give", "remove", "restore", "setstell"));
            for (String x : subs) if (x.startsWith(a[0].toLowerCase(Locale.ROOT))) out.add(x);
            return out;
        }
        String sub = a[0].toLowerCase(Locale.ROOT);
        if (a.length == 2) {
            if (sub.equals("summon") && s.hasPermission("demon.moderator")) {
                for (Demon d : plugin.demons().all()) if (d.id().toLowerCase().startsWith(a[1].toLowerCase())) out.add(d.id());
            } else if (List.of("stell", "give", "remove", "restore").contains(sub) && s.hasPermission("demon.moderator")) {
                for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(a[1].toLowerCase())) out.add(p.getName());
            } else if (sub.equals("conditions")) {
                for (Demon d : plugin.demons().all()) if (d.id().toLowerCase().startsWith(a[1].toLowerCase())) out.add(d.id());
            } else if (sub.equals("abandon")) {
                out.add("confirm");
            }
        } else if (a.length == 3) {
            if (sub.equals("give") && s.hasPermission("demon.admin")) {
                for (Demon d : plugin.demons().all()) if (d.id().toLowerCase().startsWith(a[2].toLowerCase())) out.add(d.id());
            } else if (sub.equals("summon") && s.hasPermission("demon.moderator")) {
                for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(a[2].toLowerCase())) out.add(p.getName());
            } else if (sub.equals("remove") && s.hasPermission("demon.admin")) {
                for (PowerType t : PowerType.values()) if (t.name().toLowerCase().startsWith(a[2].toLowerCase())) out.add(t.name());
            }
        } else if (a.length == 4 && sub.equals("give") && s.hasPermission("demon.admin")) {
            Demon d = plugin.demons().get(a[2]);
            if (d != null) for (DemonContract c : d.contracts()) if (c.key().startsWith(a[3].toLowerCase())) out.add(c.key());
        }
        return out;
    }
}

/** Un contrat proposé par un démon : un pouvoir contre un ou plusieurs sacrifices. */
final class DemonContract {
    public enum CostType { HEARTS, HEADS, ITEMS }

    public record Cost(CostType type, int amount, Material material) {}

    private final String key;
    private final String demonId;
    private final PowerType power;
    private final List<Cost> costs;
    private final List<String> sideEffects;

    public DemonContract(String key, String demonId, PowerType power, List<Cost> costs, List<String> sideEffects) {
        this.key = key;
        this.demonId = demonId;
        this.power = power;
        this.costs = List.copyOf(costs);
        this.sideEffects = List.copyOf(sideEffects);
    }

    public String key() { return key; }
    public String demonId() { return demonId; }
    public String id() { return demonId + "." + key; }
    public PowerType power() { return power; }
    public List<Cost> costs() { return costs; }
    public List<String> sideEffects() { return sideEffects; }

    public CostType primary() {
        return costs.isEmpty() ? CostType.HEARTS : costs.get(0).type();
    }

    public static String describe(Cost c) {
        return switch (c.type()) {
            case HEARTS -> c.amount() + (c.amount() > 1 ? " cœurs permanents" : " cœur permanent");
            case HEADS -> c.amount() + (c.amount() > 1 ? " têtes de joueurs" : " tête de joueur");
            case ITEMS -> c.amount() + "x " + Txt.pretty(c.material() == null ? "inconnu" : c.material().name());
        };
    }

    public String describeCosts() {
        List<String> parts = new ArrayList<>();
        for (Cost c : costs) parts.add(describe(c));
        return String.join(" + ", parts);
    }
}

/** Interface « CONTRAT DÉMONIAQUE » : objets personnalisés, boutons Émeraude / Barrière. */
final class DemonGUI implements Listener {
    private static final int SLOT_DEMON = 13;
    private static final int SLOT_POWER = 20;
    private static final int SLOT_PRICE = 22;
    private static final int SLOT_EFFECTS = 24;
    private static final int SLOT_TIME = 31;
    private static final int SLOT_ACCEPT = 38;
    private static final int SLOT_REFUSE = 42;

    /** Porteur d'inventaire servant à identifier l'interface. */
    public static final class Holder implements InventoryHolder {
        private final UUID player;
        private Inventory inventory;

        Holder(UUID player) { this.player = player; }

        public UUID player() { return player; }

        @Override
        public Inventory getInventory() { return inventory; }
    }

    private final DemonPactPlugin plugin;

    public DemonGUI(DemonPactPlugin plugin) { this.plugin = plugin; }

    public void open(Player p, ContractManager.Offer o) {
        Holder h = new Holder(p.getUniqueId());
        Inventory inv = Bukkit.createInventory(h, 45, Txt.c("&4&lCONTRAT DÉMONIAQUE"));
        h.inventory = inv;
        for (int i = 0; i < 45; i++) {
            boolean edge = i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8;
            inv.setItem(i, named(edge ? Material.RED_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        fill(inv, p, o);
        p.openInventory(inv);
        Fx.sound(p.getLocation(), "block.portal.trigger", 0.5f, 0.6f);
    }

    public void refresh(Player p, ContractManager.Offer o) {
        Inventory top = p.getOpenInventory().getTopInventory();
        if (top.getHolder(false) instanceof Holder) fill(top, p, o);
    }

    public void close(Player p) {
        if (p.getOpenInventory().getTopInventory().getHolder(false) instanceof Holder) p.closeInventory();
    }

    private void fill(Inventory inv, Player p, ContractManager.Offer o) {
        Demon d = o.demon;
        DemonContract c = o.contract;
        Power power = plugin.powers().power(c.power());

        inv.setItem(SLOT_DEMON, named(d.itemMaterial(), d.tag() + " &8| &7" + d.title(), List.of(
                "&7Rareté : &f" + d.rarity(),
                "&7Niveau de puissance : &f" + d.powerLevel() + "/10",
                "&7Élément : &f" + Txt.pretty(d.element().name()),
                "",
                d.color() + "&o« Signe, " + p.getName() + ". »")));

        List<String> pl = new ArrayList<>();
        pl.add("&7" + power.description());
        pl.add("");
        pl.add(power.type().passive() ? "&7Type : &fPassif" : "&7Activation : &fClic droit sur l'objet");
        if (power.cooldown() > 0) pl.add("&7Recharge : &f" + fmt(power.cooldown()) + "s");
        if (power.duration() > 0) pl.add("&7Durée : &f" + fmt(power.duration()) + "s");
        if (power.range() > 0) pl.add("&7Portée : &f" + fmt(power.range()) + " blocs");
        pl.add("");
        for (String l : plugin.powers().bonusLines(power)) pl.add("&a▸ &7" + l);
        pl.add("&8Pouvoir fixe : aucune évolution.");
        inv.setItem(SLOT_POWER, named(Material.ENCHANTED_BOOK, d.color() + "&l" + power.name(), pl));

        List<String> price = new ArrayList<>();
        for (DemonContract.Cost cost : c.costs()) {
            String need = DemonContract.describe(cost);
            switch (cost.type()) {
                case HEARTS -> {
                    double hp = cost.amount() * plugin.pacts().heartHealth();
                    boolean ok = plugin.pacts().canLose(p, hp);
                    price.add((ok ? "&a✔ " : "&c✘ ") + "&f" + need);
                    price.add("  &8Vie max : " + (int) (plugin.pacts().currentMax(p) / 2) + " ❤ → "
                            + (int) ((plugin.pacts().currentMax(p) - hp) / 2) + " ❤");
                }
                case HEADS -> {
                    int have = plugin.contracts().depositedHeads(o);
                    price.add((have >= cost.amount() ? "&a✔ " : "&c✘ ") + "&f" + need + " &8(" + have + "/" + cost.amount() + ")");
                }
                case ITEMS -> {
                    int have = plugin.contracts().depositedItems(o, cost.material());
                    price.add((have >= cost.amount() ? "&a✔ " : "&c✘ ") + "&f" + need + " &8(" + have + "/" + cost.amount() + ")");
                }
            }
        }
        price.add("");
        if (plugin.contracts().needsOfferings(c)) {
            price.add("");
            price.add("&6Jetez les offrandes (touche Q) au");
            price.add("&6centre de la Demon Stell : elles sont absorbées.");
        }
        price.add("&8Les cœurs ne sont prélevés qu'à la signature.");
        inv.setItem(SLOT_PRICE, named(Material.REDSTONE, "&c&lPRIX DU PACTE", price));

        List<String> fx = new ArrayList<>();
        fx.addAll(c.sideEffects());
        List<String> ps = plugin.powers().passiveLines(d.id());
        if (!ps.isEmpty()) {
            fx.add("");
            fx.add("&5Bonus passifs (objet dans l'inventaire) :");
            for (String l : ps) fx.add("&d ▸ &7" + l);
        }
        fx.add("");
        fx.add("&4Le pacte est définitif (sacrifices non remboursés).");
        inv.setItem(SLOT_EFFECTS, named(Material.NETHER_STAR, "&5&lEFFETS SECONDAIRES", fx));

        long left = Math.max(0, (o.expiresAt - System.currentTimeMillis()) / 1000);
        inv.setItem(SLOT_TIME, named(Material.CLOCK, "&e&lTEMPS RESTANT : &f" + Txt.mmss(left), List.of(
                "&7Sans réponse, le démon s'en ira.")));

        inv.setItem(SLOT_ACCEPT, named(Material.EMERALD, "&a&lACCEPTER LE PACTE", List.of(
                "&7Vous perdrez : &f" + c.describeCosts(),
                "&7Vous obtiendrez : " + d.color() + power.name())));
        inv.setItem(SLOT_REFUSE, named(Material.BARRIER, "&c&lREFUSER LE PACTE", List.of(
                "&7Aucun sacrifice ne sera effectué.")));
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(java.util.Locale.ROOT, "%.1f", v);
    }

    private static ItemStack named(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Txt.item(name));
        if (!lore.isEmpty()) meta.lore(Txt.items(lore));
        it.setItemMeta(meta);
        return it;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder(false) instanceof Holder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        int slot = e.getRawSlot();
        if (slot == SLOT_ACCEPT) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.contracts().accept(p));
        } else if (slot == SLOT_REFUSE) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.contracts().refuse(p));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder(false) instanceof Holder) e.setCancelled(true);
    }
}

/**
 * Objets de pacte. Chaque objet porte (PDC) l'id du démon, l'UUID du propriétaire et un numéro de série.
 * Seul l'objet dont le numéro de série correspond au pacte actif est valide : toute copie est inerte.
 */
final class DemonItem {
    public record Info(String demonId, UUID owner, UUID serial) {}

    private final DemonPactPlugin plugin;
    private final NamespacedKey kDemon;
    private final NamespacedKey kOwner;
    private final NamespacedKey kSerial;
    private final NamespacedKey kHead;

    public DemonItem(DemonPactPlugin plugin) {
        this.plugin = plugin;
        this.kDemon = new NamespacedKey(plugin, "demon_item");
        this.kOwner = new NamespacedKey(plugin, "owner");
        this.kSerial = new NamespacedKey(plugin, "serial");
        this.kHead = new NamespacedKey(plugin, "pvp_head");
    }

    public NamespacedKey headKey() { return kHead; }

    public ItemStack create(Demon d, Pact pact, String ownerName) {
        ItemStack it = new ItemStack(d.itemMaterial());
        ItemMeta meta = it.getItemMeta();
        Power power = plugin.powers().power(pact.power());
        meta.displayName(Txt.item(d.itemName()));
        List<String> lore = new ArrayList<>();
        for (String l : d.itemLore()) lore.add(Txt.fill(l, "owner", ownerName, "power", power.name()));
        lore.add("&8&m                              ");
        lore.add("&7Pouvoir : " + d.color() + power.name());
        lore.add("&7" + power.description());
        lore.add(power.type().passive() ? "&7Pouvoir &fpassif&7, toujours actif." : "&7Clic droit pour activer.");
        lore.add("&8Lié à &7" + ownerName);
        lore.add("&4Cet objet n'obéit qu'à son maître.");
        meta.lore(Txt.items(lore));
        meta.setMaxStackSize(1);
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(kDemon, PersistentDataType.STRING, d.id());
        pdc.set(kOwner, PersistentDataType.STRING, pact.owner().toString());
        pdc.set(kSerial, PersistentDataType.STRING, pact.serial().toString());
        it.setItemMeta(meta);
        return it;
    }

    /** Lit les informations d'un objet de pacte, ou null si ce n'en est pas un. */
    public Info read(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        PersistentDataContainer pdc = it.getItemMeta().getPersistentDataContainer();
        String d = pdc.get(kDemon, PersistentDataType.STRING);
        String o = pdc.get(kOwner, PersistentDataType.STRING);
        String s = pdc.get(kSerial, PersistentDataType.STRING);
        if (d == null || o == null || s == null) return null;
        try {
            return new Info(d, UUID.fromString(o), UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isDemonItem(ItemStack it) { return read(it) != null; }
}

/** Événements généraux : connexion, mort, têtes, protection des objets de pacte et de l'autel. */
final class DemonListener implements Listener {
    private static final Set<InventoryType> BLOCKED = Set.of(
            InventoryType.WORKBENCH, InventoryType.CRAFTING, InventoryType.ANVIL, InventoryType.SMITHING,
            InventoryType.GRINDSTONE, InventoryType.ENCHANTING, InventoryType.FURNACE, InventoryType.BLAST_FURNACE,
            InventoryType.SMOKER, InventoryType.BREWING, InventoryType.LOOM, InventoryType.STONECUTTER,
            InventoryType.CARTOGRAPHY, InventoryType.BEACON, InventoryType.LECTERN);

    private final DemonPactPlugin plugin;
    private final Map<UUID, Long> headCooldown = new HashMap<>();
    private final Map<UUID, Long> stealCooldown = new HashMap<>();
    private final Random rnd = new Random();

    public DemonListener(DemonPactPlugin plugin) { this.plugin = plugin; }

    // ------------------------------------------------------------------ Connexion / mort

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.disguise().joined(p);
        if (plugin.data().has(p.getUniqueId())) {
            plugin.data().get(p.getUniqueId()).name(p.getName());
            plugin.pacts().applyHealth(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.disguise().quit(e.getPlayer());
        plugin.contracts().onQuit(e.getPlayer());
        plugin.gui().close(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player v = e.getEntity();
        Player k = v.getKiller();
        long now = System.currentTimeMillis();
        plugin.disguise().remove(v, "&7Vous êtes mort : votre déguisement disparaît.");
        // 33 % de chance de laisser sa tête ; une fois tombée, délai de 250 s avant qu'elle puisse retomber
        boolean pvp = k != null && !k.equals(v);
        if (plugin.getConfig().getDouble("heads.drop-chance", 0.33) > 0
                && (pvp || !plugin.getConfig().getBoolean("heads.pvp-only", false))) {
            long cd = plugin.getConfig().getLong("heads.drop-cooldown-seconds", 250) * 1000L;
            Long last = headCooldown.get(v.getUniqueId());
            if ((last == null || now - last >= cd) && rnd.nextDouble() < plugin.getConfig().getDouble("heads.drop-chance", 0.33)) {
                headCooldown.put(v.getUniqueId(), now);
                v.getWorld().dropItemNaturally(v.getLocation(), plugin.heads().create(v, pvp ? "tué par " + k.getName() : "mort"));
                Fx.spawn(v.getLocation().add(0, 1, 0), "soul", 20, 0.3, 0.6, 0.3, 0.05);
            }
        }
        // 15 % de chance : la victime perd 1 cœur, le tueur le gagne ; délai de 480 s (par tueur)
        if (pvp && plugin.getConfig().getBoolean("steal.enabled", true)) {
            long cd = plugin.getConfig().getLong("steal.cooldown-seconds", 480) * 1000L;
            Long last = stealCooldown.get(k.getUniqueId());
            if ((last == null || now - last >= cd) && rnd.nextDouble() < plugin.getConfig().getDouble("steal.chance", 0.15)) {
                if (plugin.pacts().stealHeart(k, v)) stealCooldown.put(k.getUniqueId(), now);
            }
        }
        if (plugin.getConfig().getBoolean("items.keep-on-death", true) && !e.getKeepInventory()) {
            List<ItemStack> keep = new ArrayList<>();
            Iterator<ItemStack> it = e.getDrops().iterator();
            while (it.hasNext()) {
                ItemStack s = it.next();
                DemonItem.Info i = plugin.items().read(s);
                if (i != null && i.owner().equals(v.getUniqueId())) {
                    keep.add(s);
                    it.remove();
                }
            }
            if (!keep.isEmpty()) plugin.pacts().stash(v.getUniqueId(), keep);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.pacts().applyHealth(p);
            List<ItemStack> list = plugin.pacts().popStash(p.getUniqueId());
            if (list == null) return;
            for (ItemStack s : list) {
                for (ItemStack rest : p.getInventory().addItem(s).values()) {
                    Item drop = p.getWorld().dropItem(p.getLocation(), rest);
                    drop.setOwner(p.getUniqueId());
                }
            }
        }, 2L);
    }

    // ------------------------------------------------------------------ Démon sur l'autel

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractDemon(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof ItemFrame && (plugin.items().isDemonItem(e.getPlayer().getInventory().getItemInMainHand())
                || plugin.items().isDemonItem(e.getPlayer().getInventory().getItemInOffHand()))) {
            e.setCancelled(true);
            return;
        }
        if (!plugin.spawner().isDemonEntity(e.getRightClicked())) return;
        e.setCancelled(true);
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        if (plugin.spawner().claim(p)) return;
        ContractManager.Offer o = plugin.contracts().offerOf(p.getUniqueId());
        if (o != null && !o.resolved) {
            plugin.gui().open(p, o);
        } else {
            p.sendMessage(Txt.c(plugin.prefix() + "&7Le démon ne vous accorde pas un regard. Ce contrat ne vous est pas destiné."));
            Fx.sound(p.getLocation(), "entity.wither.ambient", 0.6f, 0.6f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDemonDamage(EntityDamageEvent e) {
        if (plugin.spawner().isDemonEntity(e.getEntity())) e.setCancelled(true);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent e) {
        DemonSpawner.Session s = plugin.spawner().session();
        for (Entity en : e.getEntities()) {
            boolean stray = plugin.spawner().isDemonEntity(en)
                    && (s == null || s.entity == null || !s.entity.equals(en));
            if (stray || en.getPersistentDataContainer().has(plugin.powers().tempKey(), PersistentDataType.BYTE)) en.remove();
        }
    }

    // ------------------------------------------------------------------ Protection de l'autel

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getConfig().getBoolean("stell.protect", true) && plugin.spawner().isInStell(e.getBlock().getLocation())
                && !e.getPlayer().hasPermission("demon.admin")) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.items().isDemonItem(e.getItemInHand())) {
            e.setCancelled(true);
            return;
        }
        if (plugin.getConfig().getBoolean("stell.protect", true) && plugin.spawner().isInStell(e.getBlock().getLocation())
                && !e.getPlayer().hasPermission("demon.admin")) e.setCancelled(true);
    }

    // ------------------------------------------------------------------ Protection des objets de pacte

    /** L'objet de pacte ne peut jamais être jeté ; tout autre objet jeté peut servir d'offrande. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.items().isDemonItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
            e.getPlayer().sendActionBar(Txt.c("&4Cet objet lié ne peut pas être jeté."));
            return;
        }
        plugin.contracts().trackDrop(e.getPlayer(), e.getItemDrop());
    }

    @EventHandler(ignoreCancelled = true)
    public void onStandManipulate(PlayerArmorStandManipulateEvent e) {
        if (plugin.items().isDemonItem(e.getPlayerItem())) e.setCancelled(true);
    }

    @EventHandler
    public void onSmithing(PrepareSmithingEvent e) {
        for (ItemStack s : e.getInventory().getContents()) {
            if (plugin.items().isDemonItem(s)) {
                e.setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        for (ItemStack s : e.getInventory().getMatrix()) {
            if (plugin.items().isDemonItem(s)) {
                e.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        if (plugin.items().isDemonItem(e.getInventory().getFirstItem())
                || plugin.items().isDemonItem(e.getInventory().getSecondItem())) e.setResult(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        boolean inTop = e.getClickedInventory() != null && e.getClickedInventory().equals(top);
        boolean bad = false;
        if (inTop) {
            if (plugin.items().isDemonItem(e.getCursor())) bad = true;
            if (e.getClick() == ClickType.NUMBER_KEY && e.getHotbarButton() >= 0
                    && plugin.items().isDemonItem(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()))) bad = true;
            if (e.getClick() == ClickType.SWAP_OFFHAND
                    && plugin.items().isDemonItem(e.getWhoClicked().getInventory().getItemInOffHand())) bad = true;
        } else if (e.isShiftClick() && plugin.items().isDemonItem(e.getCurrentItem())) {
            bad = true;
        }
        if (bad) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!plugin.items().isDemonItem(e.getOldCursor())) return;
        for (int slot : e.getRawSlots()) {
            if (slot < top.getSize()) {
                e.setCancelled(true);
                return;
            }
        }
    }
}

/** Charge les 12 démons depuis config.yml et dialogues.yml, et gère la parole des démons. */
final class DemonManager {
    private static final String[] DIALOGUE_KEYS = {"appear", "propose", "accept", "refuse", "timeout"};

    private final DemonPactPlugin plugin;
    private final Map<String, Demon> demons = new LinkedHashMap<>();

    public DemonManager(DemonPactPlugin plugin) { this.plugin = plugin; }

    public void load() {
        demons.clear();
        File f = new File(plugin.getDataFolder(), "dialogues.yml");
        YamlConfiguration dlg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("demons");
        if (root == null) {
            plugin.getLogger().severe("Section 'demons' absente de config.yml !");
            return;
        }
        for (String id : root.getKeys(false)) {
            try {
                Demon d = parse(id.toUpperCase(), root.getConfigurationSection(id), dlg.getConfigurationSection(id.toUpperCase()));
                demons.put(d.id(), d);
            } catch (Exception ex) {
                plugin.getLogger().warning("Démon '" + id + "' invalide : " + ex.getMessage());
            }
        }
        plugin.getLogger().info(demons.size() + " démons chargés.");
    }

    private Demon parse(String id, ConfigurationSection s, ConfigurationSection dlg) {
        if (s == null) throw new IllegalArgumentException("section vide");
        Map<String, List<String>> dialogues = new HashMap<>();
        if (dlg != null) {
            for (String k : DIALOGUE_KEYS) dialogues.put(k, dlg.getStringList(k));
            for (DemonContract.CostType t : DemonContract.CostType.values()) {
                dialogues.put("sacrifice." + t.name(), dlg.getStringList("sacrifice." + t.name()));
            }
        }
        List<DemonContract> contracts = new ArrayList<>();
        ConfigurationSection cs = s.getConfigurationSection("contracts");
        if (cs != null) {
            for (String key : cs.getKeys(false)) {
                ConfigurationSection c = cs.getConfigurationSection(key);
                if (c == null) continue;
                PowerType power = PowerType.of(c.getString("power", ""));
                if (power == null) throw new IllegalArgumentException("pouvoir inconnu dans le contrat " + key);
                List<DemonContract.Cost> costs = new ArrayList<>();
                for (Map<?, ?> m : c.getMapList("costs")) {
                    DemonContract.CostType type = DemonContract.CostType.valueOf(String.valueOf(m.get("type")).toUpperCase());
                    int amount = Math.max(1, Integer.parseInt(String.valueOf(m.get("amount"))));
                    Material mat = null;
                    if (type == DemonContract.CostType.ITEMS) {
                        mat = Material.matchMaterial(String.valueOf(m.get("material")));
                        if (mat == null) throw new IllegalArgumentException("matériau inconnu dans le contrat " + key);
                    }
                    costs.add(new DemonContract.Cost(type, amount, mat));
                }
                if (costs.isEmpty()) throw new IllegalArgumentException("contrat sans sacrifice : " + key);
                contracts.add(new DemonContract(key, id, power, costs, c.getStringList("side-effects")));
            }
        }
        if (contracts.isEmpty()) throw new IllegalArgumentException("aucun contrat");
        ConfigurationSection item = s.getConfigurationSection("item");
        Material itemMat = Material.matchMaterial(item == null ? "NETHER_STAR" : item.getString("material", "NETHER_STAR"));
        if (itemMat == null || itemMat.isAir()) itemMat = Material.NETHER_STAR;
        Material helmet = s.contains("helmet") ? Material.matchMaterial(s.getString("helmet", "")) : null;
        return new Demon(id, s.getString("name", id), s.getString("title", "Démon"),
                s.getString("color", "&c"), s.getString("glow-color", "red"), s.getString("rarity", "RARE"),
                s.getInt("power-level", 5), Element.of(s.getString("element", "VOID")),
                s.getString("entity", "zombie"), helmet, s.getDouble("scale", 1.0), s.getBoolean("hover", false),
                itemMat, item == null ? "&5Objet démoniaque" : item.getString("name", "&5Objet démoniaque"),
                item == null ? List.of() : item.getStringList("lore"), contracts, dialogues);
    }

    public Demon get(String id) { return id == null ? null : demons.get(id.toUpperCase()); }

    public List<Demon> all() { return new ArrayList<>(demons.values()); }

    public Demon random() {
        List<Demon> l = all();
        return l.isEmpty() ? null : l.get(ThreadLocalRandom.current().nextInt(l.size()));
    }

    /** Fait parler un démon : joueurs proches de l'autel + joueur concerné. */
    public void say(Demon d, String text, Player target, boolean global) {
        String msg = d.tag() + " &8» " + d.color() + "&o" + text;
        Location c = plugin.spawner().center();
        double radius = plugin.getConfig().getDouble("negotiation.chat-radius", 60);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            boolean near = c != null && c.getWorld() != null && p.getWorld().equals(c.getWorld())
                    && p.getLocation().distanceSquared(c) <= radius * radius;
            if (global || near || (target != null && p.getUniqueId().equals(target.getUniqueId()))) {
                p.sendMessage(Txt.c(msg));
            }
        }
    }

    /** Remplace les placeholders d'une ligne de dialogue. */
    public String format(String line, Player p, DemonContract c, Demon d) {
        String power = c == null ? "" : plugin.powers().power(c.power()).name();
        String cost = c == null ? "" : c.describeCosts();
        return Txt.fill(line, "player", p == null ? "mortel" : p.getName(), "demon", d.name(), "power", power, "cost", cost);
    }
}

/**
 * La Demon Stell : structure, runes, animation d'attente, animation d'invocation propre à chaque élément,
 * apparition et renvoi du démon.
 */
final class DemonSpawner {

    /** Une invocation en cours (animation, démon présent, négociation). */
    public static final class Session {
        public final Demon demon;
        public UUID target;
        public final Set<UUID> offered = new HashSet<>();
        public ContractManager.Offer offer;
        public LivingEntity entity;
        public boolean animating;
        public boolean active;
        public boolean ending;
        /** Apparition automatique : attend que le premier joueur lui parle. */
        public boolean waitingFirst;

        Session(Demon demon) { this.demon = demon; }
    }

    private static final Material RUNE_DIM = Material.OBSIDIAN;
    private static final Material RUNE_LIT = Material.CRYING_OBSIDIAN;
    private static final Material[] FLOOR = {
            Material.BLACKSTONE, Material.POLISHED_BLACKSTONE, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
            Material.BLACKSTONE, Material.POLISHED_BLACKSTONE_BRICKS
    };

    private final DemonPactPlugin plugin;
    private final NamespacedKey demonKey;
    private final Random rnd = new Random();
    private final List<int[]> runes = new ArrayList<>();
    private final List<int[]> pillarOffsets = new ArrayList<>();
    private Location center;
    private int lit;
    private Session session;
    private BukkitTask idleTask;
    private int idleTick;
    private long nextAuto;
    private BukkitTask autoTask;
    private boolean building;
    private UUID pendingTp;
    private static final int PODIUM = 3;
    private static final int PLAZA = 10;
    private static final int DRUM = 6;

    public DemonSpawner(DemonPactPlugin plugin) {
        this.plugin = plugin;
        this.demonKey = new NamespacedKey(plugin, "demon_entity");
    }

    public NamespacedKey demonKey() { return demonKey; }

    public Location center() { return center == null ? null : center.clone(); }

    public Session session() { return session; }

    public int radius() { return Math.max(16, Math.min(48, plugin.getConfig().getInt("stell.temple-length", 30))); }

    public int halfZ() { return radius(); }

    public int halfX() { return Math.max(8, (int) Math.round(radius() * 0.75)); }

    public int pillarHeight() { return Math.max(8, Math.min(32, plugin.getConfig().getInt("stell.column-height", 20))); }

    /** Hauteur (depuis le sol du temple) du sommet du dôme. */
    public int domeTop() { return pillarHeight() + 3 + DRUM + domeRadius(); }

    /** Rayon du dôme central (et du hall intérieur). */
    public int domeRadius() { return Math.max(5, Math.min(halfX() - 7, halfZ() - 7)); }

    // ------------------------------------------------------------------ Chargement / structure

    public void load() {
        FileConfiguration c = plugin.getConfig();
        center = null;
        String w = c.getString("stell.world", "");
        if (w != null && !w.isBlank()) {
            World world = Bukkit.getWorld(w);
            if (world != null) {
                center = new Location(world, c.getInt("stell.x"), c.getInt("stell.y") + PODIUM, c.getInt("stell.z"));
            } else {
                plugin.getLogger().warning("Monde de la Demon Stell introuvable : " + w);
            }
        }
        computeRunes();
        startIdle();
        startAuto();
    }

    /** Définit la position du temple (pieds de l'administrateur) ; le sol du temple est surélevé sur un podium. */
    public void setCenter(Location l, boolean build) { setCenter(l, build, null); }

    public void setCenter(Location l, boolean build, Player requester) {
        if (session != null) dismissNow();
        center = new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + PODIUM, l.getBlockZ());
        FileConfiguration c = plugin.getConfig();
        c.set("stell.world", l.getWorld().getName());
        c.set("stell.x", l.getBlockX());
        c.set("stell.y", l.getBlockY());
        c.set("stell.z", l.getBlockZ());
        plugin.saveConfig();
        computeRunes();
        pendingTp = requester == null ? null : requester.getUniqueId();
        if (build) build();
        else if (requester != null) requester.teleport(center.clone().add(0.5, 0.1, 6.5));
    }

    private void computeRunes() {
        runes.clear();
        int hx = halfX();
        int hall = domeRadius();
        Set<Long> seen = new HashSet<>();
        List<int[]> list = new ArrayList<>();
        addRing(list, seen, 3.5);
        addRing(list, seen, hall * 0.5);
        addRing(list, seen, hall - 1.5);
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            for (double r = 1.5; r <= hall - 2.0; r += 1.0) {
                addRune(list, seen, (int) Math.round(Math.cos(a) * r), (int) Math.round(Math.sin(a) * r));
            }
        }
        list.sort((p, q) -> {
            int c = Double.compare(Math.atan2(p[1], p[0]), Math.atan2(q[1], q[0]));
            return c != 0 ? c : Double.compare(Math.hypot(p[0], p[1]), Math.hypot(q[0], q[1]));
        });
        runes.addAll(list);
        lit = 0;
        pillarOffsets.clear();
        pillarOffsets.addAll(columnPositions(hx, halfZ()));
    }

    private void addRing(List<int[]> list, Set<Long> seen, double r) {
        int n = (int) Math.ceil(2 * Math.PI * r * 1.3);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            addRune(list, seen, (int) Math.round(Math.cos(a) * r), (int) Math.round(Math.sin(a) * r));
        }
    }

    private void addRune(List<int[]> list, Set<Long> seen, int dx, int dz) {
        if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) return;
        long key = ((long) dx << 32) ^ (dz & 0xffffffffL);
        if (seen.add(key)) list.add(new int[]{dx, dz});
    }

    private Block runeBlock(int[] o) {
        return center.getWorld().getBlockAt(center.getBlockX() + o[0], center.getBlockY() - 1, center.getBlockZ() + o[1]);
    }

    /** Positions (dx, dz) des colonnes du péristyle, espacées d'environ 4 blocs. */
    private List<int[]> columnPositions(int hx, int hz) {
        Set<Long> seen = new HashSet<>();
        List<int[]> out = new ArrayList<>();
        int ax = hx - 2, az = hz - 2;
        int nz = Math.max(1, Math.round(2f * az / 4f));
        for (int i = 0; i <= nz; i++) {
            int z = -az + Math.round(2f * az * i / nz);
            addCol(out, seen, ax, z);
            addCol(out, seen, -ax, z);
        }
        int nx = Math.max(1, Math.round(2f * ax / 4f));
        for (int i = 0; i <= nx; i++) {
            int x = -ax + Math.round(2f * ax * i / nx);
            addCol(out, seen, x, az);
            addCol(out, seen, x, -az);
        }
        return out;
    }

    private void addCol(List<int[]> out, Set<Long> seen, int x, int z) {
        if (seen.add(((long) x << 32) ^ (z & 0xffffffffL))) out.add(new int[]{x, z});
    }

    private Material checker(int dx, int dz) {
        int h = Math.floorMod(dx * 73856093 ^ dz * 19349663, 100);
        if (h >= 98) return Material.GILDED_BLACKSTONE;
        if (h >= 94) return Material.CRACKED_DEEPSLATE_TILES;
        int mx = Math.floorMod(dx, 8), mz = Math.floorMod(dz, 8);
        if (Math.abs(mx - 4) + Math.abs(mz - 4) <= 1) return Material.RED_NETHER_BRICKS;
        return (((dx >> 1) + (dz >> 1)) & 1) == 0 ? Material.POLISHED_BLACKSTONE : Material.DEEPSLATE_TILES;
    }

    private Material floorMaterial(int dx, int dz, int hx, int hz) {
        int ax = Math.abs(dx), az = Math.abs(dz);
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d <= 1.5) return (dx == 0 && dz == 0) ? Material.GILDED_BLACKSTONE : Material.CHISELED_POLISHED_BLACKSTONE;
        if (d <= 2.5) return Material.POLISHED_BLACKSTONE_BRICKS;
        if (ax == hx || az == hz) return (ax + az) % 2 == 0 ? Material.GILDED_BLACKSTONE : Material.CHISELED_POLISHED_BLACKSTONE;
        if (ax == hx - 1 || az == hz - 1) return Material.RED_NETHER_BRICKS;
        if (ax <= 1) return (az % 4 == 1) ? Material.GILDED_BLACKSTONE : Material.POLISHED_BLACKSTONE_BRICKS;
        return checker(dx, dz);
    }

    private void put(World w, int x, int y, int z, Material m) { w.getBlockAt(x, y, z).setType(m, false); }

    /** Lance la construction du colossal temple (par tranches, sans figer le serveur). */
    public void build() {
        if (center == null || center.getWorld() == null || building) return;
        building = true;
        final World w = center.getWorld();
        final int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        final int hx = halfX(), hz = halfZ(), H = pillarHeight();
        final int clearH = Math.max(domeTop() + 8, plugin.getConfig().getInt("stell.clear-height", 8));
        final int reach = hx + PODIUM + PLAZA + 2;
        final int[] cursor = {-reach};
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 2 && cursor[0] <= reach; i++, cursor[0]++) {
                        clearAndFloor(w, cx, cy, cz, cursor[0], hx, hz, clearH);
                    }
                    if (cursor[0] > reach) {
                        cancel();
                        buildStructure(w, cx, cy, cz, hx, hz, H);
                        building = false;
                        if (pendingTp != null) {
                            Player pl = Bukkit.getPlayer(pendingTp);
                            pendingTp = null;
                            if (pl != null) pl.teleport(center.clone().add(0.5, 0.1, 6.5));
                        }
                    }
                } catch (RuntimeException ex) {
                    cancel();
                    building = false;
                    plugin.getLogger().severe("Construction du temple interrompue : " + ex);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Une tranche (dx) : dégagement, podium plein, gradins, sol du temple et grand parvis dallé en marqueterie. */
    private void clearAndFloor(World w, int cx, int cy, int cz, int dx, int hx, int hz, int clearH) {
        final int gy = cy - 1 - PODIUM;
        final int reachZ = hz + PODIUM + PLAZA + 2;
        for (int dz = -reachZ; dz <= reachZ; dz++) {
            int ax = Math.abs(dx), az = Math.abs(dz);
            int k = Math.max(ax - hx, az - hz);
            if (k > PODIUM + PLAZA) continue;
            int top = k <= 12 ? clearH : 10;
            for (int y = gy + 1; y < cy + top; y++) {
                Block b = w.getBlockAt(cx + dx, y, cz + dz);
                if (!b.getType().isAir()) b.setType(Material.AIR, false);
            }
            if (k <= PODIUM - 1) {
                int topY = k <= 0 ? cy - 1 : cy - 1 - k;
                for (int y = topY - 1; y >= gy + 1; y--) put(w, cx + dx, y, cz + dz, Material.BLACKSTONE);
                for (int y = gy; y >= gy - 6; y--) {
                    Block b = w.getBlockAt(cx + dx, y, cz + dz);
                    if (b.getType().isAir() || b.isLiquid() || b.isPassable()) b.setType(Material.BLACKSTONE, false);
                    else break;
                }
                put(w, cx + dx, topY, cz + dz, k <= 0 ? floorMaterial(dx, dz, hx, hz) : Material.POLISHED_BLACKSTONE_BRICKS);
            } else {
                put(w, cx + dx, gy, cz + dz, checker(dx, dz));
            }
        }
    }

    /**
     * Colossal temple démoniaque d'inspiration antique : podium à gradins et grand escalier flanqué de braseros, péristyle de
     * colonnes à chapiteaux évasés, cella à porte en arc, entablement à frise rouge, toit plat à corniches et parapet crénelé,
     * pinacles étagés, tambour à fenêtres surmonté d'un grand dôme à nervures et lanterneau, deux tours à cadrans, parvis
     * dallé en marqueterie et lampadaires d'âmes.
     */
    private void buildStructure(World w, int cx, int cy, int cz, int hx, int hz, int H) {
        final int Rd = domeRadius();
        final double oculus = Rd - 1.5;
        final int gy = cy - 1 - PODIUM;
        for (int[] o : runes) runeBlock(o).setType(RUNE_DIM, false);
        for (int[] o : pillarOffsets) buildColumn(w, cx + o[0], cy, cz + o[1], H);

        // cella : murs intérieurs, porte monumentale en arc côté +z, fenêtres lumineuses
        final int wx = hx - 6, wz = hz - 6, gate = 5, springY = H - 9;
        for (int dx = -wx; dx <= wx; dx++) {
            for (int dz = -wz; dz <= wz; dz++) {
                if (Math.abs(dx) != wx && Math.abs(dz) != wz) continue;
                for (int y = 0; y < H; y++) {
                    if (dz == wz && Math.abs(dx) <= gate && (y < springY || Math.hypot(dx, y - springY) <= gate)) continue;
                    Material m;
                    boolean pilaster = ((Math.abs(dx) + Math.abs(dz)) % 4) == 0;
                    boolean window = y >= H / 2 - 1 && y <= H / 2 + 2 && ((Math.abs(dx) + Math.abs(dz)) % 6) == 3
                            && !(dz == wz && Math.abs(dx) <= gate + 2);
                    if (window) m = Material.CRYING_OBSIDIAN;
                    else if (y == 0 || y == H - 1) m = Material.POLISHED_BLACKSTONE_BRICKS;
                    else if (pilaster) m = Material.CHISELED_POLISHED_BLACKSTONE;
                    else m = ((dx + dz + y) & 3) == 0 ? Material.CRACKED_DEEPSLATE_BRICKS : Material.DEEPSLATE_BRICKS;
                    put(w, cx + dx, cy + y, cz + dz, m);
                }
            }
        }
        for (int dx = -gate - 1; dx <= gate + 1; dx++) {
            for (int y = 0; y < H; y++) {
                double dist = Math.hypot(dx, y - springY);
                if (Math.abs(dx) <= gate + 1 && ((y < springY && Math.abs(dx) == gate + 1) || (y >= springY && dist > gate && dist <= gate + 1.2))) {
                    put(w, cx + dx, cy + y, cz + wz, Material.GILDED_BLACKSTONE);
                }
            }
        }

        // entablement (architrave + frise rouge), plafond à caissons, grand oculus sous le dôme
        Lantern hanging = (Lantern) Material.SOUL_LANTERN.createBlockData();
        hanging.setHanging(true);
        for (int dx = -(hx - 1); dx <= hx - 1; dx++) {
            for (int dz = -(hz - 1); dz <= hz - 1; dz++) {
                int ax = Math.abs(dx), az = Math.abs(dz);
                if (ax >= hx - 3 || az >= hz - 3) {
                    put(w, cx + dx, cy + H, cz + dz, Material.POLISHED_BLACKSTONE_BRICKS);
                    Material fr = ((dx + dz) & 3) == 0 ? Material.GILDED_BLACKSTONE
                            : (((dx + dz) & 1) == 0 ? Material.RED_NETHER_BRICKS : Material.POLISHED_BLACKSTONE_BRICKS);
                    put(w, cx + dx, cy + H + 1, cz + dz, fr);
                } else {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d <= oculus) continue;
                    put(w, cx + dx, cy + H, cz + dz,
                            (((dx >> 1) + (dz >> 1)) & 1) == 0 ? Material.POLISHED_DEEPSLATE : Material.DEEPSLATE_TILES);
                    if (d <= oculus + 1.2) put(w, cx + dx, cy + H, cz + dz, Material.CRYING_OBSIDIAN);
                    else if (dx % 4 == 0 && dz % 4 == 0 && ax < wx && az < wz) {
                        w.getBlockAt(cx + dx, cy + H - 1, cz + dz).setBlockData(hanging, false);
                    }
                }
            }
        }

        // corniches étagées, toit plat, parapet crénelé hérissé de pointes
        for (int dx = -(hx + 1); dx <= hx + 1; dx++) {
            for (int dz = -(hz + 1); dz <= hz + 1; dz++) {
                int ax = Math.abs(dx), az = Math.abs(dz);
                double d = Math.sqrt(dx * dx + dz * dz);
                if (ax == hx + 1 || az == hz + 1) {
                    put(w, cx + dx, cy + H + 2, cz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
                    continue;
                }
                if (ax == hx || az == hz) put(w, cx + dx, cy + H + 1, cz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
                if (d <= oculus) continue;
                boolean border = ax == hx || az == hz;
                put(w, cx + dx, cy + H + 2, cz + dz, border ? Material.POLISHED_BLACKSTONE_BRICKS : Material.DEEPSLATE_TILES);
                if (border && (ax + az) % 2 == 0) {
                    put(w, cx + dx, cy + H + 3, cz + dz, Material.POLISHED_BLACKSTONE_BRICKS);
                    if ((ax + az) % 6 == 0) put(w, cx + dx, cy + H + 4, cz + dz, Material.POINTED_DRIPSTONE);
                }
            }
        }
        // pinacles étagés aux angles et au milieu des grands côtés
        int[][] pin = {{hx - 3, hz - 3}, {-(hx - 3), hz - 3}, {hx - 3, -(hz - 3)}, {-(hx - 3), -(hz - 3)}, {hx - 3, 0}, {-(hx - 3), 0}};
        for (int[] pc : pin) buildPinnacle(w, cx + pc[0], cy + H + 3, cz + pc[1]);

        // tambour à fenêtres et pilastres
        final int yDrum = cy + H + 3;
        for (int dx = -Rd - 1; dx <= Rd + 1; dx++) {
            for (int dz = -Rd - 1; dz <= Rd + 1; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > Rd || r <= Rd - 1.6) continue;
                double fa = ((Math.atan2(dz, dx) + Math.PI) / (Math.PI / 8)) % 1.0;
                for (int dy = 0; dy < DRUM; dy++) {
                    Material m;
                    if (fa < 0.12 || fa > 0.88) m = Material.CHISELED_POLISHED_BLACKSTONE;
                    else if (dy >= 2 && dy <= 4 && fa > 0.3 && fa < 0.7) m = Material.CRYING_OBSIDIAN;
                    else m = (dy % 2 == 0) ? Material.DEEPSLATE_BRICKS : Material.POLISHED_BLACKSTONE_BRICKS;
                    put(w, cx + dx, yDrum + dy, cz + dz, m);
                }
            }
        }
        // grand dôme à nervures d'obsidienne pleureuse, ouvert au sommet
        final int y0 = yDrum + DRUM;
        for (int dx = -Rd - 2; dx <= Rd + 2; dx++) {
            for (int dz = -Rd - 2; dz <= Rd + 2; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > Rd && r <= Rd + 1.3) put(w, cx + dx, y0, cz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
                for (int dy = 0; dy <= Rd; dy++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > Rd || dist <= Rd - 1.6) continue;
                    if (dy >= Rd - 2 && r <= 1.6) continue;
                    double fa = ((Math.atan2(dz, dx) + Math.PI) / (Math.PI / 8)) % 1.0;
                    boolean rib = fa < 0.14 || fa > 0.86;
                    Material m = rib ? Material.CRYING_OBSIDIAN
                            : ((dy % 3 == 0) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.DEEPSLATE_TILES);
                    if (!rib && dy % 6 == 5) m = Material.RED_NETHER_BRICKS;
                    put(w, cx + dx, y0 + dy, cz + dz, m);
                }
            }
        }
        // lanterneau : quatre colonnettes, couronne et pointes
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                for (int dy = Rd - 2; dy <= Rd + 3; dy++) put(w, cx + sx * 2, y0 + dy, cz + sz * 2, Material.CHISELED_POLISHED_BLACKSTONE);
                put(w, cx + sx * 2, y0 + Rd + 4, cz + sz * 2, Material.POINTED_DRIPSTONE);
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 2) put(w, cx + dx, y0 + Rd + 3, cz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
            }
        }

        // grand escalier de façade (côté +z) flanqué de joues et de braseros
        Stairs steps = (Stairs) Bukkit.createBlockData(Material.POLISHED_BLACKSTONE_BRICK_STAIRS);
        steps.setFacing(BlockFace.NORTH);
        for (int k = 1; k <= PODIUM - 1; k++) {
            for (int dx = -10; dx <= 10; dx++) w.getBlockAt(cx + dx, cy - 1 - k, cz + hz + k).setBlockData(steps, false);
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int e = 11; e <= 12; e++) {
                    for (int dy = 1; dy <= 2; dy++) put(w, cx + sx * e, cy - 1 - k + dy, cz + hz + k, Material.POLISHED_BLACKSTONE_BRICKS);
                }
            }
        }
        for (int sx = -1; sx <= 1; sx += 2) {
            int bx = cx + sx * 11;
            for (int dy = 0; dy < 4; dy++) put(w, bx, cy + dy, cz + hz + 1, dy == 0 ? Material.CHISELED_POLISHED_BLACKSTONE : Material.POLISHED_BLACKSTONE_BRICKS);
            put(w, bx, cy + 4, cz + hz + 1, Material.SOUL_CAMPFIRE);
        }

        // deux tours à cadrans à l'arrière du parvis
        buildTower(w, cx + hx + 8, gy, cz - (hz - 8), cy + H + 8);
        buildTower(w, cx - (hx + 8), gy, cz - (hz - 8), cy + H + 8);

        // lampadaires d'âmes sur le parvis
        int[] lampX = {-(hx - 6), hx - 6, -(hx + 4), hx + 4};
        for (int lx : lampX) {
            for (int lz = hz + PODIUM + 3; lz <= hz + PODIUM + PLAZA - 1; lz += 5) {
                for (int dy = 1; dy <= 3; dy++) put(w, cx + lx, gy + dy, cz + lz, Material.POLISHED_BLACKSTONE_BRICKS);
                put(w, cx + lx, gy + 4, cz + lz, Material.SOUL_LANTERN);
            }
        }

        // braseros d'âmes autour de l'autel
        int br = Math.max(3, Rd - 3);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                int px = cx + sx * br, pz = cz + sz * br;
                put(w, px, cy, pz, Material.CHISELED_POLISHED_BLACKSTONE);
                put(w, px, cy + 1, pz, Material.POLISHED_BLACKSTONE_BRICKS);
                put(w, px, cy + 2, pz, Material.SOUL_CAMPFIRE);
            }
        }
        lit = 0;
    }

    /** Pinacle étagé : 7x7, 7x7, 5x5, 5x5, 3x3, 3x3, 1x1, 1x1 puis pointe. */
    private void buildPinnacle(World w, int px, int y0, int pz) {
        int[] half = {3, 3, 2, 2, 1, 1, 0, 0};
        for (int i = 0; i < half.length; i++) {
            for (int dx = -half[i]; dx <= half[i]; dx++) {
                for (int dz = -half[i]; dz <= half[i]; dz++) {
                    boolean corner = Math.abs(dx) == half[i] && Math.abs(dz) == half[i] && half[i] > 0;
                    Material m = (i % 2 == 0) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.CHISELED_POLISHED_BLACKSTONE;
                    if (i == 1 && corner) m = Material.CRYING_OBSIDIAN;
                    if (i == 1 && !corner) m = Material.RED_NETHER_BRICKS;
                    put(w, px + dx, y0 + i, pz + dz, m);
                }
            }
        }
        put(w, px, y0 + half.length, pz, Material.POINTED_DRIPSTONE);
    }

    /** Tour à cadrans : socle, fût à bandes, cadrans sur les quatre faces, beffroi, toit en pyramide et flèche. */
    private void buildTower(World w, int tx, int gy, int tz, int topY) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean edge = Math.max(Math.abs(dx), Math.abs(dz)) == 4;
                if (edge) {
                    boolean corner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                    put(w, tx + dx, gy + 1, tz + dz, corner ? Material.CHISELED_POLISHED_BLACKSTONE : Material.POLISHED_BLACKSTONE_BRICKS);
                    put(w, tx + dx, gy + 2, tz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
                }
            }
        }
        for (int y = gy + 1; y <= topY; y++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Material m = ((y - gy) % 6 == 0) ? Material.RED_NETHER_BRICKS
                            : (((y - gy) % 3 == 0) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.DEEPSLATE_BRICKS);
                    if (Math.abs(dx) == 3 && Math.abs(dz) == 3) m = Material.CHISELED_POLISHED_BLACKSTONE;
                    put(w, tx + dx, y, tz + dz, m);
                }
            }
        }
        // cadrans sur les quatre faces
        int yc = topY - 5;
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                double r = Math.hypot(a, b);
                if (r > 2.3) continue;
                Material m = r <= 0.6 ? Material.CRYING_OBSIDIAN : (r <= 1.5 ? Material.CHISELED_POLISHED_BLACKSTONE : Material.GILDED_BLACKSTONE);
                put(w, tx + a, yc + b, tz + 3, m);
                put(w, tx + a, yc + b, tz - 3, m);
                put(w, tx + 3, yc + b, tz + a, m);
                put(w, tx - 3, yc + b, tz + a, m);
            }
        }
        // corniche, beffroi, toit
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 4) put(w, tx + dx, topY + 1, tz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
            }
        }
        int[] half = {3, 2, 1, 0};
        for (int i = 0; i < half.length; i++) {
            for (int dx = -half[i]; dx <= half[i]; dx++) {
                for (int dz = -half[i]; dz <= half[i]; dz++) {
                    put(w, tx + dx, topY + 1 + i, tz + dz, i % 2 == 0 ? Material.DEEPSLATE_TILES : Material.CRYING_OBSIDIAN);
                }
            }
        }
        put(w, tx, topY + 1 + half.length, tz, Material.POINTED_DRIPSTONE);
        put(w, tx, topY + 2 + half.length, tz, Material.SOUL_LANTERN);
    }

    /** Colonne à chapiteau évasé : plinthe à dalles et lanternes, fût en croix à veines lumineuses, abaque et lanternes suspendues. */
    private void buildColumn(World w, int px, int cy, int pz, int H) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 2) put(w, px + dx, cy, pz + dz, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
            }
        }
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) put(w, px + sx * 2, cy + 1, pz + sz * 2, Material.SOUL_LANTERN);
        }
        for (int y = 0; y <= H - 5; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                    boolean middle = dx == 0 && dz == 0;
                    if (y == 0) {
                        put(w, px + dx, cy + y, pz + dz, corner ? Material.CHISELED_POLISHED_BLACKSTONE : Material.POLISHED_BLACKSTONE_BRICKS);
                        continue;
                    }
                    if (corner) continue;
                    Material m;
                    if (middle) m = Material.POLISHED_DEEPSLATE;
                    else if (y % 5 == 0) m = Material.CRYING_OBSIDIAN;
                    else m = (y % 2 == 0) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.DEEPSLATE_BRICKS;
                    put(w, px + dx, cy + y, pz + dz, m);
                }
            }
        }
        // chapiteau évasé : 3x3 gravé, croix élargie, abaque 5x5
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) put(w, px + dx, cy + H - 4, pz + dz, Material.CHISELED_POLISHED_BLACKSTONE);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                if (!corner) put(w, px + dx, cy + H - 3, pz + dz, Material.POLISHED_BLACKSTONE_BRICKS);
                put(w, px + dx, cy + H - 2, pz + dz, corner ? Material.GILDED_BLACKSTONE : Material.CHISELED_POLISHED_BLACKSTONE);
                put(w, px + dx, cy + H - 1, pz + dz, Material.POLISHED_BLACKSTONE_BRICKS);
            }
        }
        Lantern hanging = (Lantern) Material.SOUL_LANTERN.createBlockData();
        hanging.setHanging(true);
        int[][] nubs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        for (int[] n : nubs) w.getBlockAt(px + n[0], cy + H - 4, pz + n[1]).setBlockData(hanging, false);
    }

    /** Vrai si la position se trouve sur le temple (protection contre la destruction). */
    public boolean isInStell(Location l) {
        if (center == null || l.getWorld() == null || !l.getWorld().equals(center.getWorld())) return false;
        int dx = Math.abs(l.getBlockX() - center.getBlockX()), dz = Math.abs(l.getBlockZ() - center.getBlockZ());
        int dy = l.getBlockY() - center.getBlockY();
        return dx <= halfX() + PODIUM + PLAZA && dz <= halfZ() + PODIUM + PLAZA && dy >= -(PODIUM + 8) && dy <= domeTop() + 12;
    }

    // ------------------------------------------------------------------ Invocation

    public boolean isDemonEntity(Entity e) {
        return e != null && e.getPersistentDataContainer().has(demonKey, PersistentDataType.STRING);
    }

    /** Joueur éligible au hasard (permission, capacité de pacte, proximité de l'autel). */
    public Player randomCandidate(Set<UUID> exclude) {
        if (center == null || center.getWorld() == null) return null;
        double radius = plugin.getConfig().getDouble("stell.target-radius", 40);
        List<Player> list = new ArrayList<>();
        for (Player p : center.getWorld().getPlayers()) {
            if (exclude != null && exclude.contains(p.getUniqueId())) continue;
            if (!p.hasPermission("demon.use") || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (!plugin.pacts().hasCapacity(p.getUniqueId())) continue;
            if (radius > 0 && p.getLocation().distanceSquared(center) > radius * radius) continue;
            list.add(p);
        }
        return list.isEmpty() ? null : list.get(rnd.nextInt(list.size()));
    }

    /** Lance l'invocation. Retourne un message d'erreur, ou null en cas de succès. */
    public String summon(Demon d, Player explicitTarget) {
        if (center == null || center.getWorld() == null) return "La Demon Stell n'est pas définie. Utilisez /demon setstell.";
        if (building) return "La Demon Stell est en cours de construction.";
        if (session != null) return "Un démon est déjà présent sur la Demon Stell.";
        Player target = explicitTarget;
        if (target != null) {
            if (!plugin.pacts().hasCapacity(target.getUniqueId())) return target.getName() + " possède déjà le maximum de pactes.";
        } else {
            target = randomCandidate(null);
            if (target == null) return "Aucun joueur éligible près de la Demon Stell.";
        }
        Session s = new Session(d);
        s.target = target.getUniqueId();
        session = s;
        startAnimation(s);
        return null;
    }

    private List<Player> playersNear(Location l, double radius) {
        List<Player> out = new ArrayList<>();
        if (l.getWorld() == null) return out;
        for (Player p : l.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(l) <= radius * radius) out.add(p);
        }
        return out;
    }

    private void startAnimation(Session s) {
        s.animating = true;
        final int dur = Math.max(40, plugin.getConfig().getInt("stell.animation-ticks", 100));
        final Element el = s.demon.element();
        final World w = center.getWorld();
        final Location base = center.clone().add(0.5, 0, 0.5);
        final int radius = radius();
        final boolean thunder = plugin.getConfig().getBoolean("stell.lightning-during-summon", true);
        double chat = plugin.getConfig().getDouble("negotiation.chat-radius", 60);
        for (Player p : playersNear(base, chat)) {
            p.sendMessage(Txt.c("&4&l⚠ &cLes runes de la Demon Stell s'embrasent... &7Quelque chose approche."));
        }
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (session != s || s.ending) { cancel(); return; }
                double prog = (double) t / dur;
                if (t == 0) {
                    Fx.sound(base, "block.beacon.activate", 2f, 0.5f);
                    Fx.sound(base, "entity.elder_guardian.curse", 1.5f, 0.6f);
                }
                if (t % 20 == 10) Fx.sound(base, "block.respawn_anchor.charge", 1.5f, 0.5f + (float) prog);
                if (t % 25 == 0) Fx.sound(base, el.sound, 1.5f, 0.6f);
                int target = (int) (runes.size() * Math.min(1.0, prog / 0.75));
                while (lit < target) lightRune(lit++, base, el);
                if (t % 3 == 0) floorWave(base, prog * radius, el);
                swirl(base, t, dur, radius);
                elementPattern(el, base, t, dur, radius, prog);
                templeSummonFx(base, t, prog, el.color);
                if (thunder && (t == (int) (dur * 0.55) || t == (int) (dur * 0.75) || t == (int) (dur * 0.9))) {
                    w.strikeLightningEffect(base);
                    if (!pillarOffsets.isEmpty()) {
                        int[] o = pillarOffsets.get(rnd.nextInt(pillarOffsets.size()));
                        Location top = base.clone().add(o[0], pillarHeight() + 2, o[1]);
                        w.strikeLightningEffect(top);
                        Fx.spawn(top, "electric_spark", 30, 0.5, 0.5, 0.5, 0.3);
                        Fx.spawn(top, "soul_fire_flame", 20, 0.3, 0.5, 0.3, 0.1);
                    }
                    w.strikeLightningEffect(base.clone().add(0, domeTop() + 4, 0));
                }
                t++;
                if (t >= dur) {
                    cancel();
                    finishSummon(s);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void lightRune(int i, Location base, Element el) {
        if (i < 0 || i >= runes.size()) return;
        int[] o = runes.get(i);
        runeBlock(o).setType(RUNE_LIT, false);
        Location l = base.clone().add(o[0], 0.25, o[1]);
        Fx.dust(l, el.color, 1.5f, 6, 0.2);
        Fx.spawn(l, "soul_fire_flame", 2, 0.1, 0.2, 0.1, 0.01);
        if (i % 6 == 0) Fx.sound(l, "block.amethyst_block.chime", 0.7f, 0.5f);
    }

    /** Anneau de blocs d'aspect différent qui s'étend depuis le centre (effet purement visuel côté client). */
    private void floorWave(Location base, double radius, Element el) {
        if (radius < 1) return;
        List<Player> near = playersNear(base, 48);
        if (near.isEmpty()) return;
        World w = base.getWorld();
        Set<Block> blocks = new LinkedHashSet<>();
        int n = (int) Math.max(8, radius * 7);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            Block b = w.getBlockAt((int) Math.floor(base.getX() + Math.cos(a) * radius),
                    center.getBlockY() - 1, (int) Math.floor(base.getZ() + Math.sin(a) * radius));
            if (!b.getType().isAir() && b.getType() != RUNE_LIT) blocks.add(b);
        }
        BlockData fx = el.floorFx.createBlockData();
        for (Block b : blocks) for (Player p : near) p.sendBlockChange(b.getLocation(), fx);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Block b : blocks) {
                BlockData real = b.getBlockData();
                for (Player p : near) if (p.isOnline()) p.sendBlockChange(b.getLocation(), real);
            }
        }, 6L);
    }

    /** Spirale rouge / violette / noire commune à toutes les invocations. */
    private void swirl(Location base, int t, int dur, int radius) {
        double prog = (double) t / dur;
        double r = Math.max(0.5, (radius * 0.4) * (1 - prog));
        Color[] cols = {Color.fromRGB(190, 10, 20), Color.fromRGB(130, 30, 200), Color.fromRGB(12, 0, 16)};
        for (int arm = 0; arm < 3; arm++) {
            double a = t * 0.35 + arm * 2 * Math.PI / 3;
            double h = 0.2 + ((t * 0.12) + arm) % 3.0;
            Location l = base.clone().add(Math.cos(a) * r, h, Math.sin(a) * r);
            Fx.dust(l, cols[arm], 1.6f, 2, 0.05);
        }
        Fx.spawn(base.clone().add(0, prog * 3, 0), "portal", 6, 0.3, 0.5, 0.3, 0.4);
    }

    private double rr(double a, double b) { return a + rnd.nextDouble() * (b - a); }

    /** Animation d'invocation propre à l'élément du démon. */
    private void elementPattern(Element el, Location base, int t, int dur, int radius, double prog) {
        World w = base.getWorld();
        switch (el) {
            case WIND -> {
                for (int i = 0; i < 4; i++) {
                    double a = t * 0.6 + i * Math.PI / 2;
                    double h = (t * 0.08 + i * 0.7) % 3.5;
                    double r = 0.6 + h * 0.7;
                    Fx.spawn(base.clone().add(Math.cos(a) * r, h, Math.sin(a) * r), "cloud", 1, 0.02, 0.02, 0.02, 0.01);
                }
                if (t % 10 == 0) Fx.ring(base.clone().add(0, 0.3, 0), 1 + prog * (radius - 2), 28, "poof");
            }
            case FIRE -> {
                Fx.spawn(base.clone().add(0, 0.2, 0), "flame", 10, 0.4, 0.1, 0.4, 0.12);
                Fx.spawn(base.clone().add(0, prog * 3, 0), "lava", 2, 0.3, 0.3, 0.3, 0);
                if (t % 4 == 0) Fx.ring(base.clone().add(0, 0.2, 0), 1 + prog * (radius - 2), 30, "flame");
            }
            case LIGHTNING -> {
                for (int i = 0; i < 3; i++) {
                    double a = rr(0, 2 * Math.PI), r = rr(0, radius * 0.8);
                    Fx.spawn(base.clone().add(Math.cos(a) * r, 0.2, Math.sin(a) * r), "electric_spark", 6, 0.1, 1.5, 0.1, 0.3);
                }
                if (t % 14 == 7 && !runes.isEmpty()) {
                    int[] o = runes.get(rnd.nextInt(runes.size()));
                    w.strikeLightningEffect(base.clone().add(o[0], 0, o[1]));
                }
            }
            case TIME -> {
                double a1 = -t * 0.16, a2 = -t * 0.02;
                for (double r = 0.5; r < radius - 2; r += 0.6) {
                    Fx.spawn(base.clone().add(Math.cos(a1) * r, 0.15, Math.sin(a1) * r), "end_rod", 1, 0, 0, 0, 0);
                    if (r < (radius - 2) * 0.65) {
                        Fx.spawn(base.clone().add(Math.cos(a2) * r, 0.15, Math.sin(a2) * r), "end_rod", 1, 0, 0, 0, 0);
                    }
                }
                if (t % 5 == 0) Fx.ring(base.clone().add(0, 0.1, 0), radius - 2.5, 12, "enchant");
            }
            case SHADOW -> {
                double r = radius * (1 - prog) + 0.6;
                for (int i = 0; i < 10; i++) {
                    double a = rr(0, 2 * Math.PI);
                    Location l = base.clone().add(Math.cos(a) * r, rr(0.1, 2), Math.sin(a) * r);
                    Fx.spawn(l, "squid_ink", 1, 0, 0, 0, 0);
                    Fx.spawn(l, "smoke", 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
            case BLOOD -> {
                for (int i = 0; i < 4; i++) {
                    double a = rr(0, 2 * Math.PI), r = rr(0.5, radius - 2);
                    Location l = base.clone().add(Math.cos(a) * r, rr(0.1, 2.5), Math.sin(a) * r);
                    Fx.dust(l, Color.fromRGB(170, 0, 20), 1.5f, 2, 0.1);
                    Fx.spawn(l, "crimson_spore", 2, 0.1, 0.1, 0.1, 0);
                }
                if (t % 8 == 0) Fx.spawn(base.clone().add(0, 1, 0), "damage_indicator", 6, 0.4, 0.5, 0.4, 0.05);
            }
            case EARTH -> {
                if (t % 4 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double a = rr(0, 2 * Math.PI), r = rr(1, radius - 2);
                        Fx.block(base.clone().add(Math.cos(a) * r, 0.2, Math.sin(a) * r), Material.STONE, 14, 0.3, 0.4, 0.3);
                    }
                    Fx.sound(base, "block.stone.break", 1f, 0.5f);
                }
                Fx.spawn(base.clone().add(0, 0.2, 0), "poof", 3, 0.6, 0.1, 0.6, 0.02);
            }
            case WATER -> {
                for (int i = 0; i < 5; i++) {
                    double a = t * 0.45 + i * 2 * Math.PI / 5;
                    double r = 1.0 + ((t + i * 9) % 40) * 0.07;
                    double h = ((t + i * 7) % 30) * 0.1;
                    Location l = base.clone().add(Math.cos(a) * r, h, Math.sin(a) * r);
                    Fx.spawn(l, "splash", 2, 0.05, 0.05, 0.05, 0.02);
                    Fx.spawn(l, "bubble_pop", 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
            case DEATH -> {
                if (lit > 0) {
                    for (int i = 0; i < 3; i++) {
                        int[] o = runes.get(rnd.nextInt(lit));
                        Fx.spawn(base.clone().add(o[0], 0.3, o[1]), "soul", 1, 0.05, 0.5, 0.05, 0.03);
                    }
                }
                Fx.spawn(base.clone().add(0, 1, 0), "sculk_soul", 2, 0.3, 0.8, 0.3, 0.02);
            }
            case ILLUSION -> {
                for (int i = 0; i < 3; i++) {
                    double a = rr(0, 2 * Math.PI), r = rr(0.5, radius - 2);
                    Fx.spawn(base.clone().add(Math.cos(a) * r, rr(0.2, 2.5), Math.sin(a) * r), "witch", 3, 0.2, 0.2, 0.2, 0);
                }
                if (t % 6 == 0) Fx.ring(base.clone().add(rr(-1, 1), rr(0.2, 2), rr(-1, 1)), rr(1, radius - 2), 20, "end_rod");
            }
            case GRAVITY -> {
                for (int i = 0; i < 8; i++) {
                    double a = rr(0, 2 * Math.PI), r = rr(0, radius - 2);
                    double y = 6 * (1 - prog) * rnd.nextDouble() + 0.2;
                    Fx.spawn(base.clone().add(Math.cos(a) * r, y, Math.sin(a) * r), "reverse_portal", 1, 0, -0.2, 0, 0.05);
                }
                if (t % 6 == 0) Fx.ringDust(base.clone().add(0, 0.3, 0), (radius - 2) * (1 - prog) + 0.5, 24, Element.GRAVITY.color, 1.5f);
            }
            case VOID -> {
                double r = 3 * (1 - prog) + 0.4;
                for (int i = 0; i < 6; i++) {
                    double a = t * 0.5 + i * Math.PI / 3;
                    Location l = base.clone().add(Math.cos(a) * r, 0.3 + (t % 20) * 0.1, Math.sin(a) * r);
                    Fx.spawn(l, "portal", 3, 0.05, 0.05, 0.05, 0.2);
                    Fx.dust(l, Color.fromRGB(20, 0, 30), 1.6f, 1, 0.05);
                }
                if (t == dur - 2) Fx.spawn(base.clone().add(0, 1, 0), "sonic_boom", 1, 0, 0, 0, 0);
            }
        }
    }

    private void finishSummon(Session s) {
        if (session != s || s.ending) return;
        s.animating = false;
        Demon d = s.demon;
        Location spawn = center.clone().add(0.5, d.hover() ? 1.0 : 0.0, 0.5);
        LivingEntity ent = spawnEntity(d, spawn);
        if (ent == null) {
            plugin.getLogger().severe("Impossible de faire apparaître le démon " + d.id());
            dismiss(s, "error");
            return;
        }
        s.entity = ent;
        s.active = true;
        Location eye = spawn.clone().add(0, 1, 0);
        Fx.spawn(eye, "explosion_emitter", 1, 0, 0, 0, 0);
        Fx.spawn(eye, d.element().particle, 80, 1, 1, 1, 0.3);
        Fx.sound(spawn, "entity.wither.spawn", 2f, 0.6f);
        Fx.sound(spawn, "entity.ender_dragon.growl", 1.5f, 0.8f);
        Location oc = center.clone().add(0.5, pillarHeight(), 0.5);
        Fx.spawn(oc, "end_rod", 120, 0.3, 4, 0.3, 0.25);
        Fx.spawn(oc, d.element().particle, 80, 1.5, 1, 1.5, 0.2);
        for (int i = 1; i <= 3; i++) Fx.ring(center.clone().add(0.5, i * 1.2, 0.5), 2 + i, 30, d.element().particle);
        Player target = s.target == null ? null : Bukkit.getPlayer(s.target);
        DemonManager dm = plugin.demons();
        boolean global = plugin.getConfig().getBoolean("negotiation.announce-global", true) || s.waitingFirst;
        if (global) {
            Bukkit.broadcast(Txt.c("&4&l⚠ " + d.tag() + " &7(" + d.color() + d.title() + "&7) &cest apparu sur la Demon Stell !"));
        }
        dm.say(d, dm.format(d.line("appear"), target, null, d), target, global);
        if (s.waitingFirst) {
            Bukkit.broadcast(Txt.c("&6▸ &7Le premier qui lui parle (&fclic droit sur le démon&7) recevra une proposition de pacte."));
            long wait = Math.max(1, plugin.getConfig().getLong("auto-spawn.wait-minutes", 10)) * 1200L;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (session != s || s.ending || !s.waitingFirst) return;
                dm.say(d, dm.format(d.line("timeout"), null, null, d), null, true);
                dismiss(s, "nobody");
            }, wait);
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (session != s || s.ending) return;
            Player tp = s.target == null ? null : Bukkit.getPlayer(s.target);
            if (tp != null) plugin.contracts().startOffer(s, tp);
            else dismiss(s, "absent");
        }, 70L);
    }

    private LivingEntity spawnEntity(Demon d, Location loc) {
        EntityType type = null;
        try {
            type = Registry.ENTITY_TYPE.get(NamespacedKey.minecraft(d.entity().toLowerCase()));
        } catch (Exception ignored) {
            // type invalide : repli sur zombie
        }
        if (type == null || !type.isAlive() || type == EntityType.PLAYER
                || type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
            type = EntityType.ZOMBIE;
        }
        Entity e = loc.getWorld().spawnEntity(loc, type);
        if (!(e instanceof LivingEntity le)) {
            e.remove();
            return null;
        }
        le.customName(Txt.c(d.tag() + " &8| &7" + d.title()));
        le.setCustomNameVisible(true);
        le.setInvulnerable(true);
        le.setPersistent(false);
        le.setRemoveWhenFarAway(false);
        le.setSilent(true);
        le.setCollidable(false);
        le.setGravity(!d.hover());
        le.setGlowing(true);
        if (le instanceof Mob m) m.setAI(false);
        if (le instanceof Ageable a) a.setAdult();
        le.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
        AttributeInstance scale = le.getAttribute(Attribute.SCALE);
        if (scale != null) scale.setBaseValue(Math.max(0.3, Math.min(4.0, d.scale())));
        EntityEquipment eq = le.getEquipment();
        if (eq != null && d.helmet() != null) {
            eq.setHelmet(new ItemStack(d.helmet()));
            eq.setHelmetDropChance(0f);
        }
        le.getPersistentDataContainer().set(demonKey, PersistentDataType.STRING, d.id());
        try {
            Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = sb.getTeam("dp_" + d.id());
            if (team == null) team = sb.registerNewTeam("dp_" + d.id());
            NamedTextColor nc = NamedTextColor.NAMES.value(d.glow().toLowerCase());
            team.color(nc == null ? NamedTextColor.RED : nc);
            team.addEntry(le.getUniqueId().toString());
        } catch (Exception ex) {
            plugin.getLogger().warning("Équipe de couleur impossible : " + ex.getMessage());
        }
        return le;
    }

    /** Effets propres au temple pendant l'invocation : colonne de lumière vers l'oculus, colonnes qui s'embrasent. */
    private void templeSummonFx(Location base, int t, double prog, Color tint) {
        int H = pillarHeight();
        for (int i = 0; i < 3; i++) {
            double h = rnd.nextDouble() * (H + domeRadius()) * prog + 0.3;
            Fx.spawn(base.clone().add(rr(-0.35, 0.35), h, rr(-0.35, 0.35)), "end_rod", 1, 0.03, 0.1, 0.03, 0.01);
            Fx.spawn(base.clone().add(rr(-0.5, 0.5), h * 0.6, rr(-0.5, 0.5)), "soul_fire_flame", 1, 0.05, 0.1, 0.05, 0.01);
        }
        if (!pillarOffsets.isEmpty() && t % 4 == 0) {
            int count = (int) Math.min(pillarOffsets.size(), pillarOffsets.size() * Math.min(1.0, prog / 0.8) + 1);
            for (int i = 0; i < count; i++) {
                int[] o = pillarOffsets.get(i);
                Fx.spawn(base.clone().add(o[0], H - 1, o[1]), "soul_fire_flame", 3, 0.25, 0.3, 0.25, 0.02);
                double h = 1 + ((t + i * 3) % 24) * ((H - 2) / 24.0);
                Fx.dust(base.clone().add(o[0], h, o[1]), tint, 1.4f, 1, 0.1);
            }
        }
        if (t % 6 == 0) Fx.spawn(base.clone().add(0, H, 0), "reverse_portal", 25, 0.8, 0.3, 0.8, 0.3);
        if (t % 10 == 0) Fx.ring(base.clone().add(0, 0.3 + prog * H * 0.7, 0), 2.5, 24, "enchant");
    }

    // ------------------------------------------------------------------ Apparition automatique

    /** Apparition automatique : le premier joueur qui parle au démon reçoit la proposition. */
    public boolean summonAuto() {
        if (center == null || center.getWorld() == null || session != null || building) return false;
        Demon d = plugin.demons().random();
        if (d == null) return false;
        Session s = new Session(d);
        s.waitingFirst = true;
        session = s;
        startAnimation(s);
        return true;
    }

    /** Un joueur parle au démon (clic droit). Retourne true si l'interaction a été traitée. */
    public boolean claim(Player p) {
        Session s = session;
        if (s == null || !s.waitingFirst || s.ending || !s.active) return false;
        String err = null;
        if (!p.hasPermission("demon.use")) err = "Vous n'avez pas la permission de conclure un pacte.";
        else if (!plugin.pacts().hasCapacity(p.getUniqueId())) err = "Vous avez déjà conclu le maximum de pactes.";
        if (err != null) {
            p.sendMessage(Txt.c(plugin.prefix() + "&c" + err));
            return true;
        }
        s.waitingFirst = false;
        s.target = p.getUniqueId();
        Bukkit.broadcast(Txt.c("&4&l⚠ &c" + p.getName() + " &7a parlé à " + s.demon.tag() + "&7 : le démon lui propose un pacte !"));
        plugin.contracts().startOffer(s, p);
        return true;
    }

    /** Date (ms) de la prochaine apparition automatique, 0 si inconnue ou désactivée. */
    public long nextAuto() {
        return plugin.getConfig().getBoolean("auto-spawn.enabled", true) ? nextAuto : 0;
    }

    private File autoFile() { return new File(plugin.getDataFolder(), "autospawn.yml"); }

    private long interval() { return Math.max(1, plugin.getConfig().getLong("auto-spawn.interval-minutes", 120)) * 60_000L; }

    private void saveNext() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("next", nextAuto);
        try {
            plugin.getDataFolder().mkdirs();
            y.save(autoFile());
        } catch (IOException e) {
            plugin.getLogger().warning("autospawn.yml : " + e.getMessage());
        }
    }

    private void startAuto() {
        if (autoTask != null) autoTask.cancel();
        nextAuto = 0;
        autoTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    autoStep();
                } catch (Exception ex) {
                    plugin.getLogger().warning("Apparition automatique : " + ex);
                }
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }

    private void autoStep() {
        if (!plugin.getConfig().getBoolean("auto-spawn.enabled", true)) return;
        long now = System.currentTimeMillis();
        if (nextAuto == 0) {
            long saved = autoFile().exists() ? YamlConfiguration.loadConfiguration(autoFile()).getLong("next", 0) : 0;
            nextAuto = saved > 0 ? saved : now + interval();
            saveNext();
        }
        if (now < nextAuto) return;
        if (center == null || center.getWorld() == null || session != null) return;
        if (Bukkit.getOnlinePlayers().size() < plugin.getConfig().getInt("auto-spawn.min-players", 1)) return;
        if (summonAuto()) {
            nextAuto = now + interval();
            saveNext();
        }
    }

    // ------------------------------------------------------------------ Renvoi

    public boolean despawn() {
        if (session == null) return false;
        dismiss(session, "admin");
        return true;
    }

    /** Fait disparaître le démon avec une animation puis libère l'autel. */
    public void dismiss(Session s, String reason) {
        if (s == null || session != s || s.ending) return;
        s.ending = true;
        s.animating = false;
        plugin.contracts().cancelSession(s);
        final LivingEntity ent = s.entity;
        final Element el = s.demon.element();
        if (ent != null && ent.isValid()) Fx.sound(ent.getLocation(), "entity.enderman.teleport", 1.5f, 0.5f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (center == null || center.getWorld() == null) { cancel(); finishDismiss(s); return; }
                int per = Math.max(1, runes.size() / 30 + 1);
                for (int k = 0; k < per && lit > 0; k++) {
                    lit--;
                    runeBlock(runes.get(lit)).setType(RUNE_DIM, false);
                }
                if (ent != null && ent.isValid()) {
                    Location l = ent.getLocation().add(0, ent.getHeight() / 2, 0);
                    Fx.spawn(l, el.particle, 10, 0.4, 0.6, 0.4, 0.05);
                    Fx.dust(l, el.color, 1.5f, 4, 0.4);
                }
                t++;
                if (t >= 30) {
                    cancel();
                    finishDismiss(s);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void finishDismiss(Session s) {
        LivingEntity ent = s.entity;
        if (ent != null) {
            if (ent.isValid()) {
                Fx.spawn(ent.getLocation().add(0, ent.getHeight() / 2, 0), "large_smoke", 40, 0.5, 0.8, 0.5, 0.05);
                Fx.sound(ent.getLocation(), "entity.generic.explode", 0.8f, 0.6f);
            }
            removeFromTeam(s.demon, ent);
            ent.remove();
        }
        if (center != null && center.getWorld() != null) {
            while (lit > 0) {
                lit--;
                runeBlock(runes.get(lit)).setType(RUNE_DIM, false);
            }
        }
        if (session == s) session = null;
    }

    private void removeFromTeam(Demon d, Entity e) {
        try {
            Team t = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("dp_" + d.id());
            if (t != null) t.removeEntry(e.getUniqueId().toString());
        } catch (Exception ignored) {
            // sans importance
        }
    }

    private void dismissNow() {
        Session s = session;
        if (s == null) return;
        s.ending = true;
        plugin.contracts().cancelSession(s);
        finishDismiss(s);
    }

    /** Supprime les démons restés dans le monde (crash, rechargement). */
    public void purgeStray() {
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (isDemonEntity(e) && (session == null || session.entity == null || !session.entity.equals(e))) e.remove();
            }
        }
    }

    public void shutdown() {
        if (idleTask != null) idleTask.cancel();
        idleTask = null;
        if (autoTask != null) autoTask.cancel();
        autoTask = null;
        dismissNow();
    }

    // ------------------------------------------------------------------ Animation d'attente

    private void startIdle() {
        if (idleTask != null) idleTask.cancel();
        idleTask = null;
        if (!plugin.getConfig().getBoolean("stell.idle-animation", true)) return;
        idleTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    idleStep();
                } catch (Exception ex) {
                    plugin.getLogger().warning("Animation d'attente : " + ex);
                }
            }
        }.runTaskTimer(plugin, 20L, 4L);
    }

    private void idleStep() {
        if (center == null || center.getWorld() == null) return;
        World w = center.getWorld();
        if (!w.isChunkLoaded(center.getBlockX() >> 4, center.getBlockZ() >> 4)) return;
        Session s = session;
        if (s != null && (s.animating || s.ending)) return;
        double view = plugin.getConfig().getDouble("stell.idle-view-distance", 64);
        List<Player> near = playersNear(center, view);
        if (near.isEmpty()) return;
        idleTick++;
        boolean busy = s != null && s.active;
        Location base = center.clone().add(0.5, 0, 0.5);
        double ang = idleTick * 0.22;
        Color[] cols = {Color.fromRGB(190, 10, 20), Color.fromRGB(130, 30, 200), Color.fromRGB(12, 0, 16)};
        for (int i = 0; i < 3; i++) {
            double a = ang + i * 2 * Math.PI / 3;
            double r = (busy ? 2.6 : 2.2) + Math.sin(idleTick * 0.1 + i) * 0.3;
            double y = ((idleTick * 0.07) + i * 0.6) % 2.4;
            Fx.dust(base.clone().add(Math.cos(a) * r, 0.15 + y, Math.sin(a) * r), cols[i], 1.4f, 2, 0.04);
        }
        if (idleTick % 3 == 0 && !runes.isEmpty()) {
            int[] o = runes.get(rnd.nextInt(runes.size()));
            Location rl = base.clone().add(o[0], 0.1, o[1]);
            Fx.spawn(rl, "enchant", 3, 0.1, 0.4, 0.1, 0.05);
            if (rnd.nextInt(6) == 0) Fx.spawn(rl.add(0, 0.2, 0), "soul_fire_flame", 1, 0.05, 0.1, 0.05, 0.01);
        }
        if (idleTick % 15 == 0 && lit == 0) flicker(near, base);
        int H = pillarHeight();
        if (!pillarOffsets.isEmpty()) {
            int[] po = pillarOffsets.get((idleTick / 2) % pillarOffsets.size());
            if (idleTick % 2 == 0) {
                Fx.spawn(base.clone().add(po[0], 1.4, po[1]), "soul_fire_flame", 2, 0.2, 0.2, 0.2, 0.01);
                Fx.spawn(base.clone().add(po[0] + rr(-0.5, 0.5), rr(1, H - 2), po[1] + rr(-0.5, 0.5)), "soul", 1, 0.2, 0.1, 0.2, 0.01);
                Fx.spawn(base.clone().add(po[0], H - 1, po[1]), "smoke", 1, 0.2, 0.1, 0.2, 0.01);
            }
            if (busy && idleTick % 2 == 0) {
                Location cap = base.clone().add(po[0], H - 1, po[1]);
                Vector v = base.clone().add(0, 2, 0).toVector().subtract(cap.toVector()).multiply(rnd.nextDouble());
                Fx.dust(cap.clone().add(v), s.demon.element().color, 1.2f, 2, 0.02);
                if (rnd.nextInt(8) == 0) Fx.spawn(cap, "electric_spark", 8, 0.3, 0.3, 0.3, 0.1);
            }
        }
        if (idleTick % 2 == 0) {
            Fx.spawn(base.clone().add(rr(-0.4, 0.4), rr(1.5, H + domeRadius() + 6), rr(-0.4, 0.4)), "end_rod", 1, 0.03, 0.15, 0.03, 0.004);
            Fx.spawn(base.clone().add(rr(-3.5, 3.5), H - 0.5, rr(-3.5, 3.5)), "ash", 3, 0.4, 0.3, 0.4, 0.02);
        }
        double ringAngle = -idleTick * 0.12;
        for (int i = 0; i < 2; i++) {
            Fx.spawn(base.clone().add(Math.cos(ringAngle + i * Math.PI) * 2.6, 0.15, Math.sin(ringAngle + i * Math.PI) * 2.6),
                    "reverse_portal", 2, 0.05, 0.2, 0.05, 0.02);
        }
        if (idleTick % 80 == 0) {
            for (Player p : near) {
                if (p.getLocation().distanceSquared(center) <= 24 * 24) {
                    p.playSound(p.getLocation(), busy ? "entity.warden.heartbeat" : "block.sculk_sensor.clicking", 0.4f, 0.5f);
                }
            }
        }
        if (busy && s.entity != null && s.entity.isValid()) {
            Element el = s.demon.element();
            Location eye = s.entity.getLocation().add(0, s.entity.getHeight() / 2, 0);
            double oa = idleTick * 0.3;
            Fx.spawn(eye.clone().add(Math.cos(oa) * 1.4, 0, Math.sin(oa) * 1.4), el.particle, 2, 0.05, 0.05, 0.05, 0.01);
            Fx.spawn(eye.clone().add(Math.cos(oa + Math.PI) * 1.4, 0, Math.sin(oa + Math.PI) * 1.4), el.particle2, 2, 0.05, 0.05, 0.05, 0.01);
            Player t = s.target == null ? null : Bukkit.getPlayer(s.target);
            if (t != null && t.getWorld().equals(w)) {
                Vector v = t.getLocation().toVector().subtract(s.entity.getLocation().toVector());
                s.entity.setRotation((float) Math.toDegrees(Math.atan2(-v.getX(), v.getZ())), 0f);
            }
        }
    }

    /** Quelques runes vacillent (blocs envoyés au client uniquement). */
    private void flicker(List<Player> near, Location base) {
        if (runes.isEmpty()) return;
        BlockData litData = RUNE_LIT.createBlockData();
        List<Block> blocks = new ArrayList<>();
        for (int i = 0; i < 3; i++) blocks.add(runeBlock(runes.get(rnd.nextInt(runes.size()))));
        for (Block b : blocks) for (Player p : near) p.sendBlockChange(b.getLocation(), litData);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Block b : blocks) {
                BlockData real = b.getBlockData();
                for (Player p : near) if (p.isOnline()) p.sendBlockChange(b.getLocation(), real);
            }
        }, 12L);
    }
}

/**
 * Déguisement : clic droit avec une tête de joueur (déconnecté) = même pseudo et même skin.
 * Fin : 1h30, mort, /undeguise, ou reconnexion du joueur imité.
 */
final class DisguiseManager implements Listener {
    private record Disguise(PlayerProfile original, Component display, Component list,
                            UUID targetId, String targetName, long expiresAt) {}

    private final DemonPactPlugin plugin;
    private final Map<UUID, Disguise> active = new HashMap<>();
    private BukkitTask task;

    public DisguiseManager(DemonPactPlugin plugin) { this.plugin = plugin; }

    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Map.Entry<UUID, Disguise> en : new ArrayList<>(active.entrySet())) {
                    Player p = Bukkit.getPlayer(en.getKey());
                    if (p == null) { active.remove(en.getKey()); continue; }
                    if (now >= en.getValue().expiresAt()) remove(p, "&7Votre déguisement s'estompe.");
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        for (UUID id : new ArrayList<>(active.keySet())) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) remove(p, null);
        }
        active.clear();
    }

    public boolean isDisguised(UUID id) { return active.containsKey(id); }

    private void msg(Player p, String text) { p.sendMessage(Txt.c(plugin.prefix() + text)); }

    // ------------------------------------------------------------------ Début

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.PLAYER_HEAD) return;
        Player p = e.getPlayer();
        if (a == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null
                && e.getClickedBlock().getType().isInteractable() && !p.isSneaking()) return;
        if (!plugin.heads().isGenuine(it, null)) return;
        e.setCancelled(true);
        tryDisguise(p, it);
    }

    private void tryDisguise(Player p, ItemStack head) {
        if (!plugin.getConfig().getBoolean("disguise.enabled", true)) return;
        if (active.containsKey(p.getUniqueId())) {
            msg(p, "&cVous êtes déjà déguisé. Tapez &f/undeguise &cpour redevenir vous-même.");
            return;
        }
        if (!(head.getItemMeta() instanceof SkullMeta meta)) return;
        OfflinePlayer op = meta.getOwningPlayer();
        if (op == null || op.getName() == null) return;
        if (op.getUniqueId().equals(p.getUniqueId())) {
            msg(p, "&cC'est votre propre tête.");
            return;
        }
        if (plugin.getConfig().getBoolean("disguise.require-offline", true) && op.isOnline()) {
            msg(p, "&c" + op.getName() + " est connecté : vous ne pouvez prendre que l'identité d'un joueur déconnecté.");
            return;
        }
        for (Disguise d : active.values()) {
            if (d.targetId().equals(op.getUniqueId())) {
                msg(p, "&cQuelqu'un porte déjà l'identité de " + op.getName() + ".");
                return;
            }
        }
        ItemStack back = head.clone();
        back.setAmount(1);
        ItemStack hand = p.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);
        p.getInventory().setItemInMainHand(hand.getAmount() <= 0 ? null : hand);
        msg(p, "&7La tête se dissout... transformation en cours.");
        final UUID tid = op.getUniqueId();
        final String tname = op.getName();
        final PlayerProfile tp = Bukkit.createProfile(tid, tname);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean ok;
            try {
                ok = tp.complete(true) && tp.hasTextures();
            } catch (Exception ex) {
                ok = false;
            }
            final boolean fin = ok;
            Bukkit.getScheduler().runTask(plugin, () -> apply(p, tp, fin, tid, tname, back));
        });
    }

    private void giveBack(Player p, ItemStack head) {
        for (ItemStack rest : p.getInventory().addItem(head).values()) p.getWorld().dropItem(p.getLocation(), rest);
    }

    private void apply(Player p, PlayerProfile tp, boolean ok, UUID tid, String tname, ItemStack back) {
        if (!p.isOnline()) return;
        boolean taken = plugin.getConfig().getBoolean("disguise.require-offline", true) && Bukkit.getPlayer(tid) != null;
        if (!ok || taken || active.containsKey(p.getUniqueId())) {
            giveBack(p, back);
            msg(p, "&cImpossible de prendre cette identité (skin introuvable ou joueur connecté). La tête vous est rendue.");
            return;
        }
        PlayerProfile original = p.getPlayerProfile();
        PlayerProfile np = Bukkit.createProfile(p.getUniqueId(), tname);
        np.setProperties(tp.getProperties());
        long dur = plugin.getConfig().getLong("disguise.duration-seconds", 5400) * 1000L;
        active.put(p.getUniqueId(), new Disguise(original, p.displayName(), p.playerListName(), tid, tname,
                System.currentTimeMillis() + dur));
        p.setPlayerProfile(np);
        p.displayName(Component.text(tname));
        p.playerListName(Component.text(tname));
        Fx.spawn(p.getLocation().add(0, 1, 0), "witch", 50, 0.5, 1, 0.5, 0.1);
        Fx.spawn(p.getLocation().add(0, 1, 0), "poof", 30, 0.4, 0.8, 0.4, 0.05);
        Fx.sound(p.getLocation(), "entity.illusioner.mirror_move", 1f, 1f);
        msg(p, "&dVous êtes maintenant &f" + tname + "&d (" + Txt.mmss(dur / 1000) + "). Fin : mort, &f/undeguise&d ou reconnexion de " + tname + ".");
    }

    // ------------------------------------------------------------------ Fin

    public void remove(Player p, String reason) {
        Disguise d = active.remove(p.getUniqueId());
        if (d == null) return;
        try {
            p.setPlayerProfile(d.original());
        } catch (Exception ex) {
            plugin.getLogger().warning("Retour au profil d'origine impossible : " + ex);
        }
        p.displayName(d.display());
        p.playerListName(d.list());
        Fx.spawn(p.getLocation().add(0, 1, 0), "poof", 30, 0.4, 0.8, 0.4, 0.05);
        if (reason != null) msg(p, reason);
    }

    public void undisguiseCommand(Player p) {
        if (!active.containsKey(p.getUniqueId())) {
            msg(p, "&7Vous n'êtes pas déguisé.");
            return;
        }
        remove(p, "&7Vous reprenez votre apparence.");
    }

    public void quit(Player p) { active.remove(p.getUniqueId()); }

    /** Un joueur se connecte : ceux qui portent son identité la perdent. */
    public void joined(Player j) {
        for (Map.Entry<UUID, Disguise> en : new ArrayList<>(active.entrySet())) {
            if (en.getValue().targetId().equals(j.getUniqueId())) {
                Player p = Bukkit.getPlayer(en.getKey());
                if (p != null) remove(p, "&7" + j.getName() + " vient de se reconnecter : votre déguisement s'efface.");
                else active.remove(en.getKey());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String m = e.getMessage().toLowerCase(Locale.ROOT).trim();
        if (m.equals("/undeguise") || m.equals("/undisguise") || m.equals("/undéguise") || m.equals("/dédeguise")) {
            e.setCancelled(true);
            undisguiseCommand(e.getPlayer());
        }
    }
}

/** Élément d'un démon : détermine particules, sons et animation d'invocation. */
enum Element {
    WIND("cloud", "poof", Color.fromRGB(190, 235, 255), Material.WHITE_CONCRETE_POWDER, "entity.phantom.flap"),
    FIRE("flame", "lava", Color.fromRGB(255, 110, 20), Material.MAGMA_BLOCK, "entity.blaze.shoot"),
    LIGHTNING("electric_spark", "end_rod", Color.fromRGB(255, 240, 80), Material.YELLOW_CONCRETE, "entity.lightning_bolt.thunder"),
    TIME("end_rod", "enchant", Color.fromRGB(230, 200, 90), Material.GOLD_BLOCK, "block.bell.use"),
    SHADOW("squid_ink", "smoke", Color.fromRGB(35, 10, 55), Material.BLACK_CONCRETE, "entity.enderman.stare"),
    BLOOD("crimson_spore", "damage_indicator", Color.fromRGB(170, 0, 20), Material.NETHER_WART_BLOCK, "entity.witch.ambient"),
    EARTH("poof", "ash", Color.fromRGB(120, 85, 50), Material.COARSE_DIRT, "block.gravel.break"),
    WATER("splash", "bubble_pop", Color.fromRGB(40, 110, 220), Material.PRISMARINE_BRICKS, "entity.drowned.ambient"),
    DEATH("soul", "sculk_soul", Color.fromRGB(100, 220, 200), Material.SOUL_SOIL, "entity.wither.ambient"),
    ILLUSION("witch", "end_rod", Color.fromRGB(220, 80, 220), Material.AMETHYST_BLOCK, "entity.illusioner.cast_spell"),
    GRAVITY("reverse_portal", "crit", Color.fromRGB(110, 70, 160), Material.DEEPSLATE, "block.anvil.land"),
    VOID("portal", "reverse_portal", Color.fromRGB(70, 0, 110), Material.CRYING_OBSIDIAN, "entity.enderman.teleport");

    public final String particle;
    public final String particle2;
    public final Color color;
    public final Material floorFx;
    public final String sound;

    Element(String particle, String particle2, Color color, Material floorFx, String sound) {
        this.particle = particle;
        this.particle2 = particle2;
        this.color = color;
        this.floorFx = floorFx;
        this.sound = sound;
    }

    public static Element of(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (Exception e) {
            return VOID;
        }
    }
}

/** Effets visuels et sonores : les particules et sons sont résolus par leur clé (configurable). */
final class Fx {
    private static final Map<String, Particle> CACHE = new HashMap<>();

    private Fx() {}

    public static Particle particle(String key) {
        if (key == null || key.isBlank()) return null;
        String k = key.toLowerCase(Locale.ROOT).trim();
        if (k.startsWith("minecraft:")) k = k.substring(10);
        if (CACHE.containsKey(k)) return CACHE.get(k);
        Particle p = null;
        try {
            p = Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(k));
        } catch (Exception ignored) {
            // clé invalide : on ignore
        }
        CACHE.put(k, p);
        return p;
    }

    /** Particule sans donnée additionnelle (les types nécessitant une donnée sont ignorés). */
    public static void spawn(Location l, String key, int count, double dx, double dy, double dz, double speed) {
        if (l == null || l.getWorld() == null) return;
        Particle p = particle(key);
        if (p == null || p.getDataType() != Void.class) return;
        l.getWorld().spawnParticle(p, l, count, dx, dy, dz, speed);
    }

    public static void dust(Location l, Color color, float size, int count, double spread) {
        if (l == null || l.getWorld() == null) return;
        l.getWorld().spawnParticle(Particle.DUST, l, count, spread, spread, spread, 0, new Particle.DustOptions(color, size));
    }

    public static void block(Location l, Material mat, int count, double dx, double dy, double dz) {
        if (l == null || l.getWorld() == null || mat == null || !mat.isBlock()) return;
        BlockData data = mat.createBlockData();
        l.getWorld().spawnParticle(Particle.BLOCK, l, count, dx, dy, dz, 0, data);
    }

    public static void sound(Location l, String key, float volume, float pitch) {
        if (l == null || l.getWorld() == null || key == null || key.isBlank()) return;
        l.getWorld().playSound(l, key, volume, pitch);
    }

    /** Anneau de particules horizontal. */
    public static void ring(Location c, double r, int points, String key) {
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            spawn(c.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r), key, 1, 0, 0, 0, 0);
        }
    }

    public static void ringDust(Location c, double r, int points, Color color, float size) {
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            dust(c.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r), color, size, 1, 0);
        }
    }
}

/** Vérification et création de têtes de joueurs authentiques. */
final class Heads {
    private final DemonPactPlugin plugin;

    public Heads(DemonPactPlugin plugin) { this.plugin = plugin; }

    /**
     * Une tête est authentique si elle appartient à un vrai joueur déjà connecté au serveur
     * (UUID + nom concordants). Les têtes à texture libre, ou au nom inventé, sont refusées.
     */
    public boolean isGenuine(ItemStack it, Player sacrificer) {
        if (it == null || it.getType() != Material.PLAYER_HEAD || !it.hasItemMeta()) return false;
        if (!(it.getItemMeta() instanceof SkullMeta meta)) return false;
        OfflinePlayer op = meta.getOwningPlayer();
        if (op == null || op.getName() == null) return false;
        if (!op.isOnline() && !op.hasPlayedBefore()) return false;
        if (!plugin.getConfig().getBoolean("heads.allow-own-head", false)
                && sacrificer != null && op.getUniqueId().equals(sacrificer.getUniqueId())) return false;
        if (plugin.getConfig().getBoolean("heads.require-plugin-tag", false)) {
            Byte tag = meta.getPersistentDataContainer().get(plugin.items().headKey(), PersistentDataType.BYTE);
            return tag != null && tag == 1;
        }
        return true;
    }

    /** Crée la tête d'un joueur tué (marquée par le plugin). */
    public ItemStack create(Player victim, String killerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(victim);
        meta.displayName(Txt.item("&4Tête de &c" + victim.getName()));
        meta.lore(Txt.items(java.util.List.of("&7Tombée : &f" + killerName, "&8Sacrifice démoniaque / déguisement (clic droit)")));
        meta.getPersistentDataContainer().set(plugin.items().headKey(), PersistentDataType.BYTE, (byte) 1);
        head.setItemMeta(meta);
        return head;
    }
}

/** Un pacte conclu entre un joueur et un démon. */
final class Pact {
    private final UUID owner;
    private final String demonId;
    private final String contractKey;
    private final PowerType power;
    private UUID serial;
    private final long since;
    private double healthPaid = -1;

    public Pact(UUID owner, String demonId, String contractKey, PowerType power, UUID serial, long since) {
        this.owner = owner;
        this.demonId = demonId;
        this.contractKey = contractKey;
        this.power = power;
        this.serial = serial;
        this.since = since;
    }

    public UUID owner() { return owner; }
    public String demonId() { return demonId; }
    public String contractKey() { return contractKey; }
    public PowerType power() { return power; }
    /** Numéro de série de l'objet valide : toute copie portant un autre numéro est inerte. */
    public UUID serial() { return serial; }
    public void serial(UUID s) { this.serial = s; }
    public long since() { return since; }
    /** Vie (PV) sacrifiée pour ce pacte ; -1 = inconnu (ancien pacte). */
    public double healthPaid() { return healthPaid; }
    public void healthPaid(double v) { this.healthPaid = v; }
}

/** Création, validation et suppression des pactes, objets de pacte et sacrifices de vie. */
final class PactManager {
    private final DemonPactPlugin plugin;
    private final NamespacedKey healthKey;
    private final Map<UUID, List<ItemStack>> deathStash = new HashMap<>();

    public PactManager(DemonPactPlugin plugin) {
        this.plugin = plugin;
        this.healthKey = new NamespacedKey(plugin, "life_sacrifice");
    }

    public List<Pact> pactsOf(UUID id) { return plugin.data().get(id).pacts(); }

    public int maxActive() { return Math.max(1, plugin.getConfig().getInt("pact.max-active", 1)); }

    /** Retourne un message d'erreur si le joueur ne peut pas obtenir ce pouvoir, sinon null. */
    public String canTake(UUID id, PowerType power) {
        List<Pact> l = pactsOf(id);
        if (l.size() >= maxActive()) {
            return "Vous avez déjà conclu le nombre maximal de pactes (" + maxActive() + ").";
        }
        for (Pact p : l) if (p.power() == power) return "Vous possédez déjà ce pouvoir.";
        return null;
    }

    public boolean hasCapacity(UUID id) { return pactsOf(id).size() < maxActive(); }

    /** Enregistre le pacte, sauvegarde et donne l'objet. */
    public Pact grant(Player p, Demon d, DemonContract c) {
        PlayerData pd = plugin.data().get(p.getUniqueId());
        pd.name(p.getName());
        Pact pact = new Pact(p.getUniqueId(), d.id(), c.key(), c.power(), UUID.randomUUID(), System.currentTimeMillis());
        pact.healthPaid(0);
        pd.pacts().add(pact);
        plugin.data().saveNow();
        applyAttributes(p);
        giveItem(p, d, pact);
        return pact;
    }

    public void giveItem(Player p, Demon d, Pact pact) {
        ItemStack it = plugin.items().create(d, pact, p.getName());
        for (ItemStack rest : p.getInventory().addItem(it).values()) {
            Item drop = p.getWorld().dropItem(p.getLocation(), rest);
            drop.setOwner(p.getUniqueId());
        }
    }

    /** Retire l'objet existant, change le numéro de série (invalide toute copie) et redonne un objet neuf. */
    public boolean reissue(Player p, Pact pact) {
        Demon d = plugin.demons().get(pact.demonId());
        if (d == null) return false;
        removeItems(p, pact.serial());
        pact.serial(UUID.randomUUID());
        plugin.data().saveNow();
        giveItem(p, d, pact);
        return true;
    }

    /** Retourne le pacte correspondant à l'objet, uniquement si le joueur en est le propriétaire et que le numéro de série est à jour. */
    public Pact validate(Player p, DemonItem.Info info) {
        if (info == null || !info.owner().equals(p.getUniqueId())) return null;
        for (Pact pact : pactsOf(p.getUniqueId())) {
            if (pact.serial().equals(info.serial()) && pact.demonId().equalsIgnoreCase(info.demonId())) return pact;
        }
        return null;
    }

    public void remove(UUID id, Pact pact) {
        plugin.data().get(id).pacts().remove(pact);
        plugin.data().saveNow();
        Player p = Bukkit.getPlayer(id);
        if (p != null) {
            removeItems(p, pact.serial());
            applyAttributes(p);
            if (pact.power() == PowerType.WATER_BREATHING) p.removePotionEffect(PotionEffectType.WATER_BREATHING);
        }
    }

    public void removeItems(Player p, UUID serial) {
        ItemStack[] contents = p.getInventory().getContents();
        boolean changed = false;
        for (int i = 0; i < contents.length; i++) {
            DemonItem.Info info = plugin.items().read(contents[i]);
            if (info != null && info.serial().equals(serial)) {
                contents[i] = null;
                changed = true;
            }
        }
        if (changed) p.getInventory().setContents(contents);
        ItemStack[] ender = p.getEnderChest().getContents();
        boolean ec = false;
        for (int i = 0; i < ender.length; i++) {
            DemonItem.Info info = plugin.items().read(ender[i]);
            if (info != null && info.serial().equals(serial)) {
                ender[i] = null;
                ec = true;
            }
        }
        if (ec) p.getEnderChest().setContents(ender);
    }

    // ------------------------------------------------------------------ Vie maximale

    public double currentMax(Player p) {
        AttributeInstance a = p.getAttribute(Attribute.MAX_HEALTH);
        return a == null ? 20.0 : a.getValue();
    }

    /** Vérifie que perdre hp points de vie max laisse au joueur au moins le minimum configuré. */
    public boolean canLose(Player p, double hp) {
        double min = plugin.getConfig().getDouble("sacrifice.min-max-health", 6.0);
        return currentMax(p) - hp >= min;
    }

    public double heartHealth() { return plugin.getConfig().getDouble("sacrifice.heart-health", 2.0); }

    public void addLostHealth(Player p, double hp) {
        PlayerData pd = plugin.data().get(p.getUniqueId());
        pd.name(p.getName());
        pd.lostHealth(pd.lostHealth() + hp);
        plugin.data().saveNow();
        applyHealth(p);
    }

    public void restoreHealth(UUID id) {
        PlayerData pd = plugin.data().get(id);
        pd.lostHealth(0);
        plugin.data().saveNow();
        Player p = Bukkit.getPlayer(id);
        if (p != null) applyHealth(p);
    }

    /** (Ré)applique le modificateur permanent de vie maximale du joueur. */
    public void applyHealth(Player p) {
        AttributeInstance a = p.getAttribute(Attribute.MAX_HEALTH);
        if (a == null) return;
        for (AttributeModifier m : new ArrayList<>(a.getModifiers())) {
            if (healthKey.equals(m.getKey())) a.removeModifier(m);
        }
        PlayerData pd = plugin.data().get(p.getUniqueId());
        double net = pd.stolenHealth() - pd.lostHealth();
        if (net != 0) {
            a.addModifier(new AttributeModifier(healthKey, net, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        }
        double max = a.getValue();
        if (p.getHealth() > max) p.setHealth(max);
        applyAttributes(p);
    }

    private NamespacedKey attrKey(PowerType t, Power.AttrSpec a) {
        return new NamespacedKey(plugin, "pwr_" + t.name().toLowerCase() + "_" + a.name());
    }

    /** (Ré)applique les attributs des pouvoirs possédés et retire ceux des pouvoirs que le joueur n'a plus (réinitialisation). */
    public void applyAttributes(Player p) {
        double mult = plugin.getConfig().getDouble("powers-settings.attribute-multiplier", 1.0);
        Set<PowerType> owned = new HashSet<>();
        for (Pact pact : pactsOf(p.getUniqueId())) owned.add(pact.power());
        for (PowerType type : PowerType.values()) {
            Power pw = plugin.powers().power(type);
            if (pw == null || pw.attributes().isEmpty()) continue;
            boolean has = owned.contains(type) && pw.enabled();
            for (Power.AttrSpec a : pw.attributes()) {
                AttributeInstance inst = p.getAttribute(a.attribute());
                if (inst == null) continue;
                NamespacedKey key = attrKey(type, a);
                for (AttributeModifier m : new ArrayList<>(inst.getModifiers())) {
                    if (key.equals(m.getKey())) inst.removeModifier(m);
                }
                if (has) inst.addModifier(new AttributeModifier(key, a.amount() * mult, a.operation(), EquipmentSlotGroup.ANY));
            }
        }
        double max = currentMax(p);
        if (p.getHealth() > max) p.setHealth(max);
    }

    /** Rend les cœurs sacrifiés pour ce pacte (abandon). Retourne les PV rendus. */
    public double refund(UUID id, Pact pact) {
        if (!plugin.getConfig().getBoolean("pact.refund-on-abandon", true)) return 0;
        double paid = pact.healthPaid();
        if (paid < 0) {
            Demon d = plugin.demons().get(pact.demonId());
            DemonContract c = d == null ? null : d.contract(pact.contractKey());
            paid = c == null ? 0 : plugin.contracts().heartsNeeded(c);
        }
        PlayerData pd = plugin.data().get(id);
        double back = Math.min(paid, pd.lostHealth());
        pd.lostHealth(pd.lostHealth() - back);
        plugin.data().saveNow();
        Player p = Bukkit.getPlayer(id);
        if (p != null) applyHealth(p);
        return back;
    }

    /** Le tueur vole des cœurs à sa victime (vie maximale permanente). Retourne true si le vol a eu lieu. */
    public boolean stealHeart(Player killer, Player victim) {
        double hp = Math.max(1, plugin.getConfig().getInt("steal.hearts", 1)) * heartHealth();
        PlayerData kd = plugin.data().get(killer.getUniqueId());
        PlayerData vd = plugin.data().get(victim.getUniqueId());
        double cap = plugin.getConfig().getDouble("steal.max-bonus-hearts", 10) * heartHealth();
        if (kd.stolenHealth() + hp > cap || !canLose(victim, hp)) return false;
        kd.name(killer.getName());
        vd.name(victim.getName());
        kd.stolenHealth(kd.stolenHealth() + hp);
        vd.stolenHealth(vd.stolenHealth() - hp);
        plugin.data().saveNow();
        applyHealth(killer);
        applyHealth(victim);
        int hearts = (int) (hp / heartHealth());
        killer.sendMessage(Txt.c(plugin.prefix() + "&c+" + hearts + " ❤ &7volé à &f" + victim.getName()
                + " &8(vie max : " + (int) (currentMax(killer) / 2) + " ❤)"));
        victim.sendMessage(Txt.c(plugin.prefix() + "&c-" + hearts + " ❤ &7: &f" + killer.getName() + " &7vous a volé de la vie."));
        Fx.spawn(killer.getLocation().add(0, 1, 0), "heart", 12, 0.5, 0.8, 0.5, 0.1);
        Fx.sound(killer.getLocation(), "entity.wither.spawn", 0.5f, 1.8f);
        return true;
    }

    // ------------------------------------------------------------------ Objets conservés à la mort

    public void stash(UUID id, List<ItemStack> items) { deathStash.put(id, items); }

    public List<ItemStack> popStash(UUID id) { return deathStash.remove(id); }
}

/** Données persistantes d'un joueur (identifié par UUID). */
final class PlayerData {
    private final UUID uuid;
    private String name = "";
    private double lostHealth;
    private long lastReclaim;
    private double stolenHealth;
    private final List<Pact> pacts = new ArrayList<>();

    public PlayerData(UUID uuid) { this.uuid = uuid; }

    public UUID uuid() { return uuid; }
    public String name() { return name; }
    public void name(String n) { this.name = n == null ? "" : n; }
    public double lostHealth() { return lostHealth; }
    public void lostHealth(double v) { this.lostHealth = Math.max(0, v); }
    public double stolenHealth() { return stolenHealth; }
    public void stolenHealth(double v) { this.stolenHealth = v; }
    public long lastReclaim() { return lastReclaim; }
    public void lastReclaim(long t) { this.lastReclaim = t; }
    public List<Pact> pacts() { return pacts; }
}

/** Stockage YAML des pactes et des sacrifices de vie (players.yml). */
final class PlayerDataManager {
    private final DemonPactPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerData> data = new HashMap<>();
    private boolean dirty;

    public PlayerDataManager(DemonPactPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        data.clear();
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = y.getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                ConfigurationSection s = root.getConfigurationSection(key);
                if (s == null) continue;
                PlayerData pd = new PlayerData(id);
                pd.name(s.getString("name", ""));
                pd.lostHealth(s.getDouble("lost-health", 0));
                pd.lastReclaim(s.getLong("last-reclaim", 0));
                pd.stolenHealth(s.getDouble("stolen-health", 0));
                for (Map<?, ?> m : s.getMapList("pacts")) {
                    PowerType pt = PowerType.of(String.valueOf(m.get("power")));
                    if (pt == null) continue;
                    Object since = m.get("since");
                    Pact np = new Pact(id, String.valueOf(m.get("demon")), String.valueOf(m.get("contract")), pt,
                            UUID.fromString(String.valueOf(m.get("serial"))),
                            since == null ? System.currentTimeMillis() : Long.parseLong(String.valueOf(since)));
                    Object paid = m.get("paid");
                    if (paid != null) np.healthPaid(Double.parseDouble(String.valueOf(paid)));
                    pd.pacts().add(np);
                }
                data.put(id, pd);
            } catch (Exception ex) {
                plugin.getLogger().warning("Données illisibles pour " + key + " : " + ex.getMessage());
            }
        }
    }

    public PlayerData get(UUID id) {
        return data.computeIfAbsent(id, PlayerData::new);
    }

    public boolean has(UUID id) { return data.containsKey(id); }

    public void markDirty() { dirty = true; }

    /** Sauvegarde immédiate (appelée à chaque changement de pacte). */
    public void saveNow() {
        YamlConfiguration y = new YamlConfiguration();
        for (PlayerData pd : data.values()) {
            if (pd.pacts().isEmpty() && pd.lostHealth() <= 0 && pd.stolenHealth() == 0) continue;
            String p = "players." + pd.uuid();
            y.set(p + ".name", pd.name());
            y.set(p + ".lost-health", pd.lostHealth());
            y.set(p + ".last-reclaim", pd.lastReclaim());
            y.set(p + ".stolen-health", pd.stolenHealth());
            List<Map<String, Object>> list = new ArrayList<>();
            for (Pact pact : pd.pacts()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("demon", pact.demonId());
                m.put("contract", pact.contractKey());
                m.put("power", pact.power().name());
                m.put("serial", pact.serial().toString());
                m.put("since", pact.since());
                if (pact.healthPaid() >= 0) m.put("paid", pact.healthPaid());
                list.add(m);
            }
            y.set(p + ".pacts", list);
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            y.save(file);
            dirty = false;
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder players.yml : " + e.getMessage());
        }
    }

    /** Sauvegarde périodique si des changements en attente. */
    public void saveIfDirty() {
        if (dirty) saveNow();
    }
}

/** Pouvoir configuré (valeurs effectives après lecture de config.yml). */
record Power(PowerType type, String name, String description, double cooldown, double duration,
                    double range, double strength, double damage, String particle, String sound, boolean enabled,
                    List<PotionSpec> buffs, List<PotionSpec> debuffs, List<AttrSpec> attributes) {

    /** Effet de potion : buffs pour le lanceur, debuffs pour les ennemis touchés. */
    public record PotionSpec(PotionEffectType type, double seconds, int amplifier) {}

    /** Modification permanente d'un attribut du joueur tant que le pacte existe. */
    public record AttrSpec(Attribute attribute, String name, double amount, AttributeModifier.Operation operation) {}
}

/** Exécution des 36 pouvoirs, sécurité (cooldowns, zones, propriétaire) et événements associés. */
final class PowerManager implements Listener {

    private static final class Frozen {
        long until;
        boolean ai = true;
        boolean gravity = true;
        boolean projectile;
        Vector vel = new Vector();
        String particle = "end_rod";
        int n;
    }

    private final DemonPactPlugin plugin;
    private final Map<PowerType, Power> powers = new EnumMap<>(PowerType.class);
    private final Map<UUID, Long> noFall = new HashMap<>();
    private final Map<UUID, Long> lifesteal = new HashMap<>();
    private final Map<UUID, Frozen> frozen = new HashMap<>();
    private final Map<UUID, UUID> servants = new HashMap<>();
    private final Set<UUID> clones = new HashSet<>();
    private final Set<String> walls = new HashSet<>();
    private final Map<UUID, Power> lastUsed = new HashMap<>();
    private final Map<UUID, List<ConfigurationSection>> passives = new HashMap<>();
    private boolean inPassive;
    private final NamespacedKey tempKey;
    private final NamespacedKey ownerKey;
    private final Random rnd = new Random();
    private Material wallMaterial = Material.STONE_BRICKS;
    private BukkitTask ticker;
    private BukkitTask passive;
    private int tickCount;

    public PowerManager(DemonPactPlugin plugin) {
        this.plugin = plugin;
        this.tempKey = new NamespacedKey(plugin, "temp_entity");
        this.ownerKey = new NamespacedKey(plugin, "temp_owner");
    }

    public NamespacedKey tempKey() { return tempKey; }

    public Power power(PowerType t) { return powers.get(t); }

    // ------------------------------------------------------------------ Chargement

    public void load() {
        powers.clear();
        for (PowerType t : PowerType.values()) {
            ConfigurationSection s = plugin.getConfig().getConfigurationSection("powers." + t.name());
            Demon d = plugin.demons().get(t.demon());
            Element el = d == null ? Element.VOID : d.element();
            if (s == null) {
                powers.put(t, new Power(t, t.label(), t.description(), t.cooldown(), t.duration(), t.range(),
                        t.strength(), t.damage(), el.particle, el.sound, true, List.of(), List.of(), List.of()));
            } else {
                powers.put(t, new Power(t, s.getString("name", t.label()), s.getString("description", t.description()),
                        s.getDouble("cooldown", t.cooldown()), s.getDouble("duration", t.duration()),
                        s.getDouble("range", t.range()), s.getDouble("strength", t.strength()),
                        s.getDouble("damage", t.damage()), s.getString("particle", el.particle),
                        s.getString("sound", el.sound), s.getBoolean("enabled", true),
                        specs(s, "buffs"), specs(s, "debuffs"), attrs(s)));
            }
        }
        powers.replaceAll((k, v) -> boost(v));
        Material m = Material.matchMaterial(plugin.getConfig().getString("powers-settings.wall-material", "STONE_BRICKS"));
        wallMaterial = m == null || !m.isBlock() ? Material.STONE_BRICKS : m;
        if (ticker == null) {
            restoreLeftoverWalls();
            ticker = new BukkitRunnable() {
                @Override
                public void run() { tick(); }
            }.runTaskTimer(plugin, 2L, 2L);
            passive = new BukkitRunnable() {
                @Override
                public void run() { passiveTick(); }
            }.runTaskTimer(plugin, 40L, 40L);
        }
    }

    public void shutdown() {
        if (ticker != null) ticker.cancel();
        if (passive != null) passive.cancel();
        ticker = null;
        passive = null;
        for (UUID id : new ArrayList<>(frozen.keySet())) {
            Entity e = Bukkit.getEntity(id);
            release(e, frozen.get(id));
        }
        frozen.clear();
        for (UUID id : new ArrayList<>(servants.keySet())) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        servants.clear();
        for (UUID id : new ArrayList<>(clones)) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        clones.clear();
        restoreAllWalls();
    }

    // ------------------------------------------------------------------ Boucles

    private void tick() {
        long now = System.currentTimeMillis();
        tickCount++;
        Iterator<Map.Entry<UUID, Frozen>> it = frozen.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Frozen> en = it.next();
            Frozen f = en.getValue();
            Entity e = Bukkit.getEntity(en.getKey());
            if (e == null || !e.isValid() || now >= f.until) {
                release(e, f);
                it.remove();
                continue;
            }
            e.setVelocity(new Vector(0, 0, 0));
            if (++f.n % 5 == 0) Fx.spawn(e.getLocation().add(0, e.getHeight() + 0.2, 0), f.particle, 3, 0.2, 0.1, 0.2, 0);
        }
        noFall.values().removeIf(t -> t < now);
        lifesteal.values().removeIf(t -> t < now);
        if (tickCount % 5 == 0) servantAi();
    }

    private void release(Entity e, Frozen f) {
        if (e == null || f == null) return;
        if (f.projectile) {
            e.setGravity(f.gravity);
            e.setVelocity(f.vel);
        } else if (e instanceof Mob m) {
            m.setAI(f.ai);
        } else if (e instanceof Player p) {
            p.setFallDistance(0);
        }
    }

    private void passiveTick() {
        passives.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            List<ConfigurationSection> list = new ArrayList<>();
            boolean zone = plugin.regionGuard().isDisabled(p.getLocation());
            for (Pact pact : plugin.pacts().pactsOf(p.getUniqueId())) {
                Power pw = powers.get(pact.power());
                if (pw == null || !pw.enabled() || zone) continue;
                if (pact.power() == PowerType.WATER_BREATHING) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 400, 0, false, false, true));
                }
                if (!hasItem(p, pact)) continue;
                ConfigurationSection sec = plugin.getConfig().getConfigurationSection("demons." + pact.demonId() + ".passive");
                if (sec == null || list.contains(sec)) continue;
                list.add(sec);
                for (Map<?, ?> m : sec.getMapList("effects")) {
                    PotionEffectType type = effect(String.valueOf(m.get("effect")));
                    if (type == null) continue;
                    p.addPotionEffect(new PotionEffect(type, 260,
                            Math.min(3, (int) num(m.get("amplifier"), 0) + plugin.getConfig().getInt("powers-settings.passive-amplifier-bonus", 1)),
                            false, false, true));
                }
            }
            if (!list.isEmpty()) passives.put(p.getUniqueId(), list);
        }
    }

    /** Renforce un pouvoir avec les multiplicateurs de config (dégâts, portée, durée, force, recharge, bonus d'effets). */
    private Power boost(Power p) {
        var c = plugin.getConfig();
        double dm = c.getDouble("powers-settings.damage-multiplier", 2.0);
        double rm = c.getDouble("powers-settings.range-multiplier", 1.4);
        double tm = c.getDouble("powers-settings.duration-multiplier", 1.5);
        double sm = c.getDouble("powers-settings.strength-multiplier", 1.3);
        double cm = c.getDouble("powers-settings.cooldown-multiplier", 0.8);
        int bb = c.getInt("powers-settings.buff-amplifier-bonus", 1);
        int db = c.getInt("powers-settings.debuff-amplifier-bonus", 1);
        double strength = p.strength() * sm;
        switch (p.type()) {
            case SUMMON_SERVANTS, CLONES -> strength = p.strength() + 2;
            case SLOW_ENEMIES, HASTE_SELF, REGEN, DAMAGE_RESIST, LOW_GRAVITY, LEVITATE_ZONE -> strength = Math.min(4, p.strength() + 1);
            default -> { }
        }
        List<Power.PotionSpec> buffs = new ArrayList<>();
        for (Power.PotionSpec s : p.buffs()) buffs.add(new Power.PotionSpec(s.type(), s.seconds() * tm, Math.min(4, s.amplifier() + bb)));
        List<Power.PotionSpec> debuffs = new ArrayList<>();
        for (Power.PotionSpec s : p.debuffs()) debuffs.add(new Power.PotionSpec(s.type(), s.seconds() * tm, Math.min(4, s.amplifier() + db)));
        return new Power(p.type(), p.name(), p.description(), p.cooldown() * cm, p.duration() * tm, p.range() * rm,
                strength, p.damage() * dm, p.particle(), p.sound(), p.enabled(), buffs, debuffs, p.attributes());
    }

    /** Applique le multiplicateur des bonus passifs (plafonné). */
    private double pb(double v, double cap) {
        return Math.min(cap, v * plugin.getConfig().getDouble("powers-settings.passive-multiplier", 1.5));
    }

    private boolean hasItem(Player p, Pact pact) {
        for (ItemStack it : p.getInventory().getContents()) {
            DemonItem.Info i = plugin.items().read(it);
            if (i != null && i.serial().equals(pact.serial())) return true;
        }
        return false;
    }

    private static PotionEffectType effect(String name) {
        try {
            return Registry.EFFECT.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT).trim()));
        } catch (Exception e) {
            return null;
        }
    }

    private static double num(Object o, double def) {
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return def;
        }
    }

    private List<Power.PotionSpec> specs(ConfigurationSection s, String key) {
        List<Power.PotionSpec> out = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList(key)) {
            PotionEffectType type = effect(String.valueOf(m.get("effect")));
            if (type == null) {
                plugin.getLogger().warning("Effet inconnu dans " + s.getName() + "." + key + " : " + m.get("effect"));
                continue;
            }
            out.add(new Power.PotionSpec(type, num(m.get("seconds"), 5), (int) num(m.get("amplifier"), 0)));
        }
        return out;
    }

    private static Attribute attribute(String n) {
        for (String pre : new String[]{"", "generic.", "player."}) {
            try {
                Attribute a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(pre + n));
                if (a != null) return a;
            } catch (Exception ignored) {
                // clé invalide : on essaie la suivante
            }
        }
        return null;
    }

    private List<Power.AttrSpec> attrs(ConfigurationSection s) {
        List<Power.AttrSpec> out = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("attributes")) {
            String name = String.valueOf(m.get("attribute")).toLowerCase(Locale.ROOT).trim();
            Attribute at = attribute(name);
            if (at == null) {
                plugin.getLogger().warning("Attribut inconnu dans " + s.getName() + " : " + name);
                continue;
            }
            AttributeModifier.Operation op;
            try {
                op = AttributeModifier.Operation.valueOf(String.valueOf(m.get("operation")).toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                op = AttributeModifier.Operation.ADD_NUMBER;
            }
            out.add(new Power.AttrSpec(at, name.replaceAll("[^a-z0-9_]", "_"), num(m.get("amount"), 0), op));
        }
        return out;
    }

    private String fmtNum(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.2f", v);
    }

    private String attrText(Power.AttrSpec a) {
        double v = a.amount();
        String val = a.operation() == AttributeModifier.Operation.ADD_NUMBER ? fmtNum(v) : fmtNum(v * 100) + "%";
        return Txt.pretty(a.name()) + " " + (v >= 0 ? "+" : "") + val;
    }

    private void applyDebuffs(Player p, Power pw, LivingEntity e) {
        if (pw == null) return;
        for (Power.PotionSpec d : pw.debuffs()) potion(e, d.type(), d.seconds(), d.amplifier());
    }

    private String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    private String specName(Power.PotionSpec s) {
        return Txt.pretty(s.type().getKey().getKey()) + " " + roman(s.amplifier() + 1)
                + " (" + (s.seconds() == Math.floor(s.seconds()) ? String.valueOf((long) s.seconds()) : String.valueOf(s.seconds())) + "s)";
    }

    /** Lignes décrivant les bonus et malus d'un pouvoir. */
    public List<String> bonusLines(Power pw) {
        List<String> out = new ArrayList<>();
        if (!pw.buffs().isEmpty()) out.add("Bonus : " + String.join(", ", pw.buffs().stream().map(this::specName).toList()));
        if (!pw.attributes().isEmpty()) {
            out.add("Attributs permanents : " + String.join(", ", pw.attributes().stream().map(this::attrText).toList()));
        }
        if (!pw.debuffs().isEmpty()) out.add("Malus ennemis : " + String.join(", ", pw.debuffs().stream().map(this::specName).toList()));
        return out;
    }

    /** Lignes décrivant les bonus passifs d'un démon (actifs si l'objet est dans l'inventaire). */
    public List<String> passiveLines(String demonId) {
        List<String> out = new ArrayList<>();
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("demons." + demonId + ".passive");
        if (s == null) return out;
        List<String> eff = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("effects")) {
            eff.add(Txt.pretty(String.valueOf(m.get("effect"))) + " " + roman(Math.min(3, (int) num(m.get("amplifier"), 0) + plugin.getConfig().getInt("powers-settings.passive-amplifier-bonus", 1)) + 1));
        }
        if (!eff.isEmpty()) out.add("Effets permanents : " + String.join(", ", eff));
        if (s.getDouble("ignite-seconds", 0) > 0) out.add("Vos coups enflamment la cible");
        if (s.getDouble("lifesteal", 0) > 0) out.add("Vol de vie passif : " + (int) (pb(s.getDouble("lifesteal"), 0.5) * 100) + "%");
        if (s.getDouble("bonus-damage", 0) > 0) out.add("Dégâts +" + (int) (pb(s.getDouble("bonus-damage"), 1.0) * 100) + "%");
        if (s.getDouble("dodge-chance", 0) > 0) out.add("Esquive : " + (int) (pb(s.getDouble("dodge-chance"), 0.4) * 100) + "%");
        if (s.getDouble("lightning-chance", 0) > 0) out.add("Foudre sur coup : " + (int) (pb(s.getDouble("lightning-chance"), 0.5) * 100) + "%");
        ConfigurationSection oh = s.getConfigurationSection("on-hit");
        if (oh != null) out.add(Txt.pretty(oh.getString("effect", "")) + " sur coup (" + (int) (pb(oh.getDouble("chance", 0.2), 0.6) * 100) + "%)");
        return out;
    }

    // ------------------------------------------------------------------ Utilisation

    public void use(Player p, Pact pact) {
        Power pw = powers.get(pact.power());
        Demon d = plugin.demons().get(pact.demonId());
        String col = d == null ? "&5" : d.color();
        if (pw == null || !pw.enabled()) {
            p.sendActionBar(Txt.c("&cCe pouvoir est désactivé."));
            return;
        }
        if (plugin.regionGuard().isDisabled(p.getLocation())) {
            p.sendActionBar(Txt.c("&cLes pouvoirs démoniaques sont inertes dans cette zone."));
            return;
        }
        if (frozen.containsKey(p.getUniqueId())) return;
        if (pw.type().passive()) {
            p.sendActionBar(Txt.c(col + pw.name() + " &7: pouvoir passif, toujours actif."));
            return;
        }
        CooldownManager cd = plugin.cooldowns();
        if (cd.spam(p.getUniqueId(), plugin.getConfig().getLong("powers-settings.anti-spam-ms", 500))) return;
        String key = pact.power().name();
        long rem = cd.remaining(p.getUniqueId(), key);
        if (rem > 0) {
            p.sendActionBar(Txt.c("&c" + pw.name() + " &7: recharge &f" + String.format(Locale.ROOT, "%.1f", rem / 1000.0) + "s"));
            Fx.sound(p.getLocation(), "block.note_block.bass", 0.5f, 0.6f);
            return;
        }
        lastUsed.put(p.getUniqueId(), pw);
        boolean ok;
        try {
            ok = execute(p, pw);
        } catch (Exception ex) {
            plugin.getLogger().warning("Erreur du pouvoir " + pw.type() + " : " + ex);
            ok = false;
        }
        if (ok) {
            cd.set(p.getUniqueId(), key, (long) (pw.cooldown() * 1000));
            for (Power.PotionSpec b : pw.buffs()) potion(p, b.type(), b.seconds(), b.amplifier());
            p.sendActionBar(Txt.c(col + pw.name() + " &7activé"));
        }
    }

    private boolean execute(Player p, Power pw) {
        return switch (pw.type()) {
            case AIR_DASH -> dash(p, pw, 0);
            case AIR_JUMP -> airJump(p, pw);
            case TORNADO -> tornado(p, pw);
            case FIRE_DASH -> dash(p, pw, 1);
            case FLAME_BURST -> flameBurst(p, pw);
            case FIRE_RESISTANCE -> fireResistance(p, pw);
            case LIGHTNING_STRIKE -> lightning(p, pw);
            case ELECTRIC_DASH -> dash(p, pw, 2);
            case ELECTRIC_FIELD -> electricField(p, pw);
            case TIME_STOP -> timeStop(p, pw);
            case SLOW_ENEMIES -> slowEnemies(p, pw);
            case HASTE_SELF -> hasteSelf(p, pw);
            case INVISIBILITY -> invisibility(p, pw);
            case SHORT_TELEPORT -> shortTeleport(p, pw);
            case DARK_ZONE -> darkZone(p, pw);
            case LIFESTEAL -> lifestealMode(p, pw);
            case SHOCKWAVE -> shockwave(p, pw);
            case REGEN -> regen(p, pw);
            case SEISMIC_WAVE -> seismic(p, pw);
            case STONE_WALL -> stoneWall(p, pw);
            case DAMAGE_RESIST -> damageResist(p, pw);
            case WATER_PRISON -> waterPrison(p, pw);
            case WAVE -> wave(p, pw);
            case WATER_BREATHING -> false;
            case SUMMON_SERVANTS -> summonServants(p, pw);
            case FEAR_AURA -> fearAura(p, pw);
            case CURSE -> curse(p, pw);
            case CLONES -> cloneSpell(p, pw);
            case TELEPORT -> phantomTeleport(p, pw);
            case CONFUSION -> confusion(p, pw);
            case LEVITATE_ZONE -> levitateZone(p, pw);
            case GRAVITY_CRUSH -> gravityCrush(p, pw);
            case LOW_GRAVITY -> lowGravity(p, pw);
            case RIFT -> rift(p, pw);
            case VOID_PROJECTILE -> voidProjectile(p, pw);
            case VOID_TELEPORT -> voidTeleport(p, pw);
        };
    }

    // ------------------------------------------------------------------ Aides

    private double rr(double a, double b) { return a + rnd.nextDouble() * (b - a); }

    private void potion(LivingEntity e, PotionEffectType t, double seconds, int amp) {
        e.addPotionEffect(new PotionEffect(t, (int) Math.max(1, seconds * 20), Math.max(0, amp), false, true, true));
    }

    private void potionHidden(LivingEntity e, PotionEffectType t, double seconds, int amp) {
        e.addPotionEffect(new PotionEffect(t, (int) Math.max(1, seconds * 20), Math.max(0, amp), false, false, true));
    }

    private void damage(Player p, LivingEntity t, double amount) {
        if (amount <= 0) return;
        t.damage(amount, p);
        applyDebuffs(p, lastUsed.get(p.getUniqueId()), t);
    }

    private boolean isTemp(Entity e) {
        return e.getPersistentDataContainer().has(tempKey, PersistentDataType.BYTE);
    }

    private boolean isEnemy(Player owner, LivingEntity e) {
        if (e.equals(owner) || e instanceof ArmorStand || e.isDead() || !e.isValid()) return false;
        if (plugin.spawner().isDemonEntity(e) || isTemp(e)) return false;
        UUID so = servants.get(e.getUniqueId());
        if (so != null && so.equals(owner.getUniqueId())) return false;
        if (e instanceof Player t) {
            if (!plugin.getConfig().getBoolean("powers-settings.affect-players", true)) return false;
            if (t.getGameMode() == GameMode.CREATIVE || t.getGameMode() == GameMode.SPECTATOR) return false;
        }
        return !(e instanceof Tameable tm && tm.isTamed() && owner.equals(tm.getOwner()));
    }

    private List<LivingEntity> enemies(Player p, Location c, double r) {
        List<LivingEntity> out = new ArrayList<>();
        if (c.getWorld() == null) return out;
        for (LivingEntity e : c.getWorld().getNearbyLivingEntities(c, r)) if (isEnemy(p, e)) out.add(e);
        return out;
    }

    private Vector flat(Player p) {
        Vector d = p.getLocation().getDirection();
        d.setY(0);
        if (d.lengthSquared() < 1e-4) return new Vector(1, 0, 0);
        return d.normalize();
    }

    private Vector away(Location from, Location to, double str, double y) {
        Vector v = to.toVector().subtract(from.toVector());
        v.setY(0);
        if (v.lengthSquared() < 1e-4) v = new Vector(rr(-1, 1), 0, rr(-1, 1));
        if (v.lengthSquared() < 1e-4) v = new Vector(1, 0, 0);
        v.normalize().multiply(str);
        v.setY(y);
        return v;
    }

    private LivingEntity findTarget(Player p, double range) {
        Location eye = p.getEyeLocation();
        RayTraceResult r = p.getWorld().rayTrace(eye, eye.getDirection(), range, FluidCollisionMode.NEVER, true, 0.6,
                en -> en instanceof LivingEntity le && isEnemy(p, le));
        if (r != null && r.getHitEntity() instanceof LivingEntity le) return le;
        LivingEntity best = null;
        double bestDot = 0.93;
        for (LivingEntity le : enemies(p, p.getLocation(), range)) {
            Vector to = le.getEyeLocation().toVector().subtract(eye.toVector());
            if (to.lengthSquared() < 1e-4) continue;
            double dot = to.normalize().dot(eye.getDirection());
            if (dot > bestDot && p.hasLineOfSight(le)) {
                bestDot = dot;
                best = le;
            }
        }
        return best;
    }

    private Location ground(Location l) {
        World w = l.getWorld();
        int y = l.getBlockY() + 2;
        for (int i = 0; i < 7; i++) {
            Block b = w.getBlockAt(l.getBlockX(), y - i, l.getBlockZ());
            if (!b.isPassable()) return new Location(w, l.getX(), b.getY() + 1.0, l.getZ(), l.getYaw(), l.getPitch());
        }
        return l.clone();
    }

    /** Position sûre (pieds et tête libres) autour de la cible d'un rayon, ou null. */
    private Location safeSpot(Location eyeTarget, float yaw, float pitch) {
        Location base = eyeTarget.clone().subtract(0, 1.62, 0);
        for (int dy : new int[]{0, 1, -1, 2, -2}) {
            Location f = base.clone().add(0, dy, 0);
            f.setY(Math.floor(f.getY()));
            Block b1 = f.getBlock();
            Block b2 = f.clone().add(0, 1, 0).getBlock();
            if (b1.isPassable() && b2.isPassable() && !b1.isLiquid()) {
                f.setYaw(yaw);
                f.setPitch(pitch);
                return f;
            }
        }
        return null;
    }

    private Location rayDest(Player p, double range) {
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection();
        RayTraceResult r = p.getWorld().rayTraceBlocks(eye, dir, range, FluidCollisionMode.NEVER, true);
        double dist = range;
        if (r != null) dist = Math.max(0, eye.toVector().distance(r.getHitPosition()) - 0.8);
        Location t = eye.clone().add(dir.clone().multiply(dist));
        return safeSpot(t, p.getLocation().getYaw(), p.getLocation().getPitch());
    }

    private void expandingRing(Location c, double maxR, int ticks, String particle, Color dust) {
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                t++;
                double r = maxR * t / ticks;
                int pts = (int) Math.max(12, r * 8);
                if (dust != null) Fx.ringDust(c, r, pts, dust, 1.5f);
                if (particle != null) Fx.ring(c, r, pts, particle);
                if (t >= ticks) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void freeze(LivingEntity e, double seconds, String particle) {
        long until = System.currentTimeMillis() + (long) (seconds * 1000);
        Frozen f = frozen.get(e.getUniqueId());
        if (f != null) {
            f.until = Math.max(f.until, until);
            return;
        }
        f = new Frozen();
        f.until = until;
        f.particle = particle;
        if (e instanceof Mob m) {
            f.ai = m.hasAI();
            m.setAI(false);
            m.setTarget(null);
        }
        f.gravity = e.hasGravity();
        e.setVelocity(new Vector(0, 0, 0));
        frozen.put(e.getUniqueId(), f);
    }

    private void freezeProjectile(Projectile pr, double seconds) {
        if (frozen.containsKey(pr.getUniqueId())) return;
        Frozen f = new Frozen();
        f.projectile = true;
        f.until = System.currentTimeMillis() + (long) (seconds * 1000);
        f.vel = pr.getVelocity();
        f.gravity = pr.hasGravity();
        f.particle = "end_rod";
        pr.setGravity(false);
        pr.setVelocity(new Vector(0, 0, 0));
        frozen.put(pr.getUniqueId(), f);
    }

    // ------------------------------------------------------------------ Pouvoirs : AERON

    private boolean dash(Player p, Power pw, int kind) {
        Vector v = p.getLocation().getDirection().normalize().multiply(pw.strength());
        if (v.getY() < 0.2) v.setY(0.2);
        p.setVelocity(v);
        noFall.put(p.getUniqueId(), System.currentTimeMillis() + 6000);
        Fx.sound(p.getLocation(), pw.sound(), 1f, 1.2f);
        if (kind == 2) Fx.sound(p.getLocation(), "entity.lightning_bolt.thunder", 0.5f, 1.8f);
        final Set<UUID> hit = new HashSet<>();
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || t++ >= 10) { cancel(); return; }
                Location l = p.getLocation().add(0, 0.9, 0);
                Fx.spawn(l, pw.particle(), 8, 0.25, 0.4, 0.25, 0.02);
                if (kind == 1) {
                    Fx.spawn(l, "flame", 8, 0.3, 0.4, 0.3, 0.03);
                    Fx.spawn(l, "lava", 1, 0.2, 0.2, 0.2, 0);
                }
                if (kind == 2) Fx.spawn(l, "electric_spark", 10, 0.4, 0.5, 0.4, 0.1);
                for (LivingEntity e : enemies(p, l, Math.max(1.5, pw.range()))) {
                    if (!hit.add(e.getUniqueId())) continue;
                    if (kind == 1) e.setFireTicks((int) (pw.duration() * 20));
                    else if (kind == 2) potion(e, PotionEffectType.SLOWNESS, 2, 2);
                    else e.setVelocity(away(p.getLocation(), e.getLocation(), 0.9, 0.35));
                    damage(p, e, pw.damage());
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private boolean airJump(Player p, Power pw) {
        Vector v = p.getVelocity();
        Vector f = flat(p).multiply(0.25);
        p.setVelocity(new Vector(v.getX() * 0.5 + f.getX(), pw.strength(), v.getZ() * 0.5 + f.getZ()));
        noFall.put(p.getUniqueId(), System.currentTimeMillis() + 6000);
        Location l = p.getLocation().add(0, 0.1, 0);
        Fx.ring(l, 1.0, 16, pw.particle());
        Fx.spawn(l, "cloud", 12, 0.4, 0.1, 0.4, 0.05);
        Fx.sound(l, pw.sound(), 1f, 1.5f);
        return true;
    }

    private boolean tornado(Player p, Power pw) {
        Location c = p.getLocation();
        for (LivingEntity e : enemies(p, c, pw.range())) {
            e.setVelocity(away(c, e.getLocation(), pw.strength(), 0.7));
            damage(p, e, pw.damage());
        }
        Fx.sound(c, pw.sound(), 1.2f, 0.6f);
        final int ticks = (int) (pw.duration() * 20);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || t >= ticks) { cancel(); return; }
                Location base = p.getLocation();
                for (int i = 0; i < 3; i++) {
                    double h = ((t * 0.3) + i * 0.8) % 3.0;
                    double a = t * 0.6 + i * 2.1 + h * 2;
                    double r = 0.6 + h * 0.6;
                    Location l = base.clone().add(Math.cos(a) * r, h, Math.sin(a) * r);
                    Fx.spawn(l, pw.particle(), 2, 0.05, 0.05, 0.05, 0.01);
                    Fx.spawn(l, "poof", 1, 0, 0, 0, 0.01);
                }
                if (t % 5 == 0) {
                    for (LivingEntity e : enemies(p, base, pw.range() * 0.6)) e.setVelocity(away(base, e.getLocation(), 0.55, 0.25));
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    // ------------------------------------------------------------------ IGNIS

    private boolean flameBurst(Player p, Power pw) {
        Location c = p.getLocation().add(0, 0.3, 0);
        expandingRing(c, pw.range(), 8, "flame", null);
        expandingRing(c.clone().add(0, 0.6, 0), pw.range() * 0.7, 8, "lava", null);
        for (LivingEntity e : enemies(p, c, pw.range())) {
            e.setFireTicks((int) (pw.duration() * 20));
            e.setVelocity(away(c, e.getLocation(), pw.strength(), 0.4));
            damage(p, e, pw.damage());
        }
        Fx.spawn(c.clone().add(0, 1, 0), "explosion", 1, 0, 0, 0, 0);
        Fx.sound(c, "entity.generic.explode", 0.8f, 1.2f);
        Fx.sound(c, pw.sound(), 1.2f, 0.7f);
        return true;
    }

    private boolean fireResistance(Player p, Power pw) {
        potion(p, PotionEffectType.FIRE_RESISTANCE, pw.duration(), 0);
        p.setFireTicks(0);
        Fx.ring(p.getLocation().add(0, 0.2, 0), 1.2, 20, "flame");
        Fx.spawn(p.getLocation().add(0, 1, 0), "flame", 30, 0.4, 0.8, 0.4, 0.05);
        Fx.sound(p.getLocation(), "item.firecharge.use", 1f, 0.8f);
        return true;
    }

    // ------------------------------------------------------------------ VOLTAR

    private boolean lightning(Player p, Power pw) {
        LivingEntity t = findTarget(p, pw.range());
        if (t == null) {
            p.sendActionBar(Txt.c("&cAucune cible en vue."));
            return false;
        }
        Location l = t.getLocation();
        l.getWorld().strikeLightningEffect(l);
        Fx.spawn(l.clone().add(0, 1, 0), "electric_spark", 40, 0.5, 1, 0.5, 0.3);
        damage(p, t, pw.damage());
        potion(t, PotionEffectType.SLOWNESS, 1.5, 1);
        int chains = 0;
        for (LivingEntity e : enemies(p, l, 6)) {
            if (e.equals(t) || chains++ >= 2) continue;
            e.getWorld().strikeLightningEffect(e.getLocation());
            Fx.spawn(e.getLocation().add(0, 1, 0), "electric_spark", 25, 0.4, 0.8, 0.4, 0.2);
            damage(p, e, pw.damage() * 0.5);
        }
        return true;
    }

    private boolean electricField(Player p, Power pw) {
        final int ticks = (int) (pw.duration() * 20);
        Fx.sound(p.getLocation(), "block.beacon.activate", 1f, 1.8f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || t >= ticks) { cancel(); return; }
                Location c = p.getLocation().add(0, 0.1, 0);
                if (t % 2 == 0) {
                    Fx.ring(c, pw.range(), 24, "electric_spark");
                    Fx.spawn(c.clone().add(rr(-pw.range(), pw.range()), rr(0, 2), rr(-pw.range(), pw.range())), "electric_spark", 3, 0.1, 0.1, 0.1, 0.05);
                }
                if (t % 20 == 0) {
                    Fx.sound(c, "entity.lightning_bolt.impact", 0.3f, 2f);
                    for (LivingEntity e : enemies(p, c, pw.range())) {
                        damage(p, e, pw.damage());
                        potion(e, PotionEffectType.SLOWNESS, 1.5, 1);
                        Fx.spawn(e.getLocation().add(0, 1, 0), "electric_spark", 10, 0.3, 0.5, 0.3, 0.2);
                    }
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    // ------------------------------------------------------------------ CHRONOS

    private boolean timeStop(Player p, Power pw) {
        Location c = p.getLocation();
        int max = plugin.getConfig().getInt("powers-settings.max-frozen", 40);
        int n = 0;
        for (LivingEntity e : enemies(p, c, pw.range())) {
            if (n++ >= max) break;
            freeze(e, pw.duration(), "end_rod");
        }
        for (Entity e : c.getWorld().getNearbyEntities(c, pw.range(), pw.range(), pw.range())) {
            if (e instanceof Projectile pr && !p.equals(pr.getShooter()) && n++ < max) freezeProjectile(pr, pw.duration());
        }
        Fx.sound(c, "block.bell.use", 2f, 0.5f);
        Fx.sound(c, pw.sound(), 1.5f, 0.5f);
        final int ticks = (int) Math.max(20, pw.duration() * 20);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) { cancel(); return; }
                double a1 = -t * 0.2, a2 = -t * 0.025;
                for (double r = 0.5; r < pw.range(); r += 0.6) {
                    Fx.spawn(c.clone().add(Math.cos(a1) * r, 0.2, Math.sin(a1) * r), "end_rod", 1, 0, 0, 0, 0);
                    if (r < pw.range() * 0.6) Fx.spawn(c.clone().add(Math.cos(a2) * r, 0.2, Math.sin(a2) * r), "end_rod", 1, 0, 0, 0, 0);
                }
                if (t % 10 == 0) Fx.ring(c.clone().add(0, 0.1, 0), pw.range(), 36, "enchant");
                if (t % 20 == 0) Fx.sound(c, "block.note_block.hat", 1f, 0.6f);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private boolean slowEnemies(Player p, Power pw) {
        Location c = p.getLocation();
        for (LivingEntity e : enemies(p, c, pw.range())) {
            potion(e, PotionEffectType.SLOWNESS, pw.duration(), (int) pw.strength());
            potion(e, PotionEffectType.MINING_FATIGUE, pw.duration(), 1);
            applyDebuffs(p, pw, e);
            Fx.spawn(e.getLocation().add(0, 1, 0), "end_rod", 10, 0.3, 0.5, 0.3, 0.02);
        }
        expandingRing(c.clone().add(0, 0.2, 0), pw.range(), 10, "end_rod", Color.fromRGB(230, 200, 90));
        Fx.sound(c, pw.sound(), 1.2f, 0.5f);
        return true;
    }

    private boolean hasteSelf(Player p, Power pw) {
        potion(p, PotionEffectType.SPEED, pw.duration(), (int) pw.strength());
        potion(p, PotionEffectType.HASTE, pw.duration(), 1);
        Fx.sound(p.getLocation(), "block.beacon.power_select", 1f, 1.8f);
        final int ticks = (int) (pw.duration() * 20);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || t >= ticks) { cancel(); return; }
                Location l = p.getLocation().add(0, 1, 0);
                Fx.dust(l, Color.fromRGB(230, 200, 90), 1.4f, 3, 0.3);
                Fx.spawn(l, "end_rod", 1, 0.2, 0.4, 0.2, 0);
                t += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }

    // ------------------------------------------------------------------ UMBRA

    private boolean invisibility(Player p, Power pw) {
        potionHidden(p, PotionEffectType.INVISIBILITY, pw.duration(), 0);
        Fx.spawn(p.getLocation().add(0, 1, 0), "squid_ink", 30, 0.4, 0.8, 0.4, 0.02);
        Fx.spawn(p.getLocation().add(0, 1, 0), "smoke", 20, 0.4, 0.8, 0.4, 0.02);
        Fx.sound(p.getLocation(), "entity.enderman.teleport", 0.8f, 0.5f);
        return true;
    }

    private boolean shortTeleport(Player p, Power pw) {
        Location dest = rayDest(p, pw.range());
        if (dest == null) {
            p.sendActionBar(Txt.c("&cDestination bloquée."));
            return false;
        }
        Location from = p.getLocation();
        Fx.spawn(from.clone().add(0, 1, 0), "squid_ink", 25, 0.3, 0.6, 0.3, 0.05);
        p.teleport(dest);
        noFall.put(p.getUniqueId(), System.currentTimeMillis() + 4000);
        Fx.spawn(dest.clone().add(0, 1, 0), "squid_ink", 25, 0.3, 0.6, 0.3, 0.05);
        Fx.spawn(dest.clone().add(0, 1, 0), "smoke", 15, 0.3, 0.6, 0.3, 0.02);
        Fx.sound(from, pw.sound(), 1f, 0.7f);
        Fx.sound(dest, pw.sound(), 1f, 0.7f);
        return true;
    }

    private boolean darkZone(Player p, Power pw) {
        final Location c = p.getLocation();
        final int ticks = (int) (pw.duration() * 20);
        Fx.sound(c, "entity.warden.sonic_charge", 1f, 0.6f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) { cancel(); return; }
                for (int i = 0; i < 14; i++) {
                    double a = rr(0, 2 * Math.PI), r = Math.sqrt(rnd.nextDouble()) * pw.range();
                    Location l = c.clone().add(Math.cos(a) * r, rr(0, 3), Math.sin(a) * r);
                    Fx.spawn(l, "squid_ink", 1, 0, 0, 0, 0);
                    Fx.spawn(l, "smoke", 1, 0.1, 0.1, 0.1, 0.01);
                }
                Fx.ring(c.clone().add(0, 0.1, 0), pw.range(), 30, "large_smoke");
                if (t % 10 == 0) {
                    for (LivingEntity e : enemies(p, c, pw.range())) {
                        potion(e, PotionEffectType.DARKNESS, 3, 0);
                        potion(e, PotionEffectType.BLINDNESS, 1.5, 0);
                        applyDebuffs(p, pw, e);
                    }
                    if (p.isOnline() && p.getLocation().distanceSquared(c) <= pw.range() * pw.range()) {
                        potionHidden(p, PotionEffectType.SPEED, 2, 1);
                    }
                }
                t += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }

    // ------------------------------------------------------------------ SANGUIS

    private boolean lifestealMode(Player p, Power pw) {
        lifesteal.put(p.getUniqueId(), System.currentTimeMillis() + (long) (pw.duration() * 1000));
        Fx.ringDust(p.getLocation().add(0, 0.2, 0), 1.3, 24, Color.fromRGB(170, 0, 20), 1.6f);
        Fx.spawn(p.getLocation().add(0, 1, 0), "crimson_spore", 30, 0.4, 0.8, 0.4, 0);
        Fx.sound(p.getLocation(), "entity.witch.celebrate", 1f, 0.6f);
        return true;
    }

    private boolean shockwave(Player p, Power pw) {
        Location c = p.getLocation().add(0, 0.3, 0);
        expandingRing(c, pw.range(), 8, "damage_indicator", Color.fromRGB(170, 0, 20));
        for (LivingEntity e : enemies(p, c, pw.range())) {
            e.setVelocity(away(c, e.getLocation(), pw.strength(), 0.45));
            damage(p, e, pw.damage());
            potion(e, PotionEffectType.SLOWNESS, 2, 1);
        }
        Fx.sound(c, "entity.warden.sonic_boom", 0.8f, 1.4f);
        Fx.sound(c, pw.sound(), 1f, 0.6f);
        return true;
    }

    private boolean regen(Player p, Power pw) {
        potion(p, PotionEffectType.REGENERATION, pw.duration(), (int) pw.strength());
        Fx.spawn(p.getLocation().add(0, 1, 0), "heart", 8, 0.4, 0.6, 0.4, 0);
        Fx.ringDust(p.getLocation().add(0, 0.2, 0), 1.0, 20, Color.fromRGB(170, 0, 20), 1.4f);
        p.removePotionEffect(PotionEffectType.POISON);
        p.removePotionEffect(PotionEffectType.WITHER);
        p.setFireTicks(0);
        Fx.sound(p.getLocation(), "entity.generic.drink", 1f, 0.6f);
        return true;
    }

    // ------------------------------------------------------------------ TERRAX

    private boolean seismic(Player p, Power pw) {
        final Location start = p.getLocation();
        final Vector dir = flat(p);
        final Set<UUID> hit = new HashSet<>();
        Fx.sound(start, "entity.generic.explode", 0.7f, 0.5f);
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                step++;
                if (step > pw.range() || !p.isOnline()) { cancel(); return; }
                Location l = ground(start.clone().add(dir.clone().multiply(step)));
                Material below = l.clone().subtract(0, 1, 0).getBlock().getType();
                Fx.block(l.clone().add(0, 0.3, 0), below.isBlock() ? below : Material.STONE, 25, 0.5, 0.4, 0.5);
                Fx.spawn(l.clone().add(0, 0.5, 0), "poof", 4, 0.4, 0.2, 0.4, 0.02);
                Fx.sound(l, "block.stone.break", 0.7f, 0.6f);
                for (LivingEntity e : enemies(p, l, 1.8)) {
                    if (!hit.add(e.getUniqueId())) continue;
                    e.setVelocity(new Vector(0, 0.55 + pw.strength() * 0.5, 0));
                    damage(p, e, pw.damage());
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private String wallKey(Block b) {
        return b.getWorld().getName() + "," + b.getX() + "," + b.getY() + "," + b.getZ();
    }

    private boolean stoneWall(Player p, Power pw) {
        Vector dir = flat(p);
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        Location mid = p.getLocation().add(dir.clone().multiply(3));
        int half = Math.max(1, (int) pw.range() / 2);
        List<Block> placed = new ArrayList<>();
        for (int i = -half; i <= half; i++) {
            for (int h = 0; h < 3; h++) {
                Location l = mid.clone().add(side.clone().multiply(i)).add(0, h, 0);
                Block b = l.getBlock();
                if (!b.getType().isAir()) continue;
                if (!b.getWorld().getNearbyEntities(BoundingBox.of(b), e -> e instanceof LivingEntity).isEmpty()) continue;
                b.setType(wallMaterial, false);
                placed.add(b);
                walls.add(wallKey(b) + "," + wallMaterial.name());
                Fx.block(b.getLocation().add(0.5, 0.5, 0.5), wallMaterial, 10, 0.3, 0.3, 0.3);
            }
        }
        if (placed.isEmpty()) {
            p.sendActionBar(Txt.c("&cPas de place pour le mur."));
            return false;
        }
        saveWalls();
        Fx.sound(mid, "block.stone.place", 1.5f, 0.5f);
        Fx.sound(mid, "entity.iron_golem.repair", 1f, 0.6f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Block b : placed) {
                String key = wallKey(b) + "," + wallMaterial.name();
                if (walls.remove(key) && b.getType() == wallMaterial) {
                    Fx.block(b.getLocation().add(0.5, 0.5, 0.5), wallMaterial, 12, 0.3, 0.3, 0.3);
                    b.setType(Material.AIR, false);
                }
            }
            saveWalls();
        }, (long) (pw.duration() * 20));
        return true;
    }

    private File wallFile() { return new File(plugin.getDataFolder(), "walls.yml"); }

    private void saveWalls() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("walls", new ArrayList<>(walls));
        try {
            y.save(wallFile());
        } catch (IOException e) {
            plugin.getLogger().warning("walls.yml : " + e.getMessage());
        }
    }

    private void restoreLeftoverWalls() {
        if (!wallFile().exists()) return;
        walls.addAll(YamlConfiguration.loadConfiguration(wallFile()).getStringList("walls"));
        restoreAllWalls();
    }

    private void restoreAllWalls() {
        for (String key : new ArrayList<>(walls)) {
            try {
                String[] p = key.split(",");
                World w = Bukkit.getWorld(p[0]);
                if (w == null) continue;
                Block b = w.getBlockAt(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                if (b.getType().name().equals(p[4])) b.setType(Material.AIR, false);
                walls.remove(key);
            } catch (Exception ignored) {
                walls.remove(key);
            }
        }
        saveWalls();
    }

    private boolean damageResist(Player p, Power pw) {
        potion(p, PotionEffectType.RESISTANCE, pw.duration(), (int) pw.strength());
        Fx.ringDust(p.getLocation().add(0, 0.2, 0), 1.2, 20, Color.fromRGB(120, 85, 50), 1.6f);
        Fx.block(p.getLocation().add(0, 1, 0), Material.STONE, 30, 0.4, 0.8, 0.4);
        Fx.sound(p.getLocation(), "block.stone.place", 1f, 0.5f);
        return true;
    }

    // ------------------------------------------------------------------ AQUARIS

    private boolean waterPrison(Player p, Power pw) {
        LivingEntity t = findTarget(p, pw.range());
        if (t == null) {
            p.sendActionBar(Txt.c("&cAucune cible en vue."));
            return false;
        }
        freeze(t, pw.duration(), "bubble_pop");
        Location tl = t.getLocation();
        List<Block> shell = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean outer = Math.abs(dx) == 1 || Math.abs(dz) == 1 || dy == -1 || dy == 3;
                    Block b = tl.getBlock().getRelative(dx, dy, dz);
                    if (outer && b.getType().isAir()) shell.add(b);
                }
            }
        }
        List<Player> viewers = new ArrayList<>();
        for (Player v : tl.getWorld().getPlayers()) if (v.getLocation().distanceSquared(tl) <= 1600) viewers.add(v);
        var water = Material.WATER.createBlockData();
        for (Block b : shell) for (Player v : viewers) v.sendBlockChange(b.getLocation(), water);
        Fx.sound(tl, "entity.player.splash.high_speed", 1.2f, 0.7f);
        Fx.spawn(tl.clone().add(0, 1, 0), "splash", 60, 0.8, 1, 0.8, 0.1);
        final int ticks = (int) (pw.duration() * 20);
        new BukkitRunnable() {
            int n = 0;

            @Override
            public void run() {
                n += 20;
                if (t.isValid() && n < ticks) damage(p, t, pw.damage());
                if (n >= ticks || !t.isValid()) {
                    for (Block b : shell) {
                        var real = b.getBlockData();
                        for (Player v : viewers) if (v.isOnline()) v.sendBlockChange(b.getLocation(), real);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        return true;
    }

    private boolean wave(Player p, Power pw) {
        final Location start = p.getLocation().add(0, 0.5, 0);
        final Vector dir = flat(p);
        final Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        for (LivingEntity e : enemies(p, start, pw.range())) {
            Vector to = e.getLocation().toVector().subtract(start.toVector()).setY(0);
            if (to.lengthSquared() < 1e-4 || to.normalize().dot(dir) < 0.35) continue;
            e.setVelocity(dir.clone().multiply(pw.strength()).setY(0.4));
            damage(p, e, pw.damage());
        }
        Fx.sound(start, "entity.player.splash.high_speed", 1.5f, 0.6f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                t++;
                double d = t * 1.3;
                if (d > pw.range()) { cancel(); return; }
                double w = d * 0.6;
                for (double s = -w; s <= w; s += 0.5) {
                    Location l = start.clone().add(dir.clone().multiply(d)).add(side.clone().multiply(s));
                    Fx.spawn(l, "splash", 3, 0.1, 0.4, 0.1, 0.1);
                    Fx.spawn(l, "bubble_pop", 1, 0.1, 0.3, 0.1, 0.02);
                    Fx.spawn(l, "nautilus", 1, 0.1, 0.3, 0.1, 0.2);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    // ------------------------------------------------------------------ NOX

    private boolean summonServants(Player p, Power pw) {
        int max = plugin.getConfig().getInt("powers-settings.max-servants", 8);
        int mine = 0;
        for (UUID o : servants.values()) if (o.equals(p.getUniqueId())) mine++;
        int count = Math.min((int) pw.strength(), max - mine);
        if (count <= 0) {
            p.sendActionBar(Txt.c("&cVos serviteurs sont déjà au maximum."));
            return false;
        }
        Location c = p.getLocation();
        for (int i = 0; i < count; i++) {
            double a = 2 * Math.PI * i / count;
            Location s = ground(c.clone().add(Math.cos(a) * 2, 0, Math.sin(a) * 2));
            Zombie z = c.getWorld().spawn(s, Zombie.class, e -> {
                e.setBaby(false);
                e.customName(Txt.c("&8Serviteur de &7" + p.getName()));
                e.setCustomNameVisible(true);
                e.setCanPickupItems(false);
                e.setPersistent(false);
                e.setRemoveWhenFarAway(true);
                e.getPersistentDataContainer().set(tempKey, PersistentDataType.BYTE, (byte) 1);
                e.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, p.getUniqueId().toString());
                EntityEquipment eq = e.getEquipment();
                eq.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                eq.setItemInMainHandDropChance(0f);
                eq.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                eq.setHelmetDropChance(0f);
                e.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
                        (int) (pw.duration() * 20) + 100, 0, false, false));
                e.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, (int) (pw.duration() * 20), 0, false, false));
                e.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (pw.duration() * 20), 0, false, false));
            });
            servants.put(z.getUniqueId(), p.getUniqueId());
            Fx.spawn(s.clone().add(0, 1, 0), "soul", 15, 0.3, 0.6, 0.3, 0.03);
            Fx.spawn(s.clone().add(0, 0.2, 0), "soul_fire_flame", 10, 0.3, 0.1, 0.3, 0.02);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                servants.remove(z.getUniqueId());
                if (z.isValid()) {
                    Fx.spawn(z.getLocation().add(0, 1, 0), "poof", 15, 0.3, 0.6, 0.3, 0.03);
                    z.remove();
                }
            }, (long) (pw.duration() * 20));
        }
        Fx.sound(c, "entity.wither.spawn", 0.6f, 1.5f);
        Fx.sound(c, "entity.evoker.prepare_summon", 1f, 0.6f);
        return true;
    }

    private void servantAi() {
        for (Map.Entry<UUID, UUID> en : new ArrayList<>(servants.entrySet())) {
            Entity e = Bukkit.getEntity(en.getKey());
            Player owner = Bukkit.getPlayer(en.getValue());
            if (!(e instanceof Zombie z) || !z.isValid() || owner == null) {
                if (e != null && owner == null) e.remove();
                if (e == null || !e.isValid() || owner == null) servants.remove(en.getKey());
                continue;
            }
            LivingEntity cur = z.getTarget();
            if (cur == null || !cur.isValid() || cur.equals(owner) || servants.containsKey(cur.getUniqueId())) {
                LivingEntity best = enemies(owner, z.getLocation(), 16).stream()
                        .min(Comparator.comparingDouble(x -> x.getLocation().distanceSquared(z.getLocation()))).orElse(null);
                z.setTarget(best);
                if (best == null && z.getLocation().distanceSquared(owner.getLocation()) > 64) {
                    z.getPathfinder().moveTo(owner.getLocation(), 1.2);
                }
            }
        }
    }

    private boolean fearAura(Player p, Power pw) {
        final int ticks = (int) (pw.duration() * 20);
        Fx.sound(p.getLocation(), "entity.ghast.scream", 1f, 0.5f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || t >= ticks) { cancel(); return; }
                Location c = p.getLocation().add(0, 0.2, 0);
                Fx.ring(c, pw.range(), 30, "soul");
                Fx.spawn(c.clone().add(0, 1, 0), "sculk_soul", 3, 0.5, 0.8, 0.5, 0.02);
                for (LivingEntity e : enemies(p, c, pw.range())) {
                    Vector a = away(c, e.getLocation(), 0.5, 0.1);
                    if (e instanceof Player) {
                        e.setVelocity(a);
                        potion(e, PotionEffectType.DARKNESS, 2, 0);
                    } else if (e instanceof Mob m) {
                        m.setTarget(null);
                        m.getPathfinder().moveTo(e.getLocation().add(a.clone().setY(0).normalize().multiply(8)), 1.5);
                    }
                    potion(e, PotionEffectType.WEAKNESS, 2, 0);
                }
                t += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
        return true;
    }

    private boolean curse(Player p, Power pw) {
        LivingEntity t = findTarget(p, pw.range());
        if (t == null) {
            p.sendActionBar(Txt.c("&cAucune cible en vue."));
            return false;
        }
        potion(t, PotionEffectType.WITHER, pw.duration(), 0);
        potion(t, PotionEffectType.WEAKNESS, pw.duration(), 1);
        potion(t, PotionEffectType.SLOWNESS, pw.duration(), 0);
        potion(t, PotionEffectType.GLOWING, pw.duration(), 0);
        applyDebuffs(p, pw, t);
        double drained = Math.min(4, plugin.pacts().currentMax(p) - p.getHealth());
        if (drained > 0) {
            p.setHealth(p.getHealth() + drained);
            Fx.spawn(p.getLocation().add(0, 1.2, 0), "heart", 4, 0.3, 0.4, 0.3, 0);
        }
        Fx.sound(t.getLocation(), "entity.wither.hurt", 1f, 0.5f);
        final int ticks = (int) (pw.duration() * 20);
        new BukkitRunnable() {
            int n = 0;

            @Override
            public void run() {
                if (!t.isValid() || n >= ticks) { cancel(); return; }
                Location l = t.getLocation().add(0, 1, 0);
                Fx.spawn(l, "soul", 3, 0.4, 0.6, 0.4, 0.02);
                Fx.spawn(l, "smoke", 3, 0.4, 0.6, 0.4, 0.01);
                n += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
        return true;
    }

    // ------------------------------------------------------------------ PHANTOM

    private ArmorStand spawnClone(Player p, Location loc, double seconds) {
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class, a -> {
            a.setArms(true);
            a.setBasePlate(false);
            a.setSilent(true);
            a.setCanPickupItems(false);
            a.customName(Txt.c("&f" + p.getName()));
            a.setCustomNameVisible(true);
            a.setRotation(p.getLocation().getYaw(), 0);
            a.getPersistentDataContainer().set(tempKey, PersistentDataType.BYTE, (byte) 1);
            a.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, p.getUniqueId().toString());
            EntityEquipment eq = a.getEquipment();
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            sm.setOwningPlayer(p);
            skull.setItemMeta(sm);
            eq.setHelmet(skull);
            ItemStack c = p.getInventory().getChestplate(), l = p.getInventory().getLeggings(), b = p.getInventory().getBoots();
            eq.setChestplate(c == null ? null : c.clone());
            eq.setLeggings(l == null ? null : l.clone());
            eq.setBoots(b == null ? null : b.clone());
            Material hand = p.getInventory().getItemInMainHand().getType();
            if (!hand.isAir() && plugin.items().read(p.getInventory().getItemInMainHand()) == null) eq.setItemInMainHand(new ItemStack(hand));
            eq.setHelmetDropChance(0f);
            eq.setChestplateDropChance(0f);
            eq.setLeggingsDropChance(0f);
            eq.setBootsDropChance(0f);
            eq.setItemInMainHandDropChance(0f);
            a.setDisabledSlots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
        });
        clones.add(as.getUniqueId());
        Fx.spawn(loc.clone().add(0, 1, 0), "poof", 12, 0.3, 0.6, 0.3, 0.03);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            clones.remove(as.getUniqueId());
            if (as.isValid()) {
                Fx.spawn(as.getLocation().add(0, 1, 0), "poof", 12, 0.3, 0.6, 0.3, 0.03);
                as.remove();
            }
        }, (long) (seconds * 20));
        return as;
    }

    private boolean cloneSpell(Player p, Power pw) {
        int count = Math.max(1, Math.min(6, (int) pw.strength()));
        Location c = p.getLocation();
        for (int i = 0; i < count; i++) {
            double a = 2 * Math.PI * i / count + rr(-0.3, 0.3);
            spawnClone(p, ground(c.clone().add(Math.cos(a) * 2.5, 0, Math.sin(a) * 2.5)), pw.duration());
        }
        Fx.sound(c, pw.sound(), 1f, 1.4f);
        Fx.ring(c.clone().add(0, 0.2, 0), 2.5, 24, "witch");
        return true;
    }

    private boolean phantomTeleport(Player p, Power pw) {
        Location from = p.getLocation();
        for (int attempt = 0; attempt < 12; attempt++) {
            double a = rr(0, 2 * Math.PI), d = rr(4, Math.max(5, pw.range()));
            Location g = ground(from.clone().add(Math.cos(a) * d, 0, Math.sin(a) * d));
            Block b1 = g.getBlock(), b2 = g.clone().add(0, 1, 0).getBlock();
            if (!b1.isPassable() || !b2.isPassable() || b1.isLiquid()) continue;
            g.setX(Math.floor(g.getX()) + 0.5);
            g.setZ(Math.floor(g.getZ()) + 0.5);
            g.setYaw(from.getYaw());
            g.setPitch(from.getPitch());
            spawnClone(p, from.clone(), pw.duration());
            Fx.spawn(from.clone().add(0, 1, 0), "witch", 30, 0.4, 0.8, 0.4, 0.1);
            p.teleport(g);
            noFall.put(p.getUniqueId(), System.currentTimeMillis() + 4000);
            Fx.spawn(g.clone().add(0, 1, 0), "witch", 30, 0.4, 0.8, 0.4, 0.1);
            Fx.spawn(g.clone().add(0, 1, 0), "end_rod", 15, 0.3, 0.6, 0.3, 0.05);
            Fx.sound(from, pw.sound(), 1f, 1.5f);
            Fx.sound(g, "entity.enderman.teleport", 1f, 1.5f);
            return true;
        }
        p.sendActionBar(Txt.c("&cAucun endroit libre."));
        return false;
    }

    private boolean confusion(Player p, Power pw) {
        final int ticks = (int) (pw.duration() * 20);
        Fx.sound(p.getLocation(), pw.sound(), 1.2f, 1.6f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || t >= ticks) { cancel(); return; }
                Location c = p.getLocation();
                Fx.ring(c.clone().add(0, 0.2, 0), pw.range(), 30, "witch");
                for (LivingEntity e : enemies(p, c, pw.range())) {
                    potion(e, PotionEffectType.NAUSEA, 6, 0);
                    applyDebuffs(p, pw, e);
                    potion(e, PotionEffectType.SLOWNESS, 1.5, 0);
                    Fx.spawn(e.getLocation().add(0, 2, 0), "end_rod", 3, 0.3, 0.1, 0.3, 0.02);
                    if (e instanceof Player t2) {
                        t2.setRotation(t2.getLocation().getYaw() + (float) rr(-90, 90), t2.getLocation().getPitch());
                    } else if (e instanceof Mob m) {
                        m.setTarget(null);
                        m.getPathfinder().moveTo(e.getLocation().add(rr(-5, 5), 0, rr(-5, 5)), 1.0);
                    }
                }
                t += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
        return true;
    }

    // ------------------------------------------------------------------ GRAVITAS

    private boolean levitateZone(Player p, Power pw) {
        Location c = p.getLocation();
        for (LivingEntity e : enemies(p, c, pw.range())) {
            potion(e, PotionEffectType.LEVITATION, pw.duration(), (int) pw.strength());
            applyDebuffs(p, pw, e);
            Fx.spawn(e.getLocation(), "reverse_portal", 20, 0.3, 0.1, 0.3, 0.1);
        }
        expandingRing(c.clone().add(0, 0.2, 0), pw.range(), 10, "reverse_portal", Element.GRAVITY.color);
        Fx.sound(c, "block.beacon.power_select", 1.5f, 0.5f);
        return true;
    }

    private boolean gravityCrush(Player p, Power pw) {
        Location c = p.getLocation();
        for (LivingEntity e : enemies(p, c, pw.range())) {
            double extra = e.isOnGround() ? 1.0 : 1.3;
            e.setVelocity(new Vector(0, -pw.strength(), 0));
            damage(p, e, pw.damage() * extra);
            potion(e, PotionEffectType.SLOWNESS, 2, 3);
            Fx.block(e.getLocation().add(0, 0.2, 0), Material.DEEPSLATE, 25, 0.5, 0.2, 0.5);
        }
        expandingRing(c.clone().add(0, 0.2, 0), pw.range(), 6, "crit", Element.GRAVITY.color);
        Fx.spawn(c.clone().add(0, 0.5, 0), "explosion", 1, 0, 0, 0, 0);
        Fx.sound(c, "block.anvil.land", 1.5f, 0.5f);
        Fx.sound(c, "entity.iron_golem.damage", 1.5f, 0.5f);
        return true;
    }

    private boolean lowGravity(Player p, Power pw) {
        potion(p, PotionEffectType.SLOW_FALLING, pw.duration(), 0);
        potion(p, PotionEffectType.JUMP_BOOST, pw.duration(), (int) pw.strength());
        noFall.put(p.getUniqueId(), System.currentTimeMillis() + (long) (pw.duration() * 1000) + 3000);
        Fx.ring(p.getLocation().add(0, 0.2, 0), 1.2, 20, "reverse_portal");
        Fx.sound(p.getLocation(), "block.beacon.power_select", 1f, 1.5f);
        return true;
    }

    // ------------------------------------------------------------------ VOID

    private boolean rift(Player p, Power pw) {
        Location dest = rayDest(p, 8);
        final Location c = (dest == null ? p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(4))
                : dest).clone().add(0, 1.2, 0);
        final int ticks = (int) (pw.duration() * 20);
        Fx.sound(c, "entity.warden.sonic_boom", 1f, 0.6f);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) { cancel(); return; }
                for (int arm = 0; arm < 2; arm++) {
                    double a = t * 0.5 + arm * Math.PI;
                    for (double r = 0.4; r < 2.0; r += 0.5) {
                        Location l = c.clone().add(Math.cos(a + r) * r, Math.sin(a * 0.5 + r) * 0.8, Math.sin(a + r) * r);
                        Fx.spawn(l, "portal", 2, 0.05, 0.05, 0.05, 0.1);
                        Fx.dust(l, Color.fromRGB(20, 0, 30), 1.4f, 1, 0.05);
                    }
                }
                for (double y = -1.6; y <= 1.6; y += 0.4) Fx.dust(c.clone().add(rr(-0.15, 0.15), y, rr(-0.15, 0.15)), Color.fromRGB(0, 0, 0), 1.6f, 1, 0.05);
                if (t % 2 == 0) {
                    for (LivingEntity e : enemies(p, c, pw.range())) {
                        Vector pull = c.toVector().subtract(e.getLocation().add(0, 0.5, 0).toVector());
                        if (pull.lengthSquared() > 0.5) e.setVelocity(pull.normalize().multiply(pw.strength()));
                    }
                }
                if (t % 20 == 0) {
                    Fx.sound(c, "block.portal.ambient", 1f, 0.5f);
                    for (LivingEntity e : enemies(p, c, 2.5)) damage(p, e, pw.damage());
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private boolean voidProjectile(Player p, Power pw) {
        final Location pos = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(0.6));
        final Vector dir = p.getEyeLocation().getDirection().normalize();
        final double speed = Math.max(0.5, pw.strength());
        Fx.sound(pos, "entity.enderman.scream", 0.8f, 0.5f);
        new BukkitRunnable() {
            double travelled = 0;

            @Override
            public void run() {
                for (int i = 0; i < 2; i++) {
                    pos.add(dir.clone().multiply(speed / 2));
                    travelled += speed / 2;
                    Fx.dust(pos, Color.fromRGB(15, 0, 25), 2f, 3, 0.1);
                    Fx.dust(pos, Color.fromRGB(130, 30, 200), 1.2f, 2, 0.15);
                    Fx.spawn(pos, "portal", 3, 0.1, 0.1, 0.1, 0.3);
                    boolean hitEntity = false;
                    for (LivingEntity e : pos.getWorld().getNearbyLivingEntities(pos, 1.0)) {
                        if (isEnemy(p, e)) { hitEntity = true; break; }
                    }
                    if (hitEntity || !pos.getBlock().isPassable() || travelled >= pw.range() || !p.isOnline()) {
                        explode(pos.clone());
                        cancel();
                        return;
                    }
                }
            }

            private void explode(Location l) {
                Fx.spawn(l, "explosion", 1, 0, 0, 0, 0);
                Fx.spawn(l, "portal", 60, 0.8, 0.8, 0.8, 0.5);
                Fx.ringDust(l, 1.5, 24, Color.fromRGB(70, 0, 110), 1.8f);
                Fx.sound(l, "entity.generic.explode", 1f, 0.5f);
                Fx.sound(l, "entity.enderman.teleport", 1f, 0.4f);
                if (!p.isOnline()) return;
                for (LivingEntity e : enemies(p, l, 3.5)) {
                    damage(p, e, pw.damage());
                    potion(e, PotionEffectType.DARKNESS, 4, 0);
                    potion(e, PotionEffectType.SLOWNESS, 2, 1);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private boolean voidTeleport(Player p, Power pw) {
        Location dest = rayDest(p, pw.range());
        if (dest == null) {
            p.sendActionBar(Txt.c("&cZone invalide."));
            return false;
        }
        Location from = p.getLocation();
        Fx.spawn(from.clone().add(0, 1, 0), "reverse_portal", 80, 0.4, 0.9, 0.4, 0.5);
        Fx.ringDust(from.clone().add(0, 0.1, 0), 1.2, 20, Color.fromRGB(20, 0, 30), 1.8f);
        p.teleport(dest);
        noFall.put(p.getUniqueId(), System.currentTimeMillis() + 5000);
        Fx.spawn(dest.clone().add(0, 1, 0), "portal", 80, 0.4, 0.9, 0.4, 0.5);
        Fx.ringDust(dest.clone().add(0, 0.1, 0), 1.2, 20, Color.fromRGB(70, 0, 110), 1.8f);
        Fx.sound(from, pw.sound(), 1f, 0.5f);
        Fx.sound(dest, pw.sound(), 1f, 0.5f);
        return true;
    }

    // ------------------------------------------------------------------ Événements

    private Player attackerOf(Entity d) {
        if (d instanceof Player pl) return pl;
        if (d instanceof Projectile pr && pr.getShooter() instanceof Player sp) return sp;
        return null;
    }

    /** Bonus passifs : esquive, dégâts bonus, enflammer, vol de vie, foudre, effet sur coup. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPassiveHit(EntityDamageByEntityEvent e) {
        if (passives.isEmpty() || inPassive) return;
        if (e.getEntity() instanceof Player v) {
            List<ConfigurationSection> vl = passives.get(v.getUniqueId());
            if (vl != null) {
                for (ConfigurationSection sec : vl) {
                    double ch = pb(sec.getDouble("dodge-chance", 0), 0.4);
                    if (ch > 0 && rnd.nextDouble() < ch) {
                        e.setCancelled(true);
                        Fx.spawn(v.getLocation().add(0, 1, 0), "poof", 15, 0.3, 0.6, 0.3, 0.05);
                        Fx.sound(v.getLocation(), "entity.phantom.flap", 0.8f, 1.6f);
                        v.sendActionBar(Txt.c("&7Esquive !"));
                        return;
                    }
                }
            }
        }
        Player a = attackerOf(e.getDamager());
        if (a == null || !(e.getEntity() instanceof LivingEntity t)) return;
        List<ConfigurationSection> list = passives.get(a.getUniqueId());
        if (list == null) return;
        inPassive = true;
        try {
            for (ConfigurationSection sec : list) {
                double bonus = pb(sec.getDouble("bonus-damage", 0), 1.0);
                if (bonus > 0) e.setDamage(e.getDamage() * (1 + bonus));
                double ign = pb(sec.getDouble("ignite-seconds", 0), 15);
                if (ign > 0) t.setFireTicks((int) (ign * 20));
                double ls = pb(sec.getDouble("lifesteal", 0), 0.5);
                if (ls > 0 && !a.isDead()) {
                    a.setHealth(Math.min(plugin.pacts().currentMax(a), a.getHealth() + e.getDamage() * ls));
                    Fx.spawn(a.getLocation().add(0, 1.2, 0), "heart", 1, 0.3, 0.3, 0.3, 0);
                }
                double lc = pb(sec.getDouble("lightning-chance", 0), 0.5);
                if (lc > 0 && rnd.nextDouble() < lc) {
                    t.getWorld().strikeLightningEffect(t.getLocation());
                    Fx.spawn(t.getLocation().add(0, 1, 0), "electric_spark", 20, 0.3, 0.6, 0.3, 0.2);
                    e.setDamage(e.getDamage() + 3);
                }
                ConfigurationSection oh = sec.getConfigurationSection("on-hit");
                if (oh != null && rnd.nextDouble() < pb(oh.getDouble("chance", 0.2), 0.6)) {
                    PotionEffectType type = effect(oh.getString("effect", ""));
                    if (type != null) potion(t, type, oh.getDouble("seconds", 3), (int) oh.getDouble("amplifier", 0));
                }
            }
        } finally {
            inPassive = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack it = e.getItem();
        DemonItem.Info info = plugin.items().read(it);
        if (info == null) return;
        Player p = e.getPlayer();
        if (a == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null
                && e.getClickedBlock().getType().isInteractable() && !p.isSneaking()) return;
        e.setCancelled(true);
        if (frozen.containsKey(p.getUniqueId())) return;
        Pact pact = plugin.pacts().validate(p, info);
        if (pact == null) {
            p.sendActionBar(Txt.c("&4Cet objet ne vous obéit pas."));
            Fx.sound(p.getLocation(), "entity.wither.hurt", 0.4f, 0.5f);
            return;
        }
        use(p, pact);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (frozen.isEmpty() || !frozen.containsKey(e.getPlayer().getUniqueId())) return;
        Location f = e.getFrom(), t = e.getTo();
        if (t == null) return;
        if (f.getX() != t.getX() || f.getY() != t.getY() || f.getZ() != t.getZ()) {
            e.setTo(new Location(f.getWorld(), f.getX(), f.getY(), f.getZ(), t.getYaw(), t.getPitch()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (frozen.containsKey(e.getPlayer().getUniqueId())
                && (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || e.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && frozen.containsKey(p.getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && noFall.containsKey(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        Entity damager = e.getDamager();
        Player attacker = damager instanceof Player pl ? pl
                : (damager instanceof Projectile pr && pr.getShooter() instanceof Player sp ? sp : null);
        if (attacker != null && frozen.containsKey(attacker.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (victim instanceof ArmorStand as && isTemp(as)) {
            e.setCancelled(true);
            String owner = as.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
            Fx.spawn(as.getLocation().add(0, 1, 0), "poof", 20, 0.3, 0.6, 0.3, 0.05);
            Fx.sound(as.getLocation(), "entity.illusioner.mirror_move", 1f, 1.2f);
            clones.remove(as.getUniqueId());
            as.remove();
            if (attacker != null && (owner == null || !owner.equals(attacker.getUniqueId().toString()))) {
                potion(attacker, PotionEffectType.NAUSEA, 5, 0);
                potion(attacker, PotionEffectType.BLINDNESS, 1.5, 0);
            }
            return;
        }
        UUID so = servants.get(damager.getUniqueId());
        if (so != null && victim.getUniqueId().equals(so)) e.setCancelled(true);
        UUID vo = servants.get(victim.getUniqueId());
        if (vo != null && attacker != null && attacker.getUniqueId().equals(vo)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLifesteal(EntityDamageByEntityEvent e) {
        if (lifesteal.isEmpty()) return;
        Player p = e.getDamager() instanceof Player pl ? pl
                : (e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Player sp ? sp : null);
        if (p == null || !lifesteal.containsKey(p.getUniqueId()) || p.isDead()) return;
        Power pw = powers.get(PowerType.LIFESTEAL);
        if (pw == null) return;
        double heal = e.getFinalDamage() * pw.strength();
        double max = plugin.pacts().currentMax(p);
        p.setHealth(Math.min(max, p.getHealth() + heal));
        Fx.spawn(p.getLocation().add(0, 1.2, 0), "heart", 2, 0.3, 0.3, 0.3, 0);
        Fx.spawn(e.getEntity().getLocation().add(0, 1, 0), "damage_indicator", 4, 0.3, 0.4, 0.3, 0.05);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        UUID owner = servants.get(e.getEntity().getUniqueId());
        if (owner == null || e.getTarget() == null) return;
        if (e.getTarget().getUniqueId().equals(owner) || servants.containsKey(e.getTarget().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (servants.remove(e.getEntity().getUniqueId()) != null) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        for (Map.Entry<UUID, UUID> en : new ArrayList<>(servants.entrySet())) {
            if (en.getValue().equals(id)) {
                Entity s = Bukkit.getEntity(en.getKey());
                if (s != null) s.remove();
                servants.remove(en.getKey());
            }
        }
    }

    @EventHandler
    public void onStandInteract(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand as && isTemp(as)) e.setCancelled(true);
    }

    @EventHandler
    public void onStandManipulate(PlayerArmorStandManipulateEvent e) {
        if (isTemp(e.getRightClicked())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWallBreak(BlockBreakEvent e) {
        if (walls.contains(wallKey(e.getBlock()) + "," + e.getBlock().getType().name())) e.setDropItems(false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWallExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> walls.contains(wallKey(b) + "," + b.getType().name()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onWallBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(b -> walls.contains(wallKey(b) + "," + b.getType().name()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            if (walls.contains(wallKey(b) + "," + b.getType().name())) { e.setCancelled(true); return; }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            if (walls.contains(wallKey(b) + "," + b.getType().name())) { e.setCancelled(true); return; }
        }
    }
}

/**
 * Les 36 pouvoirs du plugin avec leurs valeurs par défaut.
 * Chaque valeur peut être surchargée dans config.yml (section powers).
 * Unités : cooldown/durée en secondes, portée en blocs, force/dégâts selon le pouvoir.
 */
enum PowerType {
    // AERON
    AIR_DASH("AERON", "Air Dash", "Propulsion rapide qui bouscule les ennemis sur votre trajectoire.", 8, 0, 2.5, 2.4, 2),
    AIR_JUMP("AERON", "Pieds du Vent", "Passif : sauts bien plus hauts, chutes amorties et pas plus rapides.", 0, 0, 0, 0, 0, true),
    TORNADO("AERON", "Tornade", "Une tornade repousse violemment les ennemis proches.", 25, 3, 6, 1.6, 4),
    // IGNIS
    FIRE_DASH("IGNIS", "Fire Dash", "Charge enflammée qui brûle tout sur son passage.", 10, 4, 2.5, 2.0, 4),
    FLAME_BURST("IGNIS", "Explosion de Flammes", "Une déflagration de feu autour de vous.", 20, 5, 6, 1.3, 6),
    FIRE_RESISTANCE("IGNIS", "Sang de Braise", "Passif : mêlée plus violente, brûlures quasi nulles, résistance aux explosions.", 0, 0, 0, 0, 0, true),
    // VOLTAR
    LIGHTNING_STRIKE("VOLTAR", "Coup de Foudre", "La foudre s'abat sur l'adversaire visé.", 15, 0, 30, 0, 9),
    ELECTRIC_DASH("VOLTAR", "Réflexes de la Foudre", "Passif : attaques et déplacements fulgurants, marches plus hautes.", 0, 0, 0, 0, 0, true),
    ELECTRIC_FIELD("VOLTAR", "Champ Électrique", "Un champ d'arcs électriques foudroie les ennemis proches.", 40, 8, 5, 0, 2),
    // CHRONOS
    TIME_STOP("CHRONOS", "Arrêt du Temps", "Le temps s'arrête dans une petite zone.", 60, 4, 6, 0, 0),
    SLOW_ENEMIES("CHRONOS", "Ralentissement", "Ralentit les ennemis proches.", 25, 8, 8, 3, 0),
    HASTE_SELF("CHRONOS", "Temps Dilaté", "Passif : vitesse d'attaque, de déplacement et de minage accrues.", 0, 0, 0, 0, 0, true),
    // UMBRA
    INVISIBILITY("UMBRA", "Marche de l'Ombre", "Passif : déplacement furtif rapide et marches plus hautes.", 0, 0, 0, 0, 0, true),
    SHORT_TELEPORT("UMBRA", "Pas d'Ombre", "Téléportation courte dans la direction du regard.", 8, 0, 8, 0, 0),
    DARK_ZONE("UMBRA", "Zone d'Obscurité", "Plonge une zone dans les ténèbres.", 45, 8, 6, 0, 0),
    // SANGUIS
    LIFESTEAL("SANGUIS", "Vol de Vie", "Vos attaques vous soignent pendant un moment.", 40, 10, 0, 0.4, 0),
    SHOCKWAVE("SANGUIS", "Onde de Choc", "Une onde sanglante repousse et blesse.", 18, 0, 5, 1.2, 5),
    REGEN("SANGUIS", "Sang Vorace", "Passif : 3 cœurs de vie maximale en plus et coups plus violents.", 0, 0, 0, 0, 0, true),
    // TERRAX
    SEISMIC_WAVE("TERRAX", "Onde Sismique", "Une fissure traverse le sol devant vous.", 15, 0, 12, 0.8, 5),
    STONE_WALL("TERRAX", "Mur de Pierre", "Un mur de pierre temporaire surgit devant vous.", 25, 10, 5, 0, 0),
    DAMAGE_RESIST("TERRAX", "Peau de Roche", "Passif : armure, solidité et résistance au recul très élevées.", 0, 0, 0, 0, 0, true),
    // AQUARIS
    WATER_PRISON("AQUARIS", "Prison d'Eau", "Emprisonne une cible dans une bulle d'eau.", 35, 5, 20, 0, 1),
    WAVE("AQUARIS", "Vague", "Une vague repousse les ennemis devant vous.", 20, 0, 10, 1.8, 3),
    WATER_BREATHING("AQUARIS", "Respiration des Abysses", "Passif : respiration aquatique permanente, nage et minage sous l'eau surpuissants.", 0, 0, 0, 0, 0, true),
    // NOX
    SUMMON_SERVANTS("NOX", "Serviteurs de la Mort", "Invoque des serviteurs temporaires.", 60, 30, 20, 3, 0),
    FEAR_AURA("NOX", "Étreinte de la Mort", "Passif : coups mortels, plus de vie et résistance au recul.", 0, 0, 0, 0, 0, true),
    CURSE("NOX", "Malédiction", "Maudit un adversaire.", 40, 10, 25, 0, 0),
    // PHANTOM
    CLONES("PHANTOM", "Clones", "Crée des clones qui trompent vos adversaires.", 30, 12, 0, 3, 0),
    TELEPORT("PHANTOM", "Téléportation Illusoire", "Vous vous téléportez en laissant un leurre.", 15, 6, 14, 0, 0),
    CONFUSION("PHANTOM", "Corps d'Illusion", "Passif : plus petit, plus rapide, chutes amorties et grandes enjambées.", 0, 0, 0, 0, 0, true),
    // GRAVITAS
    LEVITATE_ZONE("GRAVITAS", "Poids du Monde", "Passif : coups écrasants, armure et résistance au recul.", 0, 0, 0, 0, 0, true),
    GRAVITY_CRUSH("GRAVITAS", "Écrasement Gravitationnel", "Écrase les ennemis au sol.", 25, 0, 7, 2.0, 6),
    LOW_GRAVITY("GRAVITAS", "Maître de la Gravité", "Passif : gravité réduite, sauts immenses et chutes sans dégâts.", 0, 0, 0, 0, 0, true),
    // VOID
    RIFT("VOID", "Faille Dimensionnelle", "Ouvre une faille qui aspire les ennemis.", 40, 6, 7, 0.35, 2),
    VOID_PROJECTILE("VOID", "Projectile du Néant", "Lance un projectile du Néant.", 12, 0, 40, 1.6, 8),
    VOID_TELEPORT("VOID", "Saut du Néant", "Téléportation dans la zone ciblée.", 25, 0, 40, 0, 0);

    private final String demon;
    private final String label;
    private final String description;
    private final double cooldown;
    private final double duration;
    private final double range;
    private final double strength;
    private final double damage;
    private final boolean passive;

    PowerType(String demon, String label, String description, double cooldown, double duration,
              double range, double strength, double damage) {
        this(demon, label, description, cooldown, duration, range, strength, damage, false);
    }

    PowerType(String demon, String label, String description, double cooldown, double duration,
              double range, double strength, double damage, boolean passive) {
        this.demon = demon;
        this.label = label;
        this.description = description;
        this.cooldown = cooldown;
        this.duration = duration;
        this.range = range;
        this.strength = strength;
        this.damage = damage;
        this.passive = passive;
    }

    public String demon() { return demon; }
    public String label() { return label; }
    public String description() { return description; }
    public double cooldown() { return cooldown; }
    public double duration() { return duration; }
    public double range() { return range; }
    public double strength() { return strength; }
    public double damage() { return damage; }
    public boolean passive() { return passive; }

    public static PowerType of(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}

/**
 * Zones où les pouvoirs sont désactivés : mondes, cuboïdes configurés, et régions WorldGuard
 * (via réflexion : aucune dépendance de compilation, le plugin reste optionnel).
 */
final class RegionGuard {
    private record Cuboid(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        boolean contains(Location l) {
            return l.getWorld() != null && l.getWorld().getName().equalsIgnoreCase(world)
                    && l.getBlockX() >= x1 && l.getBlockX() <= x2
                    && l.getBlockY() >= y1 && l.getBlockY() <= y2
                    && l.getBlockZ() >= z1 && l.getBlockZ() <= z2;
        }
    }

    private final DemonPactPlugin plugin;
    private final Set<String> worlds = new HashSet<>();
    private final Set<String> regions = new HashSet<>();
    private final List<Cuboid> cuboids = new ArrayList<>();
    private boolean wgBroken;

    public RegionGuard(DemonPactPlugin plugin) { this.plugin = plugin; }

    public void load() {
        worlds.clear();
        regions.clear();
        cuboids.clear();
        wgBroken = false;
        for (String w : plugin.getConfig().getStringList("disabled-zones.worlds")) worlds.add(w.toLowerCase(Locale.ROOT));
        for (String r : plugin.getConfig().getStringList("disabled-zones.worldguard-regions")) regions.add(r.toLowerCase(Locale.ROOT));
        // Format : "monde:x1,y1,z1:x2,y2,z2"
        for (String s : plugin.getConfig().getStringList("disabled-zones.cuboids")) {
            try {
                String[] p = s.split(":");
                String[] a = p[1].split(",");
                String[] b = p[2].split(",");
                int ax = Integer.parseInt(a[0].trim()), ay = Integer.parseInt(a[1].trim()), az = Integer.parseInt(a[2].trim());
                int bx = Integer.parseInt(b[0].trim()), by = Integer.parseInt(b[1].trim()), bz = Integer.parseInt(b[2].trim());
                cuboids.add(new Cuboid(p[0].trim(), Math.min(ax, bx), Math.min(ay, by), Math.min(az, bz),
                        Math.max(ax, bx), Math.max(ay, by), Math.max(az, bz)));
            } catch (Exception e) {
                plugin.getLogger().warning("Cuboïde invalide dans disabled-zones : " + s);
            }
        }
    }

    public boolean isDisabled(Location l) {
        if (l == null || l.getWorld() == null) return false;
        if (worlds.contains(l.getWorld().getName().toLowerCase(Locale.ROOT))) return true;
        for (Cuboid c : cuboids) if (c.contains(l)) return true;
        return !regions.isEmpty() && inWorldGuardRegion(l);
    }

    private boolean inWorldGuardRegion(Location loc) {
        if (wgBroken || plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) return false;
        try {
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wg = wgClass.getMethod("getInstance").invoke(null);
            Object platform = wgClass.getMethod("getPlatform").invoke(wg);
            Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = container.getClass().getMethod("createQuery").invoke(container);
            Class<?> adapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object wgLoc = adapter.getMethod("adapt", Location.class).invoke(null, loc);
            Method getRegions = null;
            for (Method m : query.getClass().getMethods()) {
                if (m.getName().equals("getApplicableRegions") && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isAssignableFrom(wgLoc.getClass())) {
                    getRegions = m;
                    break;
                }
            }
            if (getRegions == null) throw new NoSuchMethodException("getApplicableRegions");
            Object set = getRegions.invoke(query, wgLoc);
            for (Object region : (Iterable<?>) set) {
                String id = String.valueOf(region.getClass().getMethod("getId").invoke(region));
                if (regions.contains(id.toLowerCase(Locale.ROOT))) return true;
            }
        } catch (Exception | LinkageError e) {
            wgBroken = true;
            plugin.getLogger().warning("Compatibilité WorldGuard désactivée (API inattendue) : " + e);
        }
        return false;
    }
}

/** Utilitaires de texte : codes couleur &amp; / §, placeholders, formatage du temps. */
final class Txt {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Txt() {}

    /** Convertit une chaîne avec codes couleur (&amp; ou §) en Component. */
    public static Component c(String s) {
        return LEGACY.deserialize(s == null ? "" : s.replace('§', '&'));
    }

    /** Idem, sans italique (pour les noms et lores d'objets). */
    public static Component item(String s) {
        return c(s).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> items(List<String> lines) {
        List<Component> out = new ArrayList<>();
        for (String l : lines) out.add(item(l));
        return out;
    }

    public static String plain(Component c) {
        return PlainTextComponentSerializer.plainText().serialize(c);
    }

    /** Retire les codes couleur. */
    public static String strip(String s) {
        return s == null ? "" : s.replaceAll("[&§][0-9a-fA-Fk-oK-OrR]", "");
    }

    /** Remplace {cle} par valeur, paires clé/valeur à la suite. */
    public static String fill(String s, Object... kv) {
        if (s == null) return "";
        for (int i = 0; i + 1 < kv.length; i += 2) {
            s = s.replace("{" + kv[i] + "}", String.valueOf(kv[i + 1]));
        }
        return s;
    }

    public static String mmss(long seconds) {
        if (seconds < 0) seconds = 0;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    public static String pretty(String enumName) {
        String s = enumName.toLowerCase().replace('_', ' ');
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

final class Defaults {
    private Defaults() {}

    static final String CONFIG = """
# ============================================================================
#  DemonPact - configuration (Paper 26.3)
#  Codes couleur : &  | Sons : clés Minecraft (ex: entity.wither.spawn)
#  Particules : clés Minecraft sans donnée (ex: flame, portal, end_rod)
#  Dialogues des démons : voir dialogues.yml
# ============================================================================
prefix: "&4&l[&c&lDemonPact&4&l] &7"

# --- Négociation -------------------------------------------------------------
negotiation:
  time-seconds: 300          # temps pour accepter / refuser (5 minutes)
  on-refuse: DESPAWN         # DESPAWN : le démon disparaît | NEXT_PLAYER : il se tourne vers un autre joueur
  on-timeout: NEXT_PLAYER    # idem, en cas de non-réponse
  open-gui-delay-ticks: 40   # délai avant l'ouverture automatique de l'interface
  chat-radius: 60            # portée des paroles du démon (le joueur ciblé les reçoit toujours)
  announce-global: true      # annonce de l'apparition à tout le serveur

# --- Demon Stell (autel) -------------------------------------------------------
# Position : définie par /demon setstell (écrite ici automatiquement), modifiable à la main.
stell:
  world: ""
  x: 0
  y: 0
  z: 0
  build-on-set: true         # construit la plateforme / les runes lors de /demon setstell
  temple-length: 30          # demi-longueur du colossal temple (16 à 48) ; demi-largeur = 75 % ; dôme, tambour, tours et parvis s'adaptent
  column-height: 20          # hauteur des colonnes (8 à 32)
  clear-height: 8            # hauteur minimale dégagée (le plugin dégage toute la hauteur du temple)
  deposit-radius: 2.5        # rayon autour du centre où les offrandes jetées sont absorbées
  protect: true              # interdit casser/poser sur l'autel sans demon.admin
  target-radius: 40          # rayon (blocs) dans lequel le démon choisit un joueur (0 = tout le monde)
  animation-ticks: 100       # durée de l'animation d'invocation (20 ticks = 1 seconde)
  lightning-during-summon: true   # éclairs visuels (sans dégâts) pendant l'invocation
  idle-animation: true       # animation d'attente quand aucun démon n'est présent
  idle-view-distance: 64     # animation visible seulement si un joueur est à moins de X blocs

# --- Pactes ---------------------------------------------------------------------
pact:
  max-active: 1              # nombre de pactes simultanés par joueur
  allow-abandon: true        # /demon abandon
  refund-on-abandon: true    # rend les cœurs sacrifiés quand on abandonne le pacte

# --- Sacrifices -------------------------------------------------------------------
sacrifice:
  heart-health: 2.0          # points de vie par cœur (1 cœur = 2.0)
  min-max-health: 6.0        # vie maximale minimale après sacrifices (6.0 = 3 cœurs)

heads:
  drop-chance: 0.33          # chance (33 %) qu'un joueur laisse sa tête en mourant
  drop-cooldown-seconds: 250 # une fois sa tête tombée, elle ne peut pas retomber avant ce délai
  pvp-only: false            # true : la tête ne tombe que si le joueur est tué par un autre joueur
  require-plugin-tag: false  # true : seules les têtes lâchées par ce plugin sont acceptées
  allow-own-head: false      # interdit de sacrifier sa propre tête

items:
  keep-on-death: true        # l'objet de pacte est conservé à la mort de son propriétaire
  allow-reclaim: true        # /demon item : réémet l'objet (l'ancien devient inerte)
  reclaim-cooldown-seconds: 60

# --- Vol de cœur en tuant un joueur -------------------------------------------------
steal:
  enabled: true
  chance: 0.15               # 15 % de chance, quand vous tuez un joueur, qu'il perde 1 cœur et que vous le gagniez
  cooldown-seconds: 480      # délai avant un nouveau vol (par tueur)
  hearts: 1                  # cœurs volés
  max-bonus-hearts: 10       # maximum de cœurs gagnés par vol cumulés

# --- Déguisement : clic droit avec une tête de joueur --------------------------------
disguise:
  enabled: true
  duration-seconds: 5400     # 1h30
  require-offline: true      # on ne peut prendre que l'identité d'un joueur déconnecté
  # fin : mort, /undeguise (ou /undisguise, /demon undisguise), reconnexion du joueur imité, fin du délai

# --- Apparition automatique d'un démon sur la stell -----------------------------------
auto-spawn:
  enabled: true
  interval-minutes: 120      # toutes les 2 h
  wait-minutes: 10           # temps d'attente avant que le démon parte si personne ne lui parle
  min-players: 1             # joueurs connectés minimum
  # le premier joueur qui fait un clic droit sur le démon reçoit la proposition de pacte

# --- Réglages des pouvoirs -------------------------------------------------------
powers-settings:
  anti-spam-ms: 500          # délai minimal entre deux utilisations de pouvoir
  affect-players: true       # les pouvoirs affectent les autres joueurs
  max-frozen: 40             # entités maximum figées par Arrêt du Temps
  max-servants: 8            # serviteurs maximum par joueur (NOX)
  # --- Puissance globale des pouvoirs (appliquée à toutes les valeurs ci-dessous) ---
  attribute-multiplier: 1.0  # multiplie tous les montants d'attributs des pouvoirs passifs
  damage-multiplier: 2.0     # dégâts x2
  range-multiplier: 1.4      # portées x1.4
  duration-multiplier: 1.5   # durées x1.5 (pouvoirs, bonus et malus)
  strength-multiplier: 1.3   # force x1.3 (+1 niveau pour les pouvoirs à niveau d'effet ; +2 serviteurs/clones)
  cooldown-multiplier: 0.8   # recharges -20 %
  buff-amplifier-bonus: 1    # niveau d'effet en plus pour les bonus du lanceur (max niveau V)
  debuff-amplifier-bonus: 1  # niveau d'effet en plus pour les malus infligés
  passive-multiplier: 1.5    # bonus passifs x1.5 (esquive, vol de vie, dégâts, foudre...)
  passive-amplifier-bonus: 1 # niveau en plus pour les effets permanents (max niveau IV)
  wall-material: STONE_BRICKS

# --- Zones où les pouvoirs sont désactivés ----------------------------------------
disabled-zones:
  worlds: []                 # ex: ["world_spawn"]
  cuboids: []                # ex: ["world:0,0,0:100,255,100"]
  worldguard-regions: []     # noms de régions WorldGuard (si WorldGuard est installé)

# --- Pouvoirs ----------------------------------------------------------------------
# cooldown / duration en secondes, range en blocs. strength et damage dépendent du pouvoir.
# buffs : effets reçus par le lanceur à chaque utilisation. debuffs : effets infligés aux ennemis touchés.
# Format d'un effet : {effect: speed, seconds: 3, amplifier: 1}  (clé d'effet Minecraft, amplifier 0 = niveau I)
# Les clés name / description sont facultatives. enabled: false désactive un pouvoir.
# attributes : pouvoirs PASSIFS qui modifient les attributs du joueur tant que le pacte existe.
#   (amount ; operation : ADD_NUMBER = valeur ajoutée, ADD_SCALAR = pourcentage, 0.5 = +50 %). Supprimés à l'abandon du pacte.
# Aucune évolution : ces valeurs ne changent jamais en jeu.
powers:
  AIR_DASH: {cooldown: 8, duration: 0, range: 2.5, strength: 2.4, damage: 2, particle: cloud, sound: entity.phantom.flap, enabled: true, buffs: [{effect: speed, seconds: 3, amplifier: 1}], debuffs: []}
  AIR_JUMP: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: cloud, sound: entity.phantom.flap, enabled: true, attributes: [{attribute: jump_strength, amount: 0.5, operation: ADD_SCALAR}, {attribute: safe_fall_distance, amount: 8, operation: ADD_NUMBER}, {attribute: movement_speed, amount: 0.12, operation: ADD_SCALAR}]}
  TORNADO: {cooldown: 25, duration: 3, range: 6, strength: 1.6, damage: 4, particle: cloud, sound: entity.breeze.wind_burst, enabled: true, buffs: [], debuffs: [{effect: nausea, seconds: 4, amplifier: 0}, {effect: slowness, seconds: 2, amplifier: 1}]}
  FIRE_DASH: {cooldown: 10, duration: 4, range: 2.5, strength: 2, damage: 4, particle: flame, sound: entity.blaze.shoot, enabled: true, buffs: [{effect: strength, seconds: 4, amplifier: 0}], debuffs: []}
  FLAME_BURST: {cooldown: 20, duration: 5, range: 6, strength: 1.3, damage: 6, particle: flame, sound: entity.blaze.shoot, enabled: true, buffs: [{effect: strength, seconds: 6, amplifier: 0}], debuffs: [{effect: blindness, seconds: 2, amplifier: 0}]}
  FIRE_RESISTANCE: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: flame, sound: item.firecharge.use, enabled: true, attributes: [{attribute: attack_damage, amount: 3, operation: ADD_NUMBER}, {attribute: burning_time, amount: -0.9, operation: ADD_NUMBER}, {attribute: explosion_knockback_resistance, amount: 0.5, operation: ADD_NUMBER}]}
  LIGHTNING_STRIKE: {cooldown: 15, duration: 0, range: 30, strength: 0, damage: 9, particle: electric_spark, sound: entity.lightning_bolt.thunder, enabled: true, buffs: [], debuffs: [{effect: weakness, seconds: 4, amplifier: 0}, {effect: glowing, seconds: 6, amplifier: 0}]}
  ELECTRIC_DASH: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: electric_spark, sound: entity.lightning_bolt.impact, enabled: true, attributes: [{attribute: attack_speed, amount: 0.5, operation: ADD_SCALAR}, {attribute: movement_speed, amount: 0.1, operation: ADD_SCALAR}, {attribute: step_height, amount: 0.5, operation: ADD_NUMBER}]}
  ELECTRIC_FIELD: {cooldown: 40, duration: 8, range: 5, strength: 0, damage: 2, particle: electric_spark, sound: block.beacon.activate, enabled: true, buffs: [{effect: haste, seconds: 8, amplifier: 1}], debuffs: [{effect: weakness, seconds: 2, amplifier: 0}]}
  TIME_STOP: {cooldown: 60, duration: 4, range: 6, strength: 0, damage: 0, particle: end_rod, sound: block.bell.use, enabled: true, buffs: [{effect: speed, seconds: 4, amplifier: 2}, {effect: strength, seconds: 4, amplifier: 1}], debuffs: []}
  SLOW_ENEMIES: {cooldown: 25, duration: 8, range: 8, strength: 3, damage: 0, particle: end_rod, sound: block.bell.use, enabled: true, buffs: [], debuffs: [{effect: weakness, seconds: 6, amplifier: 0}]}
  HASTE_SELF: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: end_rod, sound: block.beacon.power_select, enabled: true, attributes: [{attribute: attack_speed, amount: 0.4, operation: ADD_SCALAR}, {attribute: movement_speed, amount: 0.15, operation: ADD_SCALAR}, {attribute: block_break_speed, amount: 0.5, operation: ADD_SCALAR}]}
  INVISIBILITY: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: squid_ink, sound: entity.enderman.teleport, enabled: true, attributes: [{attribute: sneaking_speed, amount: 0.5, operation: ADD_NUMBER}, {attribute: movement_speed, amount: 0.1, operation: ADD_SCALAR}, {attribute: step_height, amount: 0.4, operation: ADD_NUMBER}]}
  SHORT_TELEPORT: {cooldown: 8, duration: 0, range: 8, strength: 0, damage: 0, particle: squid_ink, sound: entity.enderman.teleport, enabled: true, buffs: [{effect: speed, seconds: 3, amplifier: 1}], debuffs: []}
  DARK_ZONE: {cooldown: 45, duration: 8, range: 6, strength: 0, damage: 0, particle: squid_ink, sound: entity.warden.sonic_charge, enabled: true, buffs: [{effect: night_vision, seconds: 8, amplifier: 0}], debuffs: [{effect: weakness, seconds: 4, amplifier: 0}]}
  LIFESTEAL: {cooldown: 40, duration: 10, range: 0, strength: 0.4, damage: 0, particle: crimson_spore, sound: entity.witch.celebrate, enabled: true, buffs: [{effect: strength, seconds: 10, amplifier: 0}], debuffs: []}
  SHOCKWAVE: {cooldown: 18, duration: 0, range: 5, strength: 1.2, damage: 5, particle: damage_indicator, sound: entity.warden.sonic_boom, enabled: true, buffs: [{effect: absorption, seconds: 6, amplifier: 0}], debuffs: []}
  REGEN: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: heart, sound: entity.generic.drink, enabled: true, attributes: [{attribute: max_health, amount: 6, operation: ADD_NUMBER}, {attribute: attack_damage, amount: 2, operation: ADD_NUMBER}]}
  SEISMIC_WAVE: {cooldown: 15, duration: 0, range: 12, strength: 0.8, damage: 5, particle: poof, sound: block.stone.break, enabled: true, buffs: [], debuffs: [{effect: slowness, seconds: 3, amplifier: 1}, {effect: weakness, seconds: 3, amplifier: 0}]}
  STONE_WALL: {cooldown: 25, duration: 10, range: 5, strength: 0, damage: 0, particle: poof, sound: block.stone.place, enabled: true, buffs: [{effect: resistance, seconds: 6, amplifier: 0}], debuffs: []}
  DAMAGE_RESIST: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: poof, sound: block.stone.place, enabled: true, attributes: [{attribute: armor, amount: 6, operation: ADD_NUMBER}, {attribute: armor_toughness, amount: 4, operation: ADD_NUMBER}, {attribute: knockback_resistance, amount: 0.6, operation: ADD_NUMBER}]}
  WATER_PRISON: {cooldown: 35, duration: 5, range: 20, strength: 0, damage: 1, particle: bubble_pop, sound: entity.player.splash.high_speed, enabled: true, buffs: [], debuffs: [{effect: weakness, seconds: 5, amplifier: 1}]}
  WAVE: {cooldown: 20, duration: 0, range: 10, strength: 1.8, damage: 3, particle: splash, sound: entity.player.splash.high_speed, enabled: true, buffs: [{effect: dolphins_grace, seconds: 6, amplifier: 0}], debuffs: [{effect: slowness, seconds: 3, amplifier: 0}]}
  WATER_BREATHING: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: bubble_pop, sound: block.bubble_column.upwards_ambient, enabled: true, attributes: [{attribute: oxygen_bonus, amount: 20, operation: ADD_NUMBER}, {attribute: water_movement_efficiency, amount: 1.0, operation: ADD_NUMBER}, {attribute: submerged_mining_speed, amount: 3.0, operation: ADD_SCALAR}]}
  SUMMON_SERVANTS: {cooldown: 60, duration: 30, range: 20, strength: 3, damage: 0, particle: soul, sound: entity.evoker.prepare_summon, enabled: true, buffs: [{effect: resistance, seconds: 10, amplifier: 0}], debuffs: []}
  FEAR_AURA: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: soul, sound: entity.ghast.scream, enabled: true, attributes: [{attribute: attack_damage, amount: 3, operation: ADD_NUMBER}, {attribute: max_health, amount: 4, operation: ADD_NUMBER}, {attribute: knockback_resistance, amount: 0.3, operation: ADD_NUMBER}]}
  CURSE: {cooldown: 40, duration: 10, range: 25, strength: 0, damage: 0, particle: soul, sound: entity.wither.hurt, enabled: true, buffs: [], debuffs: [{effect: mining_fatigue, seconds: 10, amplifier: 0}]}
  CLONES: {cooldown: 30, duration: 12, range: 0, strength: 3, damage: 0, particle: witch, sound: entity.illusioner.cast_spell, enabled: true, buffs: [{effect: invisibility, seconds: 2, amplifier: 0}, {effect: speed, seconds: 6, amplifier: 2}], debuffs: []}
  TELEPORT: {cooldown: 15, duration: 6, range: 14, strength: 0, damage: 0, particle: witch, sound: entity.illusioner.mirror_move, enabled: true, buffs: [{effect: speed, seconds: 4, amplifier: 1}], debuffs: []}
  CONFUSION: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: witch, sound: entity.illusioner.cast_spell, enabled: true, attributes: [{attribute: movement_speed, amount: 0.2, operation: ADD_SCALAR}, {attribute: step_height, amount: 1.0, operation: ADD_NUMBER}, {attribute: scale, amount: -0.2, operation: ADD_SCALAR}, {attribute: fall_damage_multiplier, amount: -0.5, operation: ADD_NUMBER}]}
  LEVITATE_ZONE: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: reverse_portal, sound: block.beacon.power_select, enabled: true, attributes: [{attribute: attack_damage, amount: 2, operation: ADD_NUMBER}, {attribute: armor, amount: 4, operation: ADD_NUMBER}, {attribute: knockback_resistance, amount: 0.5, operation: ADD_NUMBER}]}
  GRAVITY_CRUSH: {cooldown: 25, duration: 0, range: 7, strength: 2, damage: 6, particle: crit, sound: block.anvil.land, enabled: true, buffs: [{effect: resistance, seconds: 4, amplifier: 1}], debuffs: []}
  LOW_GRAVITY: {cooldown: 0, duration: 0, range: 0, strength: 0, damage: 0, particle: reverse_portal, sound: block.beacon.power_select, enabled: true, attributes: [{attribute: gravity, amount: -0.04, operation: ADD_NUMBER}, {attribute: jump_strength, amount: 0.3, operation: ADD_NUMBER}, {attribute: safe_fall_distance, amount: 20, operation: ADD_NUMBER}, {attribute: fall_damage_multiplier, amount: -0.5, operation: ADD_NUMBER}]}
  RIFT: {cooldown: 40, duration: 6, range: 7, strength: 0.35, damage: 2, particle: portal, sound: block.portal.ambient, enabled: true, buffs: [], debuffs: [{effect: glowing, seconds: 6, amplifier: 0}, {effect: weakness, seconds: 3, amplifier: 0}]}
  VOID_PROJECTILE: {cooldown: 12, duration: 0, range: 40, strength: 1.6, damage: 8, particle: portal, sound: entity.enderman.scream, enabled: true, buffs: [], debuffs: [{effect: wither, seconds: 3, amplifier: 0}]}
  VOID_TELEPORT: {cooldown: 25, duration: 0, range: 40, strength: 0, damage: 0, particle: portal, sound: entity.enderman.teleport, enabled: true, buffs: [{effect: invisibility, seconds: 2, amplifier: 0}, {effect: speed, seconds: 3, amplifier: 1}], debuffs: []}

# --- Les 12 démons ----------------------------------------------------------------
# Chaque contrat : un pouvoir contre un ou plusieurs sacrifices.
# Types de sacrifice : HEARTS (cœurs permanents), HEADS (têtes de joueurs), ITEMS (material + amount).
# entity : type d'entité de l'apparence du démon ; element : WIND FIRE LIGHTNING TIME SHADOW BLOOD EARTH WATER DEATH ILLUSION GRAVITY VOID
demons:
  AERON:
    name: "AERON"
    title: "Démon du Vent"
    color: "&b"
    glow-color: aqua
    rarity: RARE
    power-level: 5
    element: WIND
    entity: vex
    scale: 1.8
    hover: true
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: speed, amplifier: 0}, {effect: jump_boost, amplifier: 0}]
      dodge-chance: 0.05
    item:
      material: FEATHER
      name: "&b&lPlume du Vent"
      lore:
        - "&7Une plume qui ne retombe jamais."
    contracts:
      air_dash:
        power: AIR_DASH
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Vous perdez 2 cœurs permanents."
      tornado:
        power: TORNADO
        costs: [{type: HEADS, amount: 3}]
        side-effects:
          - "&7Trois têtes de joueurs sont consumées."
      air_jump:
        power: AIR_JUMP
        costs: [{type: HEARTS, amount: 1}]
        side-effects:
          - "&7Vous perdez 1 cœur permanent."
  IGNIS:
    name: "IGNIS"
    title: "Démon du Feu"
    color: "&c"
    glow-color: red
    rarity: RARE
    power-level: 6
    element: FIRE
    entity: blaze
    scale: 1.4
    hover: true
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: fire_resistance, amplifier: 0}]
      ignite-seconds: 4
      bonus-damage: 0.05
    item:
      material: BLAZE_POWDER
      name: "&c&lCœur des Flammes"
      lore:
        - "&7Il bat comme un brasier."
    contracts:
      fire_dash:
        power: FIRE_DASH
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Les flammes consument votre vitalité."
      flame_burst:
        power: FLAME_BURST
        costs: [{type: HEARTS, amount: 3}]
        side-effects:
          - "&7Un sacrifice lourd pour une explosion."
      fire_resistance:
        power: FIRE_RESISTANCE
        costs: [{type: HEARTS, amount: 1}]
        side-effects:
          - "&7Le feu ne vous brûlera plus."
  VOLTAR:
    name: "VOLTAR"
    title: "Démon de la Foudre"
    color: "&e"
    glow-color: yellow
    rarity: EPIC
    power-level: 7
    element: LIGHTNING
    entity: breeze
    scale: 1.5
    hover: true
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: haste, amplifier: 0}]
      lightning-chance: 0.12
    item:
      material: AMETHYST_SHARD
      name: "&e&lCristal de Foudre"
      lore:
        - "&7Il crépite à votre contact."
    contracts:
      lightning_strike:
        power: LIGHTNING_STRIKE
        costs: [{type: HEADS, amount: 3}]
        side-effects:
          - "&7Vous devez ramener des têtes authentiques."
      electric_dash:
        power: ELECTRIC_DASH
        costs: [{type: HEADS, amount: 2}]
        side-effects:
          - "&7Deux têtes de joueurs."
      electric_field:
        power: ELECTRIC_FIELD
        costs: [{type: HEADS, amount: 4}]
        side-effects:
          - "&7Quatre têtes de joueurs."
  CHRONOS:
    name: "CHRONOS"
    title: "Démon du Temps"
    color: "&6"
    glow-color: gold
    rarity: LEGENDARY
    power-level: 9
    element: TIME
    entity: evoker
    helmet: GOLDEN_HELMET
    scale: 1.3
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: speed, amplifier: 0}, {effect: haste, amplifier: 0}]
    item:
      material: CLOCK
      name: "&6&lMontre du Temps"
      lore:
        - "&7Son tic-tac n'est pas celui du monde."
    contracts:
      time_stop:
        power: TIME_STOP
        costs: [{type: HEARTS, amount: 4}]
        side-effects:
          - "&7Quatre cœurs sacrifiés pour arrêter le temps."
      slow_enemies:
        power: SLOW_ENEMIES
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Deux cœurs permanents."
      haste_self:
        power: HASTE_SELF
        costs: [{type: HEARTS, amount: 1}]
        side-effects:
          - "&7Un cœur permanent."
  UMBRA:
    name: "UMBRA"
    title: "Démon des Ombres"
    color: "&8"
    glow-color: dark_gray
    rarity: EPIC
    power-level: 7
    element: SHADOW
    entity: enderman
    scale: 1.2
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: night_vision, amplifier: 0}]
      dodge-chance: 0.10
    item:
      material: ECHO_SHARD
      name: "&8&lFragment des Ombres"
      lore:
        - "&7Il absorbe la lumière."
    contracts:
      invisibility:
        power: INVISIBILITY
        costs: [{type: HEADS, amount: 2}]
        side-effects:
          - "&7Deux têtes de joueurs."
      short_teleport:
        power: SHORT_TELEPORT
        costs: [{type: HEADS, amount: 1}]
        side-effects:
          - "&7Une seule tête suffit."
      dark_zone:
        power: DARK_ZONE
        costs: [{type: HEADS, amount: 3}]
        side-effects:
          - "&7Trois têtes de joueurs."
  SANGUIS:
    name: "SANGUIS"
    title: "Démon du Sang"
    color: "&4"
    glow-color: dark_red
    rarity: EPIC
    power-level: 7
    element: BLOOD
    entity: vindicator
    helmet: LEATHER_HELMET
    scale: 1.3
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: []
      lifesteal: 0.12
      bonus-damage: 0.05
    item:
      material: FERMENTED_SPIDER_EYE
      name: "&4&lCœur Sanguinaire"
      lore:
        - "&7Il palpite encore."
    contracts:
      lifesteal:
        power: LIFESTEAL
        costs: [{type: HEARTS, amount: 3}]
        side-effects:
          - "&7Votre sang nourrit le pacte."
      shockwave:
        power: SHOCKWAVE
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Deux cœurs permanents."
      regen:
        power: REGEN
        costs: [{type: HEARTS, amount: 1}]
        side-effects:
          - "&7Un cœur permanent."
  TERRAX:
    name: "TERRAX"
    title: "Démon de la Terre"
    color: "&2"
    glow-color: dark_green
    rarity: RARE
    power-level: 6
    element: EARTH
    entity: iron_golem
    scale: 1.6
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: resistance, amplifier: 0}]
    item:
      material: CLAY_BALL
      name: "&2&lNoyau Terrestre"
      lore:
        - "&7Lourd comme une montagne."
    contracts:
      seismic_wave:
        power: SEISMIC_WAVE
        costs: [{type: ITEMS, material: DIAMOND, amount: 16}]
        side-effects:
          - "&7Seize diamants à offrir."
      stone_wall:
        power: STONE_WALL
        costs: [{type: ITEMS, material: EMERALD, amount: 24}]
        side-effects:
          - "&7Vingt-quatre émeraudes."
      damage_resist:
        power: DAMAGE_RESIST
        costs: [{type: ITEMS, material: NETHERITE_SCRAP, amount: 4}]
        side-effects:
          - "&7Quatre débris de netherite."
  AQUARIS:
    name: "AQUARIS"
    title: "Démon des Abysses"
    color: "&3"
    glow-color: dark_aqua
    rarity: EPIC
    power-level: 7
    element: WATER
    entity: drowned
    helmet: TURTLE_HELMET
    scale: 1.4
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: dolphins_grace, amplifier: 0}, {effect: conduit_power, amplifier: 0}]
    item:
      material: HEART_OF_THE_SEA
      name: "&3&lPerle des Abysses"
      lore:
        - "&7On y entend l'océan."
    contracts:
      water_prison:
        power: WATER_PRISON
        costs: [{type: HEADS, amount: 3}]
        side-effects:
          - "&7Trois têtes de joueurs."
      wave:
        power: WAVE
        costs: [{type: HEADS, amount: 2}]
        side-effects:
          - "&7Deux têtes de joueurs."
      water_breathing:
        power: WATER_BREATHING
        costs: [{type: HEADS, amount: 1}]
        side-effects:
          - "&7Pouvoir passif : respiration aquatique permanente."
  NOX:
    name: "NOX"
    title: "Démon de la Mort"
    color: "&5"
    glow-color: dark_purple
    rarity: LEGENDARY
    power-level: 9
    element: DEATH
    entity: wither_skeleton
    scale: 1.5
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: night_vision, amplifier: 0}]
      on-hit: {effect: wither, seconds: 3, amplifier: 0, chance: 0.2}
    item:
      material: WITHER_SKELETON_SKULL
      name: "&5&lCrâne de la Mort"
      lore:
        - "&7Il murmure des noms oubliés."
    contracts:
      summon_servants:
        power: SUMMON_SERVANTS
        costs: [{type: HEARTS, amount: 3}]
        side-effects:
          - "&7Trois cœurs permanents."
      fear_aura:
        power: FEAR_AURA
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Deux cœurs permanents."
      curse:
        power: CURSE
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Deux cœurs permanents."
  PHANTOM:
    name: "PHANTOM"
    title: "Démon de l'Illusion"
    color: "&d"
    glow-color: light_purple
    rarity: EPIC
    power-level: 7
    element: ILLUSION
    entity: illusioner
    scale: 1.2
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: speed, amplifier: 0}]
      dodge-chance: 0.12
    item:
      material: SPYGLASS
      name: "&d&lMiroir Illusoire"
      lore:
        - "&7Votre reflet ne vous ressemble plus."
    contracts:
      clones:
        power: CLONES
        costs: [{type: ITEMS, material: TOTEM_OF_UNDYING, amount: 1}]
        side-effects:
          - "&7Un totem d'immortalité."
      teleport:
        power: TELEPORT
        costs: [{type: ITEMS, material: ENDER_PEARL, amount: 16}]
        side-effects:
          - "&7Seize perles de l'Ender."
      confusion:
        power: CONFUSION
        costs: [{type: ITEMS, material: ENCHANTED_GOLDEN_APPLE, amount: 1}]
        side-effects:
          - "&7Une pomme d'or enchantée."
  GRAVITAS:
    name: "GRAVITAS"
    title: "Démon de la Gravité"
    color: "&9"
    glow-color: blue
    rarity: LEGENDARY
    power-level: 8
    element: GRAVITY
    entity: ravager
    scale: 1.2
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: jump_boost, amplifier: 1}, {effect: slow_falling, amplifier: 0}]
    item:
      material: NAUTILUS_SHELL
      name: "&9&lOrbe Gravitationnel"
      lore:
        - "&7Il pèse plus qu'il n'y paraît."
    contracts:
      levitate_zone:
        power: LEVITATE_ZONE
        costs: [{type: HEARTS, amount: 2}]
        side-effects:
          - "&7Deux cœurs permanents."
      gravity_crush:
        power: GRAVITY_CRUSH
        costs: [{type: HEARTS, amount: 3}]
        side-effects:
          - "&7Trois cœurs permanents."
      low_gravity:
        power: LOW_GRAVITY
        costs: [{type: HEARTS, amount: 1}]
        side-effects:
          - "&7Un cœur permanent."
  VOID:
    name: "VOID"
    title: "Démon du Néant"
    color: "&5"
    glow-color: black
    rarity: MYTHIC
    power-level: 10
    element: VOID
    entity: warden
    scale: 1.0
    hover: false
    # bonus passifs : actifs tant que l'objet du pacte est dans l'inventaire
    passive:
      effects: [{effect: night_vision, amplifier: 0}]
      dodge-chance: 0.08
      bonus-damage: 0.08
    item:
      material: NETHER_STAR
      name: "&5&lFragment du Néant"
      lore:
        - "&7Il n'y a rien dedans. C'est le problème."
    contracts:
      rift:
        power: RIFT
        costs: [{type: HEADS, amount: 2}, {type: HEARTS, amount: 1}]
        side-effects:
          - "&7Deux têtes et un cœur permanent."
      void_projectile:
        power: VOID_PROJECTILE
        costs: [{type: HEADS, amount: 3}, {type: HEARTS, amount: 2}]
        side-effects:
          - "&7Trois têtes et deux cœurs permanents."
      void_teleport:
        power: VOID_TELEPORT
        costs: [{type: HEADS, amount: 2}, {type: HEARTS, amount: 2}]
        side-effects:
          - "&7Deux têtes et deux cœurs permanents."
""";

    static final String DIALOGUES = """
# ============================================================================
# DemonPact - dialogues des 12 démons
# Une ligne est tirée au hasard dans chaque liste. Placeholders :
#   {player}  joueur concerné      {demon}  nom du démon
#   {power}   pouvoir proposé      {cost}   sacrifice demandé
# Catégories : appear, propose, accept, refuse, timeout, sacrifice.HEARTS / HEADS / ITEMS
# (la catégorie "sacrifice" suit le premier sacrifice du contrat)
# ============================================================================

AERON:
  appear:
    - "Enfin quelqu'un d'assez lent pour m'attendre. Je suis là, regardez-moi vite, je ne resterai pas."
    - "Le vent a soufflé, et vous voilà tous immobiles. Charmant spectacle."
    - "Je suis arrivé avant même que vous ne clignez des yeux. Essayez de me suivre !"
  propose:
    - "Alors {player}, tu veux voler ? {power}, rien que pour toi... si tu es assez rapide pour signer."
    - "Je t'offre {power}, mortel. Dépêche-toi, mon temps vaut plus que ta vie entière."
  accept:
    - "Voilà ! Tu as signé plus vite que je ne l'espérais. Tâche de ne pas tomber, {player}."
    - "Le vent est avec toi. Ne me remercie pas, tu n'aurais pas le temps de finir."
  refuse:
    - "Tu refuses ? Quelle lenteur d'esprit. Le ciel se passera de toi."
    - "Rester cloué au sol, {player}... c'est ton choix. Pathétique."
  timeout:
    - "Tu as mis trop de temps. Le vent n'attend personne, surtout pas toi."
    - "Cinq minutes ! Un souffle m'aurait suffi. Adieu, tortue."
  sacrifice:
    HEARTS:
      - "Un peu de vie, juste de quoi me divertir : {cost}. Tu ne vas pas pleurer pour si peu ?"
      - "Je prends {cost} et tu repars plus léger. Au sens propre, ha !"
    HEADS:
      - "Apporte-moi {cost}. Des trophées, voilà ce qui amuse le vent."
      - "{cost}, des têtes bien arrachées. Je les ferai tourbillonner un moment."
    ITEMS:
      - "Quelques richesses : {cost}. Je les emporterai dans mes tempêtes."
      - "Donne-moi {cost}, que je puisse enfin me moquer de ton coffre vide."

IGNIS:
  appear:
    - "BRÛLEZ ! Tout doit brûler ! Qui ose réveiller IGNIS ?!"
    - "Mes flammes ont faim, mortels. Elles auront toujours faim."
    - "Je sors des cendres de mille villages. Le vôtre sera le prochain."
  propose:
    - "{player} ! Prends {power} et réduis tout en cendres, ou crève dans ton coin !"
    - "Voici {power}. Accepte, ou je te brûle sur place, {player} !"
  accept:
    - "AH ! Ça, c'est un pacte ! Brûle, {player}, brûle tout sur ton passage !"
    - "Enfin du courage ! Mes flammes coulent déjà dans tes veines !"
  refuse:
    - "Tu REFUSES ?! Je devrais te transformer en torche, misérable !"
    - "Lâche ! Retourne trembler dans l'ombre, {player} !"
  timeout:
    - "Tu ne réponds pas ? Mon feu s'est éteint d'ennui. Tu me dégoûtes !"
    - "Cinq minutes à ne rien dire ! Je brûle d'une colère que tu ne mérites même pas !"
  sacrifice:
    HEARTS:
      - "Je veux {cost} ! Ton sang bouillira pour mes flammes !"
      - "{cost}, arrachés à ta chair. Paie, et brûle plus fort !"
    HEADS:
      - "Des têtes, {cost} ! Je les ferai fondre dans ma forge !"
      - "Apporte {cost} et je les ferai flamber !"
    ITEMS:
      - "Jette-moi {cost} dans le brasier, et vite !"
      - "{cost} ! Je veux voir cet or fondre sous mes yeux !"

VOLTAR:
  appear:
    - "Ah ! C'est déjà ici ? Bien, bien, peu importe, vite, vite !"
    - "ZZZT ! Pardon, j'étais ailleurs. Je suis là, maintenant. Peut-être."
    - "Le ciel m'a craché dans votre monde. Je ne sais pas combien de temps je reste !"
  propose:
    - "{player} ! {power}, décide-toi, j'ai déjà changé d'avis deux fois !"
    - "Voilà {power}, ou autre chose, non, {power}, signe, tout de suite !"
  accept:
    - "Parfait ! Ou presque ! Peu importe ! Tu as la foudre, {player} !"
    - "ZZZT ! Signé ! Maintenant va frapper quelque chose avant que je change d'idée !"
  refuse:
    - "Non ? Alors j'ai perdu mon temps. Ou le tien, je ne sais plus."
    - "Refus ! Intéressant ! Ennuyeux ! Je pars !"
  timeout:
    - "Tu ne réponds pas ? J'ai oublié pourquoi j'étais venu. Tant pis !"
    - "Cinq minutes, une éternité ! Un éclair aurait été plus rapide."
  sacrifice:
    HEARTS:
      - "{cost}, un petit prélèvement, une étincelle de vie, c'est tout !"
      - "Donne-moi {cost}, vite, ça fait de jolies étincelles !"
    HEADS:
      - "Il me faut {cost} ! Pourquoi ? Parce que j'aime la foudre qui frappe des têtes !"
      - "{cost}, rapporte-les vite, ou je m'en vais !"
    ITEMS:
      - "{cost}, je ne sais plus pourquoi, mais je les veux !"
      - "Apporte {cost} avant que mon humeur ne tourne !"

CHRONOS:
  appear:
    - "Je suis déjà venu. Je viendrai encore. Vous ne l'avez simplement pas encore vécu."
    - "Le temps se plie, mortels. Ce soir, il se plie pour moi."
    - "Chaque seconde que vous gaspillez m'appartient. Je suis venu les récolter."
  propose:
    - "Tu hésites, {player}... j'ai déjà vu ce que tu choisiras. {power} sera tien."
    - "Je t'offre {power}. Ne cherche pas à comprendre : tu as déjà accepté, dans un autre instant."
  accept:
    - "Comme je le savais. Le temps t'obéit désormais, {player}... pour un moment."
    - "Bien. Tu viens de signer ce que tu avais déjà signé. Savoure."
  refuse:
    - "Tu refuses. Curieux, je t'avais vu accepter. Un autre fil du temps, sans doute."
    - "Un refus, soit. Nous nous reverrons, {player}, avant même que tu ne t'en souviennes."
  timeout:
    - "Cinq minutes écoulées, exactement comme prévu. Le silence aussi est un choix."
    - "Tu as laissé filer le sable. Il ne remonte jamais, mortel."
  sacrifice:
    HEARTS:
      - "Un peu de ta vie, {cost}. Elle t'aurait de toute façon échappé un jour."
      - "{cost}. Le temps te les prendrait de toute façon, autant me les donner."
    HEADS:
      - "Apporte-moi {cost}. Les morts ont déjà vu leur futur, ils n'en ont plus l'usage."
      - "{cost}, ni plus ni moins, comme dans ce que je sais déjà."
    ITEMS:
      - "{cost}. Peu de chose pour qui possède des siècles."
      - "Je veux {cost}. Tu me les devrais de toute façon, dans une autre vie."

UMBRA:
  appear:
    - "..."
    - "Je suis là. Je l'ai toujours été."
    - "Ne cherchez pas mon visage. Il n'y en a pas."
  propose:
    - "{player}. {power}. Murmure oui."
    - "Dans l'ombre, tout se négocie. {power}... {player}."
  accept:
    - "Bien. L'ombre te connaît maintenant."
    - "Va. Sans bruit."
  refuse:
    - "Soit."
    - "Tu retournes à la lumière. Elle te brûlera un jour."
  timeout:
    - "Silence. Je prends cela pour une réponse."
    - "..."
  sacrifice:
    HEARTS:
      - "{cost}. Sans un cri."
      - "Un peu de toi, {cost}. L'ombre sait attendre."
    HEADS:
      - "{cost}. Les morts se taisent bien."
      - "Offre-moi {cost}. Ils ne diront rien."
    ITEMS:
      - "{cost}. Dépose-les dans le noir."
      - "Je prends {cost}. Personne ne verra."

SANGUIS:
  appear:
    - "Mmmh... ça sent le sang frais. Quel merveilleux accueil, mes chéris !"
    - "Ne vous inquiétez pas, je ne mords que ceux qui me plaisent. Et vous me plaisez tous."
    - "Une hémorragie de bonheur ! Me voici, assoiffé et de bonne humeur !"
  propose:
    - "{player}, mon petit pressoir préféré ! {power}, juste un petit sacrifice, une goutte d'amour..."
    - "Signe, mon cœur, signe ! {power} en échange d'un peu de ce délicieux sang."
  accept:
    - "HAHA ! Délicieux ! Tu saignes si bien, {player}. Je t'adore !"
    - "Voilà, c'est fait, ça n'a pas fait trop mal. Enfin, pas pour moi !"
  refuse:
    - "Oh... tu as peur ? Quelle tristesse. Je t'aurais fait un sacrifice si doux."
    - "Refuser mon offre ? Tu me brises le cœur. Enfin, le tien, c'est plus drôle."
  timeout:
    - "Tu n'as rien dit... Ta peur sent merveilleusement bon. Je reviendrai, mon chéri."
    - "Cinq minutes de suspense ! Tu m'as bien fait rire, {player}."
  sacrifice:
    HEARTS:
      - "{cost}, tout simplement ! Une larme de sang, un grand sourire !"
      - "Je veux {cost}, et je te promets de ne les prendre qu'une seule fois. Ha ha !"
    HEADS:
      - "{cost}, mon trésor, bien fraîches, rouges, sanglantes... Comme j'aime !"
      - "Apporte-moi {cost}. Je serai tendre avec elles, promis."
    ITEMS:
      - "{cost}, pour le décor ! Mon boudoir manque de cadeaux."
      - "Donne-moi {cost}, et je ne te prendrai presque rien d'autre."

TERRAX:
  appear:
    - "Je suis TERRAX. La terre se souvient de moi. Inclinez-vous."
    - "Cette montagne n'a pas bougé depuis des millénaires. Moi non plus. Écoutez-moi."
    - "Silence. Le sol tremble sous mon pas. Écoutez la voix de la roche."
  propose:
    - "{player}, approche. {power} te sera accordé selon mes conditions. Elles ne sont pas négociables."
    - "J'ai décidé : {power}. Tu accepteras, ou tu partiras. Choisis, {player}."
  accept:
    - "Le pacte est scellé dans la pierre. Honore-le, {player}."
    - "Bien. Tu es désormais lié à la terre. Ne la trahis jamais."
  refuse:
    - "Tu refuses mon offre ? La terre n'oublie pas les insolents."
    - "Retire-toi. La pierre saura attendre un autre."
  timeout:
    - "Le silence est une insulte à ma patience. Je m'en vais."
    - "Cinq minutes. Une montagne s'érode en moins de temps. Tu m'as déçu."
  sacrifice:
    HEARTS:
      - "J'exige {cost}. Tu me les donneras, comme la terre prend tout à la fin."
      - "{cost}. La loi est la loi."
    HEADS:
      - "Il me faut {cost}. Ce sont les offrandes que j'accepte."
      - "{cost} serviront de fondations. Apporte-les."
    ITEMS:
      - "Pour ma part, {cost}. Rien de moins. La terre ne se contente pas de peu."
      - "Dépose {cost} au pied de l'autel. Je jugerai de leur valeur."

AQUARIS:
  appear:
    - "Les abysses vous observent depuis toujours. Aujourd'hui, c'est moi qui viens."
    - "Calme. Froid. Profond. C'est ainsi que je suis, et c'est ainsi que vous finirez."
    - "Vous respirez encore. Je note ce détail."
  propose:
    - "{player}, j'ai calculé que {power} te serait utile. Les termes sont ci-dessous."
    - "{power} contre un prix raisonnable. Je n'ai jamais perdu une négociation, {player}."
  accept:
    - "Transaction conclue. Tes calculs étaient justes, {player}. Les miens, bien plus."
    - "Comme prévu. Les abysses t'ouvrent leurs eaux."
  refuse:
    - "Refus enregistré. Tu as perdu une opportunité que tu ne retrouveras pas."
    - "Dommage. Statistiquement, tu viens de commettre une erreur."
  timeout:
    - "Le silence est une réponse. Une mauvaise, mais une réponse."
    - "Cinq minutes d'inaction. Intéressant. Peu glorieux."
  sacrifice:
    HEARTS:
      - "Le prix : {cost}. Un montant juste pour un pouvoir juste."
      - "{cost}. J'ai tout calculé, ce n'est que justice."
    HEADS:
      - "Il me faut {cost}. Les profondeurs gardent leurs morts, mais j'en veux d'autres."
      - "{cost}. Un tribut raisonnable pour les abysses."
    ITEMS:
      - "Je demande {cost}. Un échange équitable, comme toujours."
      - "{cost}. Pas un de moins, j'ai déjà compté."

NOX:
  appear:
    - "La mort ne prévient pas. Moi non plus."
    - "Vos souffles se font courts. Je viens récolter ce qu'il en reste."
    - "Je suis NOX. Que chacun sente le froid dans son dos."
  propose:
    - "{player}... {power}. Un murmure de tombeau pour toi."
    - "J'ai un cadeau pour toi, {player}. {power}. Les morts ne mentent jamais."
  accept:
    - "Le pacte est écrit. Les morts s'en souviendront, {player}."
    - "Bien. Désormais, tu marcheras avec nous."
  refuse:
    - "Tu refuses la mort ? Elle viendra quand même, {player}."
    - "Insensé. Le tombeau t'attend, qu'importe ton choix."
  timeout:
    - "Ton silence est celui des tombes. Je m'en vais, pour l'instant."
    - "Cinq minutes. Tu as déjà un pied dans ma demeure."
  sacrifice:
    HEARTS:
      - "{cost}. La vie est un prêt, {player}. Je la réclame."
      - "Tu me dois {cost}. La mort ne demande jamais deux fois."
    HEADS:
      - "Apporte-moi {cost}. Les crânes se rangent bien dans mon royaume."
      - "{cost}. Les morts réclament leurs semblables."
    ITEMS:
      - "{cost}. Même les morts aiment les trésors."
      - "Dépose {cost} sur la pierre. Elle se souviendra."

PHANTOM:
  appear:
    - "Ah, bonsoir ! Ou plutôt, non, ce n'était pas moi. Ou si ? Qui sait !"
    - "Je suis ici. Enfin, je crois. Regardez bien, je ne suis jamais où vous pensez."
    - "Surprise ! Vous m'attendiez, n'est-ce pas ? Non ? Moi non plus."
  propose:
    - "{player}, mon ami, mon cher ami ! J'ai {power} pour toi, et je ne mens jamais. Presque jamais."
    - "{power}, dis-tu ? Quelle coïncidence, j'en avais justement un sous la main !"
  accept:
    - "Génial ! Tu as signé, tu es magnifique ! Relis donc les petites lignes, {player}, ha !"
    - "Oh, tu y as cru ! Félicitations, c'est fait, tu es à moi, enfin, à toi !"
  refuse:
    - "Refuser ? Moi qui te trouvais si malin ! Tu as raison, d'ailleurs. Ou pas."
    - "Hé ! Reviens ! Je plaisantais ! Ou pas. Ou si."
  timeout:
    - "Tu ne dis rien ? Ça tombe bien, moi non plus. Je m'éclipse."
    - "Cinq minutes ! Tu as joué à la perfection, {player}. Je suis déjà parti, d'ailleurs."
  sacrifice:
    HEARTS:
      - "Un peu de ta vie, {cost}, rien de grave ! Tu n'en auras pas besoin, c'est un jeu."
      - "{cost}, sois sérieux, c'est une offrande ridicule. Ou presque."
    HEADS:
      - "Je veux {cost}, pour les collectionner, évidemment, bien sûr, absolument !"
      - "{cost}, voilà un cadeau qui ne s'oublie pas !"
    ITEMS:
      - "{cost}, pour mon petit spectacle. Promis, tu les reverras. Peut-être."
      - "Donne-moi {cost}, et je te promets de ne pas te voler. Cette fois."

GRAVITAS:
  appear:
    - "Courbez la tête, créatures. Ce monde tient parce que je le veux."
    - "Tout ce qui tombe, tombe pour moi. Tout ce qui s'élève, s'élève par ma permission."
    - "Je suis GRAVITAS. Vos mondes pèsent peu devant moi."
  propose:
    - "Mortel {player}, je daigne t'offrir {power}. Sois reconnaissant."
    - "{power}, voilà ce que ma grandeur t'accorde. Ne gâche pas cette faveur, {player}."
  accept:
    - "Sage décision. Tu pèseras désormais plus que ta propre ombre, {player}."
    - "Le poids du monde est avec toi. Ne t'effondre pas sous lui."
  refuse:
    - "Tu refuses ? Insignifiant. La gravité n'a pas besoin de toi."
    - "Tu t'abaisses toi-même, mortel. Je n'insisterai pas."
  timeout:
    - "Ton silence est une insulte à ma grandeur. Je me retire."
    - "Cinq minutes de mon temps gaspillées. Tu en paieras le poids un jour."
  sacrifice:
    HEARTS:
      - "{cost}. Une obole bien légère pour mon pouvoir."
      - "Tu me céderas {cost}. Considère-le comme un honneur."
    HEADS:
      - "J'exige {cost}. Les têtes s'inclinent d'elles-mêmes devant moi."
      - "{cost}. Que ces trophées reconnaissent leur maître."
    ITEMS:
      - "Offre-moi {cost}, et considère-toi béni."
      - "{cost}. Que ta richesse tombe à mes pieds."

VOID:
  appear:
    - "Il n'y avait rien. Il y aura rien. Mais je suis ici entre les deux."
    - "Ceci n'est pas une voix. Ceci n'est pas une présence. Ceci est."
    - "Regardez-moi. Non, ne me regardez pas. Vous ne me voyez déjà plus."
  propose:
    - "{player}. {power}. Le néant offre ce qu'il n'a pas."
    - "Entends-tu l'écho de {power} ? Il t'appelait avant ta naissance, {player}."
  accept:
    - "Signé. Ou pas encore. Ou depuis toujours. {player}, tu appartiens au vide."
    - "Il est fait. Il n'est pas fait. Il l'était. Tu comprendras, ou non."
  refuse:
    - "Refus. Accepté. Oublié. Tu n'as jamais été ici."
    - "Le néant ne se vexe pas. Il attend, simplement."
  timeout:
    - "Cinq minutes. Cinq siècles. Cinq instants. Aucune différence."
    - "Tu n'as rien dit. Le silence est ma langue maternelle."
  sacrifice:
    HEARTS:
      - "{cost}. Ce que tu perds, tu ne l'avais jamais."
      - "Un cœur, un néant. {cost}. Quelle différence ?"
    HEADS:
      - "{cost}. Les têtes ne pensent plus, le vide s'en réjouit."
      - "Apporte {cost} et regarde-les disparaître, comme tout."
    ITEMS:
      - "{cost}. Donne ce qui n'a pas de valeur pour qui n'existe pas."
      - "Je veux {cost}. Je ne sais pas pourquoi. Toi non plus."
""";
}
